<?php
if (session_status() === PHP_SESSION_NONE) {
    session_name(SESSION_NAME);
    session_start();
}
require_once __DIR__ . '/config.php';
require_once __DIR__ . '/functions.php';
$csrf_token = generate_csrf_token();
?>
<!doctype html>
<html lang="pt-br">
<head>
    <meta charset="utf-8">
    <meta name="viewport" content="width=device-width, initial-scale=1">
    <title><?= e(APP_NAME) ?></title>
    <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.3/dist/css/bootstrap.min.css" rel="stylesheet">
    <link href="assets/css/style.css" rel="stylesheet">
</head>
<body>
<nav class="navbar navbar-expand-lg navbar-dark bg-dark">
    <div class="container-fluid">
        <a class="navbar-brand" href="dashboard.php"><?= e(APP_NAME) ?></a>
        <div class="d-flex">
            <?php if (is_logged_in()): ?>
                <a href="dashboard.php" class="btn btn-outline-light btn-sm me-2">Dashboard</a>
                <a href="users.php" class="btn btn-outline-light btn-sm me-2">Usuários</a>
                <a href="logs.php" class="btn btn-outline-light btn-sm me-2">Logs</a>
                <a href="logout.php" class="btn btn-danger btn-sm">Sair</a>
            <?php endif; ?>
        </div>
    </div>
</nav>
<div class="container py-4">