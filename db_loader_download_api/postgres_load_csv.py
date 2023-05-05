import configparser
import logging
import sys

from pyspark.sql import SparkSession

import psycopg as psycopg3
from psycopg import sql as sql3

def load_data_to_postgres(table, cols, records, db_properties: dict):
    conn = psycopg3.connect(
        host=db_properties.get("host", "localhost"),
        port=db_properties.get("port", 5432),
        dbname=db_properties.get("database", "postgres"),
        user=db_properties.get("user", "please_set_user"),
        password=db_properties.get("password", "please_set_db_pass"),
    )

    copy_query_obj = sql3.SQL(
        """
        COPY {table} ({columns}) FROM STDIN
        """
    ).format(
        table=sql3.Identifier(table),
        columns=sql3.SQL(", ").join(map(sql3.Identifier, list(cols))),
    )
    copy_query = copy_query_obj.as_string(conn)
    logging.info(f"[SparkUpsertHasMarkerFieldDf]: The copy query is {copy_query}")

    curr = conn.cursor()
    try:
        with curr.copy(copy_query) as copy:
            for record in records:
                copy.write_row(record)
        conn.commit()
    finally:
        try:
            curr.close()
        except:
            pass
        conn.close()


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
    load_data_to_postgres(db_properties.get("table", "trips"), cols, records, db_properties)


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

    spark = SparkSession.builder.appName("trips_db_loader").getOrCreate()

    df = spark.read \
        .option("header", "true") \
        .option("inferSchema", "true") \
        .option("compression", "gzip") \
        .csv(config['lake']['lake_url'] + "/" + date + "/" + hour + "/*")

    db_properties = {
        "user": config['postgres']['user'],
        "password": config['postgres']['password'],
        "driver": "org.postgresql.Driver",
        "host": config['postgres']['host'],
        "port": int(config['postgres']['port']),
        "table": config['postgres']['table'],
        "database": config['postgres']['database'],
    }

    # pass tuple to foreachPartition
    cols = tuple(df.columns)
    df.rdd.foreachPartition(lambda partition: process_partition(partition, cols, db_properties))

    spark.stop()
