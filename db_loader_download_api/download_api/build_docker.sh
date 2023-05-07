#!/bin/bash
cd "$(dirname "$0")"
#todo: have seperate pyproject.toml for download_api project as parent is polluting deps
#and making docker image large
poetry export --without-hashes --format=requirements.txt|grep -v numpy |grep -v pytest-grpc|grep -v pandas|grep -v pyarrow|grep -v pytest|grep -v pyspark |grep -v psycopg > requirements.txt
docker build -t jaihind213/data_download_grpc:1.0 -t jaihind213/data_download_grpc:latest .
echo `pwd`
