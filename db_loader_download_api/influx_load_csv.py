import configparser
import logging
import sys

from pyspark.sql import SparkSession

import psycopg as psycopg3
from psycopg import sql as sql3
import influxdb
from influxdb_client import Point
from influxdb_client.client.write_api import SYNCHRONOUS

def load_data_to_tsdb(table, cols, records, db_properties: dict):
    """
    load to timeseries db
    """
    client = influxdb.InfluxDBClient(host=db_properties.get("host", "localhost"), port=db_properties.get("port", 8086))
    client.set_user_password(db_properties.get("user", "postgres"), db_properties.get("password", "postgres"))
    client.switch_database(db_properties.get("database", "trips"))
    #todo: insert POINT datatype too and use geo-flux package to use points, polygon, mapbox for analysis etc
    try:
       for row in records:
           json_body = [
               {
                   "measurement": table,
                   "time": row[2],
                   "fields": {
                       "trip_seconds": int(row[4]),
                       "trip_miles": float(row[5]),
                       "trip_total": float(row[14]),
                       "trip_id": row[0],
                       "taxi_id": row[1],
                   },
                   "tags": {
                       "payment_type": str(row[15]),
                       "company": str(row[16]),
                       "pickup_community_area": str(row[8]),
                       "dropoff_community_area": str(row[9]),
                   }
               }
           ]
       client.write_points(json_body)
    finally:
        client.close()


def do_casting(col_val, col_name: str):
    if col_val:
        output = col_val
        if col_name == "trip_seconds" or col_name == ":@computed_region_vrxf_vc4k":
            output = int(col_val)
        elif col_name == "trip_miles":
            output = float(col_val)
        elif col_name == "pickup_census_tract" or col_name == "dropoff_census_tract" or col_name == "pickup_community_area" or col_name == "dropoff_community_area":
            output = int(col_val)
        elif col_name == "fare" or col_name == "tips" or col_name == "tolls" or col_name == "extras" or col_name == "trip_total":
            output = float(col_val)
        elif col_name == "pickup_centroid_latitude" or col_name == "pickup_centroid_longitude" or col_name == "dropoff_centroid_latitude" or col_name == "dropoff_centroid_longitude" or col_name == "trip_total":
            output = float(col_val)
        return output
    return col_val


def process_partition(partition, cols, db_properties):
    records = [
        tuple(do_casting(getattr(row, col_name), col_name) for col_name in cols) for row in partition
    ]
    # print(records)
    load_data_to_tsdb(db_properties.get("table", "trips"), cols, records, db_properties)


if __name__ == "__main__":
    arguments = sys.argv[0:]
    config_file = arguments[1]
    date = arguments[2]  # YYYY-MM-DD
    if not date:
        raise ValueError("date not specified in YYYY-MM-DD format ")
    hour = arguments[3]  # HH
    if not hour:
        raise ValueError("hour not specified. (HH) format")

    config = configparser.ConfigParser()
    with open(config_file) as fh:
        config.read_file(fh)

    spark = SparkSession.builder.appName("trips_tsdb_loader").getOrCreate()

    df = spark.read \
        .option("header", "true") \
        .option("inferSchema", "true") \
        .option("compression", "gzip") \
        .csv(config['lake']['lake_url'] + "/" + date + "/" + hour + "/*")

    db_properties = {
        "host": config['influxdb']['host'],
        "port": int(config['influxdb']['port']),
        "table": config['influxdb']['table'],
        "user": config['influxdb']['user'],
        "password": config['influxdb']['password'],
        "database": config['influxdb']['database'],
    }

    # pass tuple to foreachPartition
    cols = tuple(df.columns)
    df.rdd.foreachPartition(lambda partition: process_partition(partition, cols, db_properties))

    spark.stop()
