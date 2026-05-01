<?php
require_once __DIR__ . '/includes/auth.php';
require_once __DIR__ . '/includes/header.php';

$status = $_GET['status'] ?? '';
$search = trim($_GET['search'] ?? '');

$sql = "SELECT * FROM users WHERE 1=1";
$params = [];

if ($status !== '') {
    $sql .= " AND status = ?";
    $params[] = $status;
}

if ($search !== '') {
    $sql .= " AND (name LIKE ? OR login_code LIKE ?)";
    $params[] = "%$search%";
    $params[] = "%$search%";
}

$sql .= " ORDER BY created_at DESC";
$stmt = $pdo->prepare($sql);
$stmt->execute($params);
$users = $stmt->fetchAll();

function user_status_label($user) {
    $status = calculate_status($user['expires_at'], $user['status']);
    return match ($status) {
        'active' => '<span class="badge bg-success badge-status">Ativo</span>',
        'inactive' => '<span class="badge bg-secondary badge-status">Inativo</span>',
        'expired' => '<span class="badge bg-danger badge-status">Expirado</span>',
        default => '<span class="badge bg-dark badge-status">Indefinido</span>',
    };
}
?>

<div class="d-flex justify-content-between align-items-center mb-4">
    <h1>Usuários</h1>
    <a href="user_form.php" class="btn btn-primary">Novo usuário</a>
</div>

<div class="card p-3 mb-4">
    <form class="row g-3" method="GET">
        <div class="col-md-6">
            <input type="text" name="search" class="form-control" placeholder="Buscar por nome ou login" value="<?= e($search) ?>">
        </div>
        <div class="col-md-4">
            <select name="status" class="form-select">
                <option value="">Todos os status</option>
                <option value="active" <?= $status === 'active' ? 'selected' : '' ?>>Ativo</option>
                <option value="inactive" <?= $status === 'inactive' ? 'selected' : '' ?>>Inativo</option>
                <option value="expired" <?= $status === 'expired' ? 'selected' : '' ?>>Expirado</option>
            </select>
        </div>
        <div class="col-md-2">
            <button class="btn btn-outline-primary w-100">Filtrar</button>
        </div>
    </form>
</div>

<div class="card p-3">
    <div class="table-responsive">
        <table class="table table-hover align-middle">
            <thead>
                <tr>
                    <th>ID</th>
                    <th>Nome</th>
                    <th>Login</th>
                    <th>Status</th>
                    <th>Expira em</th>
                    <th>Criado em</th>
                    <th>Ações</th>
                </tr>
            </thead>
            <tbody>
                <?php foreach ($users as $user): 
                    $effective_status = calculate_status($user['expires_at'], $user['status']);
                ?>
                    <tr>
                        <td><?= e($user['id']) ?></td>
                        <td><?= e($user['name']) ?></td>
                        <td><strong><?= e($user['login_code']) ?></strong></td>
                        <td><?= user_status_label($user) ?></td>
                        <td><?= e(format_date($user['expires_at'])) ?></td>
                        <td><?= e(format_date($user['created_at'])) ?></td>
                        <td>
                            <a href="user_form.php?id=<?= e($user['id']) ?>" class="btn btn-sm btn-warning">Editar</a>
                            <form action="user_delete.php" method="POST" class="d-inline delete-form">
                                <input type="hidden" name="csrf_token" value="<?= e($csrf_token) ?>">
                                <input type="hidden" name="id" value="<?= e($user['id']) ?>">
                                <button class="btn btn-sm btn-danger">Excluir</button>
                            </form>
                        </td>
                    </tr>
                <?php endforeach; ?>
            </tbody>
        </table>
    </div>
</div>

<?php require_once __DIR__ . '/includes/footer.php'; ?>