<?php
require_once __DIR__ . '/includes/auth.php';
require_once __DIR__ . '/includes/header.php';

$totalUsers = $pdo->query("SELECT COUNT(*) FROM users")->fetchColumn();
$activeUsers = $pdo->query("SELECT COUNT(*) FROM users WHERE status = 'active' AND expires_at >= NOW()")->fetchColumn();
$inactiveUsers = $pdo->query("SELECT COUNT(*) FROM users WHERE status = 'inactive'")->fetchColumn();
$expiredUsers = $pdo->query("SELECT COUNT(*) FROM users WHERE status = 'expired' OR expires_at < NOW()")->fetchColumn();
?>

<h1 class="mb-4">Dashboard</h1>

<div class="row g-4">
    <div class="col-md-3">
        <div class="card p-3">
            <h5>Total de usuários</h5>
            <h2><?= e($totalUsers) ?></h2>
        </div>
    </div>
    <div class="col-md-3">
        <div class="card p-3">
            <h5>Ativos</h5>
            <h2 class="text-success"><?= e($activeUsers) ?></h2>
        </div>
    </div>
    <div class="col-md-3">
        <div class="card p-3">
            <h5>Inativos</h5>
            <h2 class="text-secondary"><?= e($inactiveUsers) ?></h2>
        </div>
    </div>
    <div class="col-md-3">
        <div class="card p-3">
            <h5>Expirados</h5>
            <h2 class="text-danger"><?= e($expiredUsers) ?></h2>
        </div>
    </div>
</div>

<div class="mt-4 card p-3">
    <h5>Bem-vindo, <?= e($_SESSION['admin_name']) ?></h5>
    <p class="mb-0">Use o menu para gerenciar usuários e visualizar logs.</p>
</div>

<?php require_once __DIR__ . '/includes/footer.php'; ?>