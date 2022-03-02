<?php

try
{
    $pdo = new PDO('mysql:host=mariadb-cactus;dbname=frames;charset=utf8', 'user1', 'password1');
}
catch (Exception $e)
{
    die('Erreur : ' . $e->getMessage());
}

if( (isset($_GET['t1']) or !empty($_GET['t1'])) and (isset($_GET['t1']) or !empty($_GET['t1'])) ){

    $t1 = $_GET['t1'];
    $t2 = $_GET['t2'];

    if ($t1 > $t2){
        http_response_code(400);
        die();
    }else{

        $req=$pdo->prepare('SELECT * FROM cameraX WHERE timestamp>=:t1 AND timestamp<=:t2  ORDER BY timestamp');
		$req->execute(array(
			':t1'=>$t1,
            ':t2'=>$t2
		));
        $frames=$req->fetchAll();

        ?>
        <html><head><title>Index of /cameraX/</title></head>
        <body bgcolor="white">
        <h1>Index of /cameraX/</h1>
        <hr>
        <pre>

        <?php
        foreach ($frames as $frame) {
            ?>
            <a href="<?php echo $frame['timestamp'];?>"><?php echo $frame['timestamp'];?></a>
            <?php
        }
        ?>

        </pre><hr></body>
        </html>

        <?php
    }
}else{
    http_response_code(400);
    die();
}

?>