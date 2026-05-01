<?php
require_once __DIR__ . '/includes/auth.php';
require_once __DIR__ . '/includes/header.php';

$stmt = $pdo->query("
    SELECT l.*, a.name AS admin_name
    FROM activity_logs l
    INNER JOIN admins a ON a.id = l.admin_id
    ORDER BY l.created_at DESC
    LIMIT 100
");
$logs = $stmt->fetchAll();
?>

<h1 class="mb-4">Logs de atividade</h1>

<div class="card p-3">
    <div class="table-responsive">
        <table class="table table-striped align-middle">
            <thead>
                <tr>
                    <th>Data</th>
                    <th>Admin</th>
                    <th>Ação</th>
                    <th>Detalhes</th>
                </tr>
            </thead>
            <tbody>
                <?php foreach ($logs as $log): ?>
                    <tr>
                        <td><?= e(format_date($log['created_at'])) ?></td>
                        <td><?= e($log['admin_name']) ?></td>
                        <td><?= e($log['action']) ?></td>
                        <td><?= e($log['details']) ?></td>
                    </tr>
                <?php endforeach; ?>
            </tbody>
        </table>
    </div>
</div>

<?php require_once __DIR__ . '/includes/footer.php'; ?>