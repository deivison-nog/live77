<?php
require_once __DIR__ . '/includes/auth.php';

$id = isset($_GET['id']) ? (int)$_GET['id'] : 0;
$user = [
    'name' => '',
    'login_code' => '',
    'password_code' => '',
    'status' => 'active',
    'expires_at' => date('Y-m-d H:i:s', strtotime('+' . DEFAULT_ACCESS_DAYS . ' days'))
];

if ($id) {
    $stmt = $pdo->prepare("SELECT * FROM users WHERE id = ?");
    $stmt->execute([$id]);
    $user = $stmt->fetch();
    if (!$user) {
        die('Usuário não encontrado.');
    }
}

$error = '';

if ($_SERVER['REQUEST_METHOD'] === 'POST') {
    $csrf = $_POST['csrf_token'] ?? '';
    if (!verify_csrf_token($csrf)) {
        $error = 'Falha de segurança.';
    } else {
        $name = trim($_POST['name'] ?? '');
        $login_code = trim($_POST['login_code'] ?? '');
        $password_code = trim($_POST['password_code'] ?? '');
        $status = $_POST['status'] ?? 'active';
        $expires_at = $_POST['expires_at'] ?? '';

        if ($name === '' || !is_valid_six_digit_number($login_code) || !is_valid_six_digit_number($password_code)) {
            $error = 'Preencha corretamente os campos. Login e senha devem ter 6 números.';
        } elseif (!in_array($status, ['active', 'inactive', 'expired'])) {
            $error = 'Status inválido.';
        } else {
            if ($id) {
                $stmt = $pdo->prepare("UPDATE users SET name=?, login_code=?, password_code=?, status=?, expires_at=? WHERE id=?");
                $stmt->execute([$name, $login_code, $password_code, $status, $expires_at, $id]);
                log_action($pdo, $_SESSION['admin_id'], 'update_user', "Usuário ID $id atualizado");
            } else {
                $stmt = $pdo->prepare("INSERT INTO users (name, login_code, password_code, status, created_at, expires_at) VALUES (?, ?, ?, ?, NOW(), ?)");
                $stmt->execute([$name, $login_code, $password_code, $status, $expires_at]);
                log_action($pdo, $_SESSION['admin_id'], 'create_user', "Novo usuário criado: $name");
            }
            redirect('users.php');
        }
    }
}

$csrf_token = generate_csrf_token();
?>

<?php require_once __DIR__ . '/includes/header.php'; ?>

<h1 class="mb-4"><?= $id ? 'Editar usuário' : 'Novo usuário' ?></h1>

<?php if ($error): ?>
    <div class="alert alert-danger"><?= e($error) ?></div>
<?php endif; ?>

<div class="card p-4">
    <form method="POST">
        <input type="hidden" name="csrf_token" value="<?= e($csrf_token) ?>">

        <div class="mb-3">
            <label class="form-label">Nome</label>
            <input type="text" name="name" class="form-control" value="<?= e($user['name']) ?>" required>
        </div>

        <div class="mb-3">
            <label class="form-label">Login de 6 números</label>
            <input type="text" name="login_code" maxlength="6" class="form-control" value="<?= e($user['login_code']) ?>" required>
        </div>

        <div class="mb-3">
            <label class="form-label">Senha de 6 números</label>
            <input type="text" name="password_code" maxlength="6" class="form-control" value="<?= e($user['password_code']) ?>" required>
        </div>

        <div class="mb-3">
            <label class="form-label">Status</label>
            <select name="status" class="form-select">
                <option value="active" <?= $user['status'] === 'active' ? 'selected' : '' ?>>Ativo</option>
                <option value="inactive" <?= $user['status'] === 'inactive' ? 'selected' : '' ?>>Inativo</option>
                <option value="expired" <?= $user['status'] === 'expired' ? 'selected' : '' ?>>Expirado</option>
            </select>
        </div>

        <div class="mb-3">
            <label class="form-label">Vencimento</label>
            <input type="datetime-local" name="expires_at" class="form-control" value="<?= e(date('Y-m-d\TH:i', strtotime($user['expires_at']))) ?>" required>
        </div>

        <button class="btn btn-success">Salvar</button>
        <a href="users.php" class="btn btn-secondary">Voltar</a>
    </form>
</div>

<?php require_once __DIR__ . '/includes/footer.php'; ?>