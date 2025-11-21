<?php
// words.php
// Simple REST endpoints for GET /words and PUT /words
// Deploy as /words.php and use requests to /words.php (GET and PUT)

ini_set('display_errors', 1);
ini_set('display_startup_errors', 1);
error_reporting(E_ALL);


header('Access-Control-Allow-Origin: *');
header('Access-Control-Allow-Methods: GET, PUT, OPTIONS');
header('Access-Control-Allow-Headers: Content-Type');
if ($_SERVER['REQUEST_METHOD'] === 'OPTIONS') {
    http_response_code(204);
    exit;
}

$dsn = 'pgsql:host=db;port=5432;dbname=tts';
$dbUser = 'tts';
$dbPass = 'ttsX1799'; // placeholder password

try {
    $pdo = new PDO($dsn, $dbUser, $dbPass, [
        PDO::ATTR_ERRMODE => PDO::ERRMODE_EXCEPTION,
        PDO::ATTR_DEFAULT_FETCH_MODE => PDO::FETCH_ASSOC,
    ]);
} catch (PDOException $e) {
    http_response_code(500);
    echo json_encode(['error' => 'Database connection failed']);
    exit;
}

$method = $_SERVER['REQUEST_METHOD'];

if ($method === 'GET') {
    // GET /words?count=...&type=...
    $count = isset($_GET['count']) ? (int)$_GET['count'] : 100;
    $type = isset($_GET['type']) ? (int)$_GET['type'] : 9;

    // clamp sensible limits
    if ($count <= 0) $count = 100;
    if ($count > 1000) $count = 1000;

    // prepare SQL - using placeholders for integers
    $sql = "SELECT id, word, pron, trans, stat, times FROM public.high WHERE stat > 0 AND stat <= :type AND times <= 5 ORDER BY RANDOM() LIMIT :count";
    $stmt = $pdo->prepare($sql);
    $stmt->bindValue(':type', $type, PDO::PARAM_INT);
    // Some PDO drivers require cast to int for LIMIT
    $stmt->bindValue(':count', $count, PDO::PARAM_INT);

    try {
        $stmt->execute();
        $rows = $stmt->fetchAll();
        // Each row is already an associative array with keys as column names
        header('Content-Type: application/json; charset=utf-8');
        echo json_encode(array_values($rows), JSON_UNESCAPED_UNICODE);
        exit;
    } catch (PDOException $e) {
        http_response_code(500);
        echo json_encode(['error' => 'Query failed']);
        exit;
    }
}

if ($method === 'PUT') {
    // Read PUT data (accept JSON or form-encoded)
    $data = file_get_contents('php://input');
    $id = null;

    // try JSON
    $decoded = json_decode($data, true);
    if (json_last_error() === JSON_ERROR_NONE && isset($decoded['id'])) {
        $id = (int)$decoded['id'];
    } else {
        // try parse as query string
        parse_str($data, $putVars);
        if (isset($putVars['id'])) {
            $id = (int)$putVars['id'];
        }
    }

    if (empty($id)) {
        http_response_code(400);
        echo json_encode(['error' => 'Missing id']);
        exit;
    }

    $sql = "UPDATE public.high SET times = times + 1 WHERE id = :id";
    $stmt = $pdo->prepare($sql);
    $stmt->bindValue(':id', $id, PDO::PARAM_INT);
    try {
        $stmt->execute();
        header('Content-Type: application/json; charset=utf-8');
        echo json_encode(['ok' => true]);
        exit;
    } catch (PDOException $e) {
        http_response_code(500);
        echo json_encode(['error' => 'Update failed']);
        exit;
    }
}

// Unsupported method
http_response_code(405);
echo json_encode(['error' => 'Method not allowed']);
