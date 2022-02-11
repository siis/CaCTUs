<?php

try
{
    $pdo = new PDO('mysql:host=localhost;dbname=frames;charset=utf8', 'username', 'password');
}
catch (Exception $e)
{
    die('Erreur : ' . $e->getMessage());
}

if( $_SERVER["HTTP_X_FILE"] && $_SERVER["HTTP_CORRECT_FILENAME"] ) {

    $timestamp = $_SERVER["HTTP_CORRECT_FILENAME"];
    $temp_name = $_SERVER["HTTP_X_FILE"];
    $name = '/srv/cameras/cameraX/' . $timestamp;

    $result = rename($temp_name, $name);
    if ($result){
        $req = $pdo->prepare("INSERT INTO cameraX (timestamp) VALUES (?)");
        $req->execute([$timestamp]);
    }
}

die();

?>