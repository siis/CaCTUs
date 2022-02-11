<?php

try
{
    $pdo = new PDO('mysql:host=localhost;dbname=frames;charset=utf8', 'username', 'password');
    $req = $pdo->prepare("TRUNCATE TABLE cameraX");
    $req->execute();
}
catch (Exception $e)
{
    die('Erreur : ' . $e->getMessage());
}

?>