# Taxi Batch Data puller

## Description

As per the assignment requirements, we pull taxi data from the chicago web portal

Here we have implemented

*  a java app/job to download the data from the portal
*  The java app will take 2 main arguments - date in YYYY-MM-DD and hour in HH format
*  => it will download data for the specified hour.
*  Can i download for 15/30 minute buckets with in the hour ? yes, set 'TIME_BUCKET_TYPE' to FIFTEEN_MIN/HALF_HOURLY
* The app downloads data from the portal, zips it and uploads to a trips lake. The lake can be configured to be s3/azure/local file system.

## Capabilities

* Download csv/json data based on configuration. (set SRC_API_URL in config either https://<>.csv or .json)
* download data for specific time window YYYY-MM-DD/HH/xx where xx is half_hourly/15min/30min bucket
* upload to configurable lake like s3/azure/local file system etc

## Requirements
* java 8
* docker

## Setup
```
mvn clean compile
```

## Tests

```
mvn clean compile test
```

## How to run ?

```
mvn clean package
mkdir /tmp/staging
mkdir -p /tmp/trips_bucket/csv
curr_dir=`pwd`
java -cp target/taxi_puller.jar com.creativefab.ingest.BatchTaxiTripPuller $curr_dir/config.properties 2022-04-01 00
#or
#build & use docker (see demo below)
```

## Docker build  / Scan Vulnerability /Demo

### build
```
sh build_docker.sh
```

### Scan Vulnerability
```
docker scout cves taxis/puller:latest
```

### Demo
we download data and upload to our lake (configured to local filesystem at /tmp/trips_bucket/csv)
```
mkdir /tmp/staging
mkdir -p /tmp/trips_bucket/csv
rm -rf /tmp/staging/*
rm -rf /tmp/trips_bucket/csv/*
sh build_docker.sh
curr_dir=`pwd`
docker run -v $curr_dir/config.properties:/app/conf/config.properties -v /tmp/staging/csv:/tmp/staging/csv -v /tmp/trips_bucket/csv:/tmp/trips_bucket/csv  taxis/puller:latest
echo "data has arrived:....."
ls -lah /tmp/trips_bucket/csv/2022-04-01/00/
```

