<?php

try
{
    $pdo = new PDO('mysql:host=mariadb-cactus;dbname=frames;charset=utf8', 'user1', 'password1');
}
catch (Exception $e)
{
    die('Erreur : ' . $e->getMessage());
}

if( $_SERVER["HTTP_X_FILE"] && $_SERVER["HTTP_CORRECT_FILENAME"] ) {

    $timestamp = $_SERVER["HTTP_CORRECT_FILENAME"];
    $temp_name = $_SERVER["HTTP_X_FILE"];
    $name = '/var/www/html/cameras/cameraX/' . $timestamp;

    $result = rename($temp_name, $name);
    if ($result){
        $req = $pdo->prepare("INSERT INTO cameraX (timestamp) VALUES (?)");
        $req->execute([$timestamp]);
    }
}

die();

?>