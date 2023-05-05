import pytest
from fastapi.testclient import TestClient
from datetime import datetime, timedelta
import os

import json

import api
import config
import configparser

import db_pool
import server
from fastapi import APIRouter, Depends, Response


@pytest.fixture(scope="module")
def test_client():
    config.setup_app_config(os.getcwd() + "/default.ini")
    db_pool.create_pool(config.get_config())
    server.app.include_router(api.router, prefix="/v1",
                              dependencies=[Depends(config.get_config), Depends(db_pool.get_db_pool)])
    client = TestClient(server.app)
    yield client


def test_hi(test_client):
    response = test_client.get("/v1/hi")
    assert response.status_code == 200


def test_get_summary(test_client):
    test_db_pool = db_pool.get_db_pool()
    # Create a test trip
    connection = test_db_pool.getconn()
    cursor = connection.cursor()
    cursor.execute("truncate table public.trips")
    sql = """INSERT INTO public.trips (trip_id, trip_total ,company, taxi_id, trip_start_timestamp, dropoff_community_area, pickup_community_area)
                 VALUES(%s, %s, %s, %s, %s, %s, %s) ;"""
    time = datetime.utcnow()
    cursor.execute(sql, (1, 10, "Flash Cab", "taxi1", time, 28, 29))
    cursor.execute(sql, (2, 20, "Flash Cab", "taxi2", time, 28, 29))
    cursor.execute(sql, (21, 30, "Flash Cab", "taxi1", time, 28, 29))

    connection.commit()

    # Test the function
    start_time = datetime.utcnow() - timedelta(minutes=30)
    end_time = datetime.utcnow() + timedelta(minutes=30)
    response = test_client.get(
        f"/v1/trips/summary/?start_time={start_time.isoformat()}&end_time={end_time.isoformat()}&company=Flash Cab", )

    assert response.status_code == 200
    json_map = json.loads(response.json())
    assert json_map[0]['num_trips'] == 3
    assert json_map[0]['uniq_taxis_seen'] == 2
    assert json_map[0]['total_earning'] == 60
    assert json_map[0]['total_earning'] == 60
    assert json_map[0]['avg_trip_fare'] == 20


def test_get_num_trips_to_area(test_client):
    test_db_pool = db_pool.get_db_pool()
    # Create a test trip
    connection = test_db_pool.getconn()
    cursor = connection.cursor()
    cursor.execute("delete from public.trips where company = 'test_cab'")
    sql = """INSERT INTO public.trips (trip_id, company, taxi_id, trip_start_timestamp, dropoff_community_area, pickup_community_area)
                 VALUES(%s, %s, %s, %s, %s, %s) ;"""
    time = datetime.utcnow()
    cursor.execute(sql, (1, "test_cab", "taxi1", time, 28, 29))
    cursor.execute(sql, (2, "test_cab", "taxi1", time, 28, 29))

    connection.commit()

    # Test the function
    start_time = datetime.utcnow() - timedelta(minutes=30)
    end_time = datetime.utcnow() + timedelta(minutes=30)
    response = test_client.get(
        f"/v1/trips/to/community_area/28?start_time={start_time.isoformat()}&end_time={end_time.isoformat()}&company=Flash Cab", )

    assert response.status_code == 200
    assert response.json()[0]['num_trips'] == 2
