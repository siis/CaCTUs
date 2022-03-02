<?php

try
{
    $pdo = new PDO('mysql:host=mariadb-cactus;dbname=frames;charset=utf8', 'user1', 'password1');
    $req = $pdo->prepare("TRUNCATE TABLE cameraX");
    $req->execute();
}
catch (Exception $e)
{
    die('Erreur : ' . $e->getMessage());
}

?>