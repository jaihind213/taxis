import os

import pytest
import psycopg as psycopg3
from pyspark.sql.connect.session import SparkSession

from postgres_load_csv import process_partition
from pyspark.sql import SparkSession

db_properties = {
    "user": "postgres",
    "password": "postgres",
    "driver": "org.postgresql.Driver",
    "host": "localhost",
    "port": 5432,
    "table": "trips",
    "database": "postgres",
}

@pytest.fixture
def conn():
    # Set up a connection to the PostgreSQL database
    conn = psycopg3.connect(
        host=db_properties["host"],
        port=db_properties["port"],
        dbname=db_properties["database"],
        user=db_properties["user"],
        password=db_properties["password"],
    )
    yield conn
    # Close the connection to the database
    conn.close()


@pytest.fixture(scope="session")
def spark_session(request):
    """Fixture for creating a SparkSession in local mode for Pytest."""
    spark = SparkSession.builder \
        .appName("pytest_spark_local") \
        .master("local") \
        .getOrCreate()
    request.addfinalizer(lambda: spark.stop())
    return spark

def test_data_loaded_into_db(conn, spark_session):
    truncate = "truncate table public.trips;"

    # Execute the query and fetch the result
    cursor = conn.cursor()
    cursor.execute(truncate)
    conn.commit()
    cursor.close()

    df = spark_session.read \
        .option("header", "true") \
        .option("inferSchema", "true") \
        .option("compression", "gzip") \
        .csv(os.getcwd() + "/download_api/trips_lake/sample.csv")

    # pass tuple to foreachPartition
    cols = tuple(df.columns)
    df.rdd.foreachPartition(lambda partition: process_partition(partition, cols, db_properties))


    query = "SELECT count(*) FROM public.trips"

    # Execute the query and fetch the result
    cursor = conn.cursor()
    cursor.execute(query)
    result = cursor.fetchall()

    # Assert that the count of the result is equal to the expected count
    expected_count = 2
    assert result[0][0] == expected_count
