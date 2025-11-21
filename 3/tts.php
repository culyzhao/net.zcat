<?php
// tts.php
header("Access-Control-Allow-Origin: *");
header("Access-Control-Allow-Methods: GET");
header("Access-Control-Allow-Headers: Content-Type");

$text = isset($_GET['text']) ? trim($_GET['text']) : "";
$lang = isset($_GET['lang']) ? trim($_GET['lang']) : "en";

if ($text === "") {
    http_response_code(400);
    echo "Missing text parameter";
    exit;
}

// Google Translate TTS 官方接口的非公开可用端点
$api = "https://translate.google.com/translate_tts";

// 参数构建
$params = http_build_query([
    "ie"   => "UTF-8",
    "q"    => $text,
    "tl"   => $lang,
    "client" => "tw-ob"
]);

$url = $api . "?" . $params;

// 通过 curl 抓取音频
$ch = curl_init($url);
curl_setopt($ch, CURLOPT_USERAGENT, "Mozilla/5.0"); // 必须伪装 UA
curl_setopt($ch, CURLOPT_RETURNTRANSFER, true);

$audio = curl_exec($ch);

if (curl_errno($ch)) {
    http_response_code(500);
    echo "TTS request failed: " . curl_error($ch);
    curl_close($ch);
    exit;
}

$code = curl_getinfo($ch, CURLINFO_HTTP_CODE);
curl_close($ch);

// 如果 Google 返回 200，则输出 mp3
if ($code == 200 && $audio) {
    header("Content-Type: audio/mpeg");
    header("Content-Length: " . strlen($audio));
    echo $audio;
    exit;
}

http_response_code(500);
echo "TTS failed";
