<?php

function jsonresult($msg) {
    $j = array('result'=>'');
    $j['result'] = $msg;
    return json_encode($j);
}

function dbquery($conn, $stmt, $sql, $param)
{
  $res = pg_prepare($conn, $stmt, $sql) or die( jsonresult('Prepare failed: ' . pg_last_error()));
  $res = pg_execute($conn, $stmt, $param) or die( jsonresult('Execute failed: ' . pg_last_error()));
  return $res;
}
header('Content-Type: application/json');
// get the HTTP method, path and body of the request
$method = $_SERVER['REQUEST_METHOD'];
$request = explode('/', trim($_SERVER['PATH_INFO'],'/'));
$route = preg_replace('/[^a-z0-9_]+/i','',array_shift($request));
//$input = file_get_contents('php://input');
if ($route == 'wx')
{
    // connect to the mysql database
    $dbconn = pg_connect("host=127.0.0.1 port=5432 dbname=qualitydb user=qduser password=Pass1234")
        or die( jsonresult('Could not connect: ' . pg_last_error()) );
    // create SQL based on HTTP method
    switch ($method)
    {
      case 'GET':
        $result = dbquery($dbconn, 'get', 'SELECT * FROM quality_cases', array());
        $list = pg_fetch_row($result);
        echo ($list[0]);
        break;
      case 'POST':

        $input = $_POST['data'];
        json_decode($input) != null or die( jsonresult('Json format e2rror!') );
        //backup data
       $result = dbquery($dbconn, 'upd', 'UPDATE quality_cases SET caseinfo=$1', array($input));
        if(!empty($_FILES['file']['tmp_name'])) {

            if(!is_uploaded_file($_FILES['file']['tmp_name'])) {
                die( jsonresult('File upload failed.') );
            }
            $uploaded_file = $_FILES['file']['tmp_name'];
            $upload_path = $_SERVER['DOCUMENT_ROOT']."/cases/upload/";
            $file_true_name=$_FILES['file']['name'];
            if (!move_uploaded_file($uploaded_file,iconv("utf-8","gb2312", $upload_path.$file_true_name))) {
                die( jsonresult('File save failed.')) ;
            }
        }
        echo (jsonresult('0'));
        break;
    }
    // Free resultset
    pg_free_result($result);
    // Closing connection
    pg_close($dbconn);
}
?>
