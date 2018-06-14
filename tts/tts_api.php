<?php

function dbquery($conn, $stmt, $sql, $param)
{
  $res = pg_prepare($conn, $stmt, $sql) or die('Prepare failed: ' . pg_last_error());
  $res = pg_execute($conn, $stmt, $param) or die('Execute failed: ' . pg_last_error());
  return $res;
}


header('Content-Type: application/json');

// get the HTTP method, path and body of the request
$method = $_SERVER['REQUEST_METHOD'];
$request = explode('/', trim($_SERVER['PATH_INFO'],'/'));

$route = preg_replace('/[^a-z0-9_]+/i','',array_shift($request));
$input = file_get_contents('php://input');

if ($route == 'words') 
{
    // connect to the mysql database
    $dbconn = pg_connect("*******************")
        or die('Could not connect: ' . pg_last_error());

    // create SQL based on HTTP method
    switch ($method) 
    {
      case 'GET':
        $result = dbquery($dbconn, 'get', 'SELECT * FROM words', array());
        $list = pg_fetch_row($result);
        echo ($list[0]);
        break;

      case 'PUT':
        json_decode($input) != null or die('Json format error!');
        //backup data
        $result = dbquery($dbconn, 'ins', 'INSERT INTO words_bk(list) select list from words', array());
        $result = dbquery($dbconn, 'upd', 'UPDATE words SET list=$1', array($input));

        echo (json_encode(pg_num_rows($result))); 
        break;
    }

    // Free resultset
    pg_free_result($result);

    // Closing connection
    pg_close($dbconn);

}



?>
