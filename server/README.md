# Nginx Server

For convenience in the deployment, we provide a `docker-compose.yml` configuration file.

1. Edit the `nginx.conf` as well as the `srv/` folder accordingly to the number of cameras you want to support. In the php files in the `srv/` folder edit the user and password (`user1` and `password1` by default) that will be used to connect to the database and that we will configure in step 4. 
2. Create a volume to store the database-related information:
   ```
   docker volume create mariadb_vol
   ```
3. Bring up the containers (after editing `MYSQL_ROOT_PASSWORD` in `docker-compose.yml`):
    ```
    docker-compose up -d
    ```
4. Configure the database:
    ```
    docker exec -it mariadb-cactus mysql -u root -p
    MariaDB >CREATE DATABASE frames;
    MariaDB >CREATE USER 'user1'@'%' IDENTIFIED BY 'password1';
    MariaDB >GRANT ALL PRIVILEGES ON frames.* TO 'user1'@'%';
    MariaDB >FLUSH PRIVILEGES;
    MariaDB >USE frames;
    ```
    Use the following SQL commands to create a table for each camera. Note: the placeholder `"X"` in `cameraX` was used to identify which camera was being used, if you have only one, you can leave it as is, drop it, or change it and adapt accordingly the other corresponding files on the web server, smartphone application, and camera system.

    ```
    --
    -- Table structure for table `cameraX`
    --
    DROP TABLE IF EXISTS `cameraX`;
    CREATE TABLE `cameraX` (
    `id` BIGINT AUTO_INCREMENT PRIMARY KEY,
    `timestamp` BIGINT UNSIGNED NOT NULL
    );
    ```
Note: we additionally provide a `delete_frames.sh` and `truncate.php` scripts to facilitate clean up of files and database tables between several runs. These scripts must be adapted to the according number of camera devices and tables being used.

Finally make sure that no firewall rule blocks connections to ports `80` and `8080` on your host.

# Performance Evaluation

For the performance evaluation, it could be useful to log when each frame has been uploaded:

1. Add a new column to the corresponding table and define a new trigger:
   ```
    docker exec -it mariadb-cactus mysql -u user1 -p
    MariaDB >USE frames;
    MariaDB >ALTER TABLE cameraX ADD uploadtime BIGINT UNSIGNED;
    MariaDB >CREATE TRIGGER before_insert_cameraX BEFORE INSERT ON cameraX FOR EACH ROW SET new.uploadtime = FLOOR(UNIX_TIMESTAMP(NOW(3))*1000);
    ```
2. To dump the database in `/tmp`: 
    ```
    mysqldump -u user1 -p -t -T/tmp frames --fields-enclosed-by=\" --fields-terminated-by=,
    ```