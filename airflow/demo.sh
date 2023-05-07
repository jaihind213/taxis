#!/bin/bash

# PLEASE SET THIS
export HOST_IP=192.168.1.11

#build locally the airflow
#docker build -t jaihind213/airflow:1.0 -t jaihind213/airflow:latest .

docker-compose down --volumes --remove-orphans

#########prep config.ini

touch config.ini
cat /dev/null > config.ini
cp config.ini_template config.ini

echo "[postgres]" >> config.ini
echo "host=${HOST_IP}" >> config.ini
echo "port=5432" >> config.ini
echo "database=postgres" >> config.ini
echo "user=postgres" >> config.ini
echo "password=postgres" >> config.ini
echo "table=trips" >> config.ini

echo "" >> config.ini

echo "[influxdb]" >> config.ini
echo "host=${HOST_IP}" >> config.ini
echo "port=8086" >> config.ini
echo "database=trips" >> config.ini
echo "table=trips" >> config.ini
echo "user=posgres" >> config.ini
echo "password=posgres" >> config.ini

touch config2.ini
cat /dev/null > config2.ini
cp config2.ini_template config2.ini
echo "host=${HOST_IP}" >> config2.ini

################
mkdir -p /tmp/staging/csv
mkdir -p /tmp/trips_bucket/csv
rm -rf /tmp/staging/csv/*
rm -rf /tmp/trips_bucket/csv/*


echo "setting up. increase sleep if setup fails"
docker-compose up postgis -d
docker-compose up grpc -d
docker-compose up influx -d

sleep 80
docker cp `pwd`/../db_loader_download_api/trips.sql postgis:/tmp/
docker exec -it postgis psql -U postgres -d postgres -f /tmp/trips.sql
curl -XPOST "http://${HOST_IP}:8086/query" --data-urlencode "q=CREATE DATABASE trips"


docker-compose up summary -d
docker-compose up airflow-init 
docker-compose up airflow-scheduler airflow-webserver

#docker cp `pwd`/../db_loader_download_api/trips.sql postgis:/tmp/
#docker exec -it postgis psql -U postgres -d postgres -f /tmp/trips.sql
#curl -XPOST "http://${HOST_IP}:8086/query" --data-urlencode "q=CREATE DATABASE trips"
################



