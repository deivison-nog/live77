<?php
declare(strict_types=1);

header('Content-Type: application/json; charset=utf-8');

require_once __DIR__ . '/includes/config.php';
require_once __DIR__ . '/includes/db.php';
require_once __DIR__ . '/includes/functions.php';

date_default_timezone_set(APP_TIMEZONE);

function json_response(bool $success, string $message, $data = null, int $httpCode = 200): void
{
    http_response_code($httpCode);
    echo json_encode([
        'success' => $success,
        'message' => $message,
        'data' => $data
    ], JSON_UNESCAPED_UNICODE | JSON_UNESCAPED_SLASHES);
    exit;
}

function get_request_data(): array
{
    $contentType = $_SERVER['CONTENT_TYPE'] ?? $_SERVER['HTTP_CONTENT_TYPE'] ?? '';

    if (str_contains($contentType, 'application/json')) {
        $raw = file_get_contents('php://input');
        $json = json_decode($raw ?: '', true);
        $body = is_array($json) ? $json : [];
        // Merge query-string params so ?action=login works alongside a JSON body
        return array_merge($_GET, $body);
    }

    return array_merge($_GET, $_POST);
}

function get_bearer_token(): string
{
    $headers = function_exists('getallheaders') ? getallheaders() : [];
    $authHeader = $headers['Authorization'] ?? $headers['authorization'] ?? '';

    if (preg_match('/Bearer\s+(.+)/i', $authHeader, $matches)) {
        return trim($matches[1]);
    }

    return trim($_GET['token'] ?? $_POST['token'] ?? '');
}

function generate_api_token(): string
{
    return bin2hex(random_bytes(32));
}

function require_app_auth(PDO $pdo): array
{
    $token = get_bearer_token();

    if ($token === '') {
        json_response(false, 'Token não informado.', null, 401);
    }

    $stmt = $pdo->prepare("
        SELECT
            t.id AS token_id,
            t.user_id,
            t.token,
            t.expires_at AS token_expires_at,
            u.name,
            u.login_code,
            u.status,
            u.expires_at AS user_expires_at
        FROM app_tokens t
        INNER JOIN users u ON u.id = t.user_id
        WHERE t.token = ?
        LIMIT 1
    ");
    $stmt->execute([$token]);
    $row = $stmt->fetch();

    if (!$row) {
        json_response(false, 'Token inválido.', null, 401);
    }

    if (strtotime($row['token_expires_at']) < time()) {
        json_response(false, 'Sessão expirada.', null, 401);
    }

    if ($row['status'] !== 'active' || strtotime($row['user_expires_at']) < time()) {
        json_response(false, 'Usuário inativo ou expirado.', null, 403);
    }

    return $row;
}

$input = get_request_data();
$action = trim((string)($input['action'] ?? ''));

if ($action === '') {
    json_response(false, 'Ação não informada.', null, 400);
}

try {
    switch ($action) {
        case 'login':
            $login_code = trim((string)($input['login_code'] ?? ''));
            $password_code = trim((string)($input['password_code'] ?? ''));

            if (!is_valid_six_digit_number($login_code) || !is_valid_six_digit_number($password_code)) {
                json_response(false, 'Login e senha devem ter 6 números.', null, 422);
            }

            $stmt = $pdo->prepare("SELECT * FROM users WHERE login_code = ? LIMIT 1");
            $stmt->execute([$login_code]);
            $user = $stmt->fetch();

            if (!$user) {
                json_response(false, 'Usuário não encontrado.', null, 404);
            }

            if (!hash_equals((string)$user['password_code'], $password_code)) {
                json_response(false, 'Senha inválida.', null, 401);
            }

            $effectiveStatus = calculate_status($user['expires_at'], $user['status']);
            if ($effectiveStatus !== 'active') {
                json_response(false, 'Usuário inativo ou expirado.', null, 403);
            }

            $token = generate_api_token();
            $tokenExpiresAt = date('Y-m-d H:i:s', strtotime('+30 days'));

            $pdo->beginTransaction();

            $pdo->prepare("DELETE FROM app_tokens WHERE user_id = ?")->execute([$user['id']]);

            $stmt = $pdo->prepare("
                INSERT INTO app_tokens (user_id, token, created_at, expires_at)
                VALUES (?, ?, NOW(), ?)
            ");
            $stmt->execute([$user['id'], $token, $tokenExpiresAt]);

            log_app_action($pdo, (int)$user['id'], 'app_login', 'Login realizado com sucesso via API');

            $pdo->commit();

            json_response(true, 'Login realizado com sucesso.', [
                'user' => [
                    'id' => (int)$user['id'],
                    'name' => $user['name'],
                    'login_code' => $user['login_code'],
                    'status' => $effectiveStatus,
                    'expires_at' => $user['expires_at']
                ],
                'token' => $token,
                'token_expires_at' => $tokenExpiresAt
            ]);
            break;

        case 'me':
            $auth = require_app_auth($pdo);

            json_response(true, 'Dados do usuário autenticado.', [
                'user' => [
                    'id' => (int)$auth['user_id'],
                    'name' => $auth['name'],
                    'login_code' => $auth['login_code'],
                    'status' => $auth['status'],
                    'expires_at' => $auth['user_expires_at']
                ]
            ]);
            break;

        case 'items':
            require_app_auth($pdo);

            $stmt = $pdo->query("SELECT id, name, url, image_url, category FROM items ORDER BY category, name");
            $items = $stmt->fetchAll();

            json_response(true, 'Lista carregada com sucesso.', [
                'items' => $items
            ]);
            break;

        case 'logout':
            $auth = require_app_auth($pdo);

            $stmt = $pdo->prepare("DELETE FROM app_tokens WHERE token = ?");
            $stmt->execute([$auth['token']]);

            log_app_action($pdo, (int)$auth['user_id'], 'app_logout', 'Logout realizado via API');

            json_response(true, 'Logout realizado com sucesso.');
            break;

        default:
            json_response(false, 'Ação inválida.', null, 400);
    }
} catch (Throwable $e) {
    if ($pdo->inTransaction()) {
        $pdo->rollBack();
    }

    json_response(false, 'Erro interno no servidor.', [
        'error' => $e->getMessage()
    ], 500);
}
