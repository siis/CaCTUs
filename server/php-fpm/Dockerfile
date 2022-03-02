FROM php:fpm  
RUN docker-php-ext-install pdo_mysql 
RUN usermod -u 101 www-data
#uid 101 corresponds to the one of the nginx user in the nginx container
