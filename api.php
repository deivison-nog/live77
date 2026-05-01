<?php
/**
 * Live 77 – Backend API
 *
 * Endpoint : http://<host>/live77/api.php
 * Method   : POST (application/x-www-form-urlencoded)
 *
 * Actions
 * -------
 * action=login
 *   Fields : login (string), senha (string)
 *   Returns: {"success": true}  – credentials valid
 *            {"success": false} – credentials invalid
 *
 * Database : live77_admin
 * Table    : users
 *   Expected columns: login (VARCHAR), senha (VARCHAR)
 *   Passwords are stored as SHA-256 hex strings.
 *   If your passwords are stored in plain text, remove the hash comparison
 *   and compare directly: WHERE login = ? AND senha = ?
 */

// ── Database configuration ────────────────────────────────────────────────────
define('DB_HOST', 'localhost');
define('DB_NAME', 'live77_admin');
define('DB_USER', 'root');       // change to your MySQL user
define('DB_PASS', '');           // change to your MySQL password
define('DB_CHARSET', 'utf8mb4');
// ─────────────────────────────────────────────────────────────────────────────

header('Content-Type: application/json; charset=utf-8');

// Only accept POST
if ($_SERVER['REQUEST_METHOD'] !== 'POST') {
    http_response_code(405);
    echo json_encode(['success' => false, 'error' => 'Method not allowed']);
    exit;
}

$action = trim($_POST['action'] ?? '');

switch ($action) {
    case 'login':
        handleLogin();
        break;
    default:
        http_response_code(422);
        echo json_encode(['success' => false, 'error' => 'Unknown action']);
        exit;
}

// ── Handlers ──────────────────────────────────────────────────────────────────

function handleLogin(): void
{
    $login = trim($_POST['login'] ?? '');
    $senha = trim($_POST['senha'] ?? '');

    if ($login === '' || $senha === '') {
        http_response_code(422);
        echo json_encode(['success' => false, 'error' => 'login e senha sao obrigatorios']);
        exit;
    }

    $pdo = getConnection();

    // Passwords stored as SHA-256. If plain text, replace hash() with $senha directly.
    $hash = hash('sha256', $senha);

    $stmt = $pdo->prepare('SELECT id FROM users WHERE login = :login AND senha = :senha LIMIT 1');
    $stmt->execute([':login' => $login, ':senha' => $hash]);
    $user = $stmt->fetch(PDO::FETCH_ASSOC);

    echo json_encode(['success' => $user !== false]);
}

// ── Database helper ───────────────────────────────────────────────────────────

function getConnection(): PDO
{
    static $pdo = null;
    if ($pdo === null) {
        $dsn = 'mysql:host=' . DB_HOST . ';dbname=' . DB_NAME . ';charset=' . DB_CHARSET;
        $options = [
            PDO::ATTR_ERRMODE            => PDO::ERRMODE_EXCEPTION,
            PDO::ATTR_DEFAULT_FETCH_MODE => PDO::FETCH_ASSOC,
            PDO::ATTR_EMULATE_PREPARES   => false,
        ];
        try {
            $pdo = new PDO($dsn, DB_USER, DB_PASS, $options);
        } catch (PDOException $e) {
            http_response_code(500);
            echo json_encode(['success' => false, 'error' => 'Erro de conexao com o banco de dados']);
            exit;
        }
    }
    return $pdo;
}
