#!/bin/sh
rm -r /var/www/html/cameras/camera*
mkdir /var/www/html/cameras/cameraX ##add other camera folders if neeeded
chown -R www-data:root /var/www/html/cameras
php ./truncate.php