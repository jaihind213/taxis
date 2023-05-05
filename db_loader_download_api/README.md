# Data Retrival Api & Db loader

## Description

As per the assignment requirements, we need an Api to retrieve trips data sets which are fetched
from chicago trips portal. We also need an api to connect to the db to get summary statistics

This project houses 2 components:

*  Data download /Retrieval Api - exposed via GRPC protocol
*  Spark job to load CSV to postgres(GIS) / timeseries NoSQL geo spatial store - influx db

The above is implemented in python.

## Requirements
* mamba (https://mamba.readthedocs.io/en/latest/installation.html)
* since the download/Retrieval api is implemented via GRPC (u need grpc client to talk to the api)
  * You can refer to sample python code in $PROJECT_DIR/grpc_client.py
* docker
* spark installation - spark-3.1.2-bin-hadoop3.2

## Setup
```
mamba create -n db_loader_download_api python=3.9 poetry
mamba activate db_loader_download_api
pip3 install six
poetry install

#start docker postgres gis
docker run --name postgis -e POSTGRES_USER=postgres -e POSTGRES_PASSWORD=postgres -p 5432:5432 -d postgis/postgis:15-3.3
sleep 5
#run DDL
docker cp trips.sql postgis:/tmp/ 
docker exec -it postgis psql -U postgres -d postgres -f /tmp/trips.sql

#run influx db
docker run --name influx -d -p 8086:8086  -e DOCKER_INFLUXDB_INIT_MODE=setup -e DOCKER_INFLUXDB_INIT_USERNAME=posgres -e DOCKER_INFLUXDB_INIT_PASSWORD=postgres -e DOCKER_INFLUXDB_INIT_ORG=trips -e DOCKER_INFLUXDB_INIT_BUCKET=trips influxdb:1.0
curl -XPOST "http://localhost:8086/query" --data-urlencode "q=CREATE DATABASE trips"


cd download_api
python -m grpc_tools.protoc -I. --python_out=. --grpc_python_out=. download_request.proto

```

## Csv loader to sql/nosql db store

The spark job which loads Gzipped CSV files and loads to postgres(gis)/ influx db.
Number of connections opened while loading = num of executor_cores x num of executors

### Running demo for loaders ?

We run the spark job by providing 3 arguments
1. path to config.ini
2. Date in YYYY-MM-DD
3. hour in HH

```
working_dir=`pwd` 

#start docker postgres gis/influx db as shown in setup
 
#configure postgres/influx db section in config.ini
$SPARK_HOME/bin/spark-submit --driver-memory 1g  --master "local[*]"  --jars $working_dir/postgresql-42.6.0.jar postgres_load_csv.py $working_dir/config.ini 2022-04-01 00
export OBJC_DISABLE_INITIALIZE_FORK_SAFETY=YES
$SPARK_HOME/bin/spark-submit --driver-memory 1g  --master "local[*]"  influx_load_csv.py $working_dir/config.ini 2022-04-01 00

#see results for postgres
psql -U postgres -h localhost -d postgres -c "SELECT count(*) from public.trips;"
#password is postgres
## influx db
curl -XPOST "http://localhost:8086/query" --data-urlencode "db=trips" --data-urlencode "q=select count(*) from trips"
```

## Data Retrieval Api

### Why GRPC api for data retrieval?

Since this is a data download api & I assume large volumes of data will be retrieved, 
In general, it is said that, GRPC is more efficient for the transfer of data when compared to http. 

however i did a benchmark: the results were surprising:

for a 2.5 GB csv file zipped to 883MB

| protocol | language | read buffersize | time sec |
|----------|----------|-----------------|----------|
| HTTP     | java     | 1k              | 2        |
| HTTP     | python   | 1k              | 2        |
| grpc     | python   | 1k              | 42       |
| HTTP     | java     | 10k             | 2        |
| HTTP     | python   | 10k             | 2        |
| grpc     | python   | 10k             | 5        |
| HTTP     | java     | 1024k           | <1       |
| HTTP     | python   | 1024k           | <1       |
| grpc     | python   | 1024k           | <1       |

So it seems like grpc was same as http? have to do more research. its a learning.

### Docker image for Grpc Api server 

```
cd download_api
sh build_docker.sh
cd -
```

### Docker vulnerability scan

```
docker scout cves data_download_grpc/python39-alpine:latest
```
### Demo Run docker Grpc Api server  & run client

```
#make sure to run the batch pull job to get some data into the lake
cd download_api
mkdir /tmp/downloaded
rm -rf /tmp/downloaded/*
mamba activate db_loader_download_api
docker run --name grpc -p 50051:50051 -v /tmp/trips_bucket:/app/lake -d data_download_grpc/python39-alpine:4.0
sleep 5
python grpc_client.py
#check /tmp/downloaded/ directory
ls -lah /tmp/downloaded/
cd -
```
## Tests

```
#start docker postgres gis as shown in set
mamba activate db_loader_download_api
poetry install
export OBJC_DISABLE_INITIALIZE_FORK_SAFETY=YES
poetry run pytest
```

