<?php

function e($value) {
    return htmlspecialchars($value ?? '', ENT_QUOTES, 'UTF-8');
}

function is_logged_in() {
    return isset($_SESSION['admin_id']);
}

function redirect($url) {
    header("Location: $url");
    exit;
}

function is_valid_six_digit_number($value) {
    return preg_match('/^\d{6}$/', $value);
}

function generate_csrf_token() {
    if (empty($_SESSION['csrf_token'])) {
        $_SESSION['csrf_token'] = bin2hex(random_bytes(32));
    }
    return $_SESSION['csrf_token'];
}

function verify_csrf_token($token) {
    return isset($_SESSION['csrf_token']) && hash_equals($_SESSION['csrf_token'], $token);
}

function log_action($pdo, $admin_id, $action, $details = null) {
    $stmt = $pdo->prepare("INSERT INTO activity_logs (admin_id, action, details, created_at) VALUES (?, ?, ?, NOW())");
    $stmt->execute([$admin_id, $action, $details]);
}

function log_app_action($pdo, $user_id, $action, $detail = null) {
    $stmt = $pdo->prepare("INSERT INTO logs (user_id, action, detail, created_at) VALUES (?, ?, ?, NOW())");
    $stmt->execute([$user_id, $action, $detail]);
}

function calculate_status($expires_at, $manual_status = 'active') {
    if ($manual_status !== 'active') {
        return $manual_status;
    }

    if (strtotime($expires_at) < time()) {
        return 'expired';
    }

    return 'active';
}

function format_date($date) {
    if (!$date) return '-';
    return date('d/m/Y H:i', strtotime($date));
}