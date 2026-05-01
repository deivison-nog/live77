<?php
require_once __DIR__ . '/includes/auth.php';

if ($_SERVER['REQUEST_METHOD'] !== 'POST') {
    redirect('users.php');
}

$csrf = $_POST['csrf_token'] ?? '';
$id = (int)($_POST['id'] ?? 0);

if (!verify_csrf_token($csrf)) {
    die('Falha de segurança.');
}

$stmt = $pdo->prepare("DELETE FROM users WHERE id = ?");
$stmt->execute([$id]);

log_action($pdo, $_SESSION['admin_id'], 'delete_user', "Usuário ID $id removido");

redirect('users.php');