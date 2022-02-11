#!/bin/sh
rm -r /srv/cameras/camera*
mkdir /srv/cameras/cameraX ##add other camera folders if neeeded
chown -R www-data:root /srv/cameras
php ./truncate.php