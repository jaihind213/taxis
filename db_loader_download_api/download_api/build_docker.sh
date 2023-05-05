#!/bin/bash
cd "$(dirname "$0")"
#todo: have seperate pyproject.toml for download_api project as parent is polluting deps
#and making docker image large
poetry export --without-hashes --format=requirements.txt|grep -v numpy |grep -v pytest-grpc|grep -v pandas|grep -v pyarrow|grep -v pytest|grep -v pyspark |grep -v psycopg > requirements.txt
docker build -t data_download_grpc/python39-alpine:4.0 -t data_download_grpc/python39-alpine:latest .
echo `pwd`
