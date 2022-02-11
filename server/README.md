# Nginx Server

Follow these instructions to configured the web server used to upload and download encrypted frames:

* Deploy a Nginx web server with PHP and a MySQL instance installed: 
    ```
    sudo apt-get install nginx php php-fpm php-mysql mariadb-server
    ```

* Configure database: 
    ```
    sudo mysql_secure_installation
    sudo mysql -u root
    MariaDB >CREATE DATABASE frames;
    MariaDB >CREATE USER 'user1'@localhost IDENTIFIED BY 'password1';
    MariaDB >GRANT ALL PRIVILEGES ON frames.* TO 'user1'@localhost;
    MariaDB >FLUSH PRIVILEGES;
    ```
* Use the SQL commands provided in the `structure.sql` file to create a table for each camera. Note: the placeholder `"X"` in `cameraX` was used to identify which camera was being used, if you have only one, you can leave it as is, drop it, or change it and adapt accordingly the other corresponding files on the web server, smartphone application, and camera system.

* Edit the default NGINX configuration file (`/etc/nginx/sites-enabled/default`) with the `nginx_config_file` (pay attention to the correct version of PHP socket you put).
  
* Create `/srv/cameras`,  `/srv/cameras/cameraX`, `/srv/cameras/indexX.php`, and `/srv/upload/renameX.php` (refer to the template structure provided). Make sure that `www-data` is the owner and has the correct permissions. Adapt the username and password to use to access the database.
  
* Enable and restart the Nginx service (you may have to disable and stop or remove Apache if it is installed on the machine):
    ```
    sudo systemctl enable nginx
    sudo systemctl restart nginx
    ```

* Make sure that no firewall rule blocks connection to the ports used for the web traffic (80 and 8080)

Note: we additionally provide a `delete_frames.sh` and `truncate.php` scripts to facilitate clean up of files and database tables between several runs. These scripts must be adapted to the according number of camera devices and tables being used.
