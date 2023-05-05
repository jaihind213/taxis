from typing import List

from fastapi import APIRouter, Depends, Response, Path, Query, Body
# from server import read_config, get_db_pool, app
from config import get_config
from typing import Annotated

import summary as smry
import json
import db_pool

router = APIRouter()


@router.get("/hi")
async def hi(config=Depends(get_config)):
    return f"hi there: serving you on port{config['serve']['port']}"


@router.get("/trips/summary")
async def get_summary(response: Response, company: str = Query(default="all", title="Query string"),
                      db_pool=Depends(db_pool.get_db_pool),
                      start_time: str = Query(None, title="Query string", example='2022-04-01 08:00:00+0800'),
                      end_time: str = Query(None, title="Query string", example='2022-04-01 09:00:00+0800'),
                      ):
    """
    get summary statistics for taxis from time t1 to t2
    ex(url decoded): http://localhost:8000/v1/trips/summary?start_time=2016-01-01 08:15:00+0800&end_time=2016-01-01 08:30:00+0800

    :param response:
    :param start_time: default: 2016-01-01 08:15:00+0800
    :param end_time: ex: 2016-01-01 08:30:00+0800
    :param company: if empty then we fetch for all companies
    :param db_pool: its injected
    :return: json [] of summary per company

    Note: while using curl please encode url properly:
    ex: http://localhost:8000/v1/trips/summary?start_time=2016-01-01%2008:15:00%2B0800&end_time=2016-01-01%2008:30:00%2B0800
    ex: http://localhost:8000/v1/trips/summary?start_time=2016-01-01%2008:15:00%2B0800&end_time=2016-01-01%2008:30:00%2B0800&company=Flash%20Cab
    """
    err = validate_time_range(start_time, end_time)
    if err:
        return err

    try:
        connection = db_pool.getconn()
        cursor = connection.cursor()

        per_company_query = """
            SELECT company,
                   COUNT(trip_id) AS num_trips,
                   count(distinct(taxi_id)) as uniq_taxis_seen,
                   avg(trip_seconds) as avg_trip_time,
                   sum(trip_total) as earning,
                   avg(trip_total) as avg_trip_fare
            FROM public.trips
            WHERE trip_start_timestamp BETWEEN '{start_time}' AND '{end_time}' {company_filter}
            GROUP BY company;
        """
        company_filter = "" if company == '' or company == 'all' else "and company='" + company + "'"
        query = per_company_query.format(start_time=start_time, end_time=end_time, company_filter=company_filter)
        print(query)
        cursor.execute(query)
        rows = cursor.fetchall()
        summaries = []
        for row in rows:
            company, num_trips, uniq_taxis_seen, avg_trip_time, earning, avg_trip_fare = row
            summary = smry.Summary(company)
            summary.num_trips = num_trips
            summary.uniq_taxis_seen = uniq_taxis_seen
            summary.avg_trip_time = avg_trip_time
            summary.earning = earning
            summary.avg_trip_fare = avg_trip_fare
            summaries.append(summary)
        response.status_code = 200
    except Exception as e:
        print("Error in querying data", e)
        response.status_code = 500
        raise e
    finally:
        db_pool.putconn(connection)
        # return the connection to the pool
    return json.dumps([smy.toJSON() for smy in summaries], default=str)


@router.get("/trips/{to_from}/community_area/{community_area}")
async def get_num_trips(community_area: int, response: Response, company: str = Query(default="all", title="Query string"),
                        to_from="to", db_pool=Depends(db_pool.get_db_pool),
                        start_time: str = Query(None, title="Query string", example='2022-04-01 08:00:00+0800'),
                        end_time: str = Query(None, title="Query string", example='2022-04-01 09:00:00+0800'),
                        ):
    """
    get num trips to/from community_area
    :param start_time: ex: 2016-01-01 08:15:00+0800
    :param end_time:   ex: 2016-01-01 08:22:00+0800
    :param to_from:  either 'to' or 'from'
    :param community_area: ex: 28 refer https://en.wikipedia.org/wiki/Community_areas_in_Chicago
    :param company: if blank we get for all companies
    :param db_pool: injected into query
    :return: num trips to/from area
    Note: while using curl please encode url properly:
    ex: http://localhost:8000/v1/trips/to/community_area/28?start_time=2016-01-01%2008:15:00%2B0800&end_time=2016-01-01%2008:30:00%2B0800&company=Flash%20Cab
    """
    err = validate_time_range(start_time, end_time)
    if err:
        return err
    if not to_from or to_from == "":
        return Response(content="invalid to_from path paramater. it is either to or from", status_code=400)
    if not community_area:
        return Response(content="invalid community_area path paramater. it is an integer.", status_code=400)
    connection = db_pool.getconn()
    try:
        cursor = connection.cursor()
        per_company_query = """
            SELECT company, {area_type_column}, COUNT(trip_id) AS num_trips
            FROM public.trips
            WHERE trip_start_timestamp BETWEEN '{start_time}' AND '{end_time}'
            and {area_type_column} = {community_area} {company_filter}
            GROUP BY company, {area_type_column};
        """
        company_filter = "" if company == '' or company == 'all' else "and company='" + company + "'"
        area_type_column = "dropoff_community_area" if to_from == 'to' else 'pickup_community_area'
        query = per_company_query.format(start_time=start_time, end_time=end_time,
                                         company_filter=company_filter,
                                         community_area=community_area,
                                         area_type_column=area_type_column)
        print(query)
        cursor.execute(query)
        rows = cursor.fetchall()
        summaries = []
        for row in rows:
            company, area, num_trips = row
            summary = smry.AreaSummary(company, area, num_trips, to_from)
            summaries.append(summary)
    except Exception as e:
        print("Error in querying data", e)
        response.status_code = 500
        raise e
    finally:
        db_pool.putconn(connection)
        # return the connection to the pool
    return summaries


@router.post("/trips/subset")
def get_trips(
        #filter: smry.Filter,
        filter: Annotated[smry.Filter, Body(example={"where_clause": "company = '\''Flash Cab'\''"})],
        response: Response, page_num: int = Query(default=1, title="Query int"),
        page_size: int = Query(default=1000, title="Query int"),
        db_pool=Depends(db_pool.get_db_pool)):
    """
    get subset of data with mandatory where clause as select * is dangerous to db.
    :param filter: ex: its a json= {"where_clause" : "age>20"}
    :param response:
    :param page_num: integer  from 1 onwards
    :param page_size: integer - defaults to 1000
    :param db_pool: injected
    :return: lsit of rows
    ex: curl -X POST "http://localhost:8000/v1/trips/subset?page_num=1&page_size=10" -H "accept: application/json" -H "Content-Type: application/json" -d '{"where_clause": "company = '\''Flash Cab'\''"}'
    """
    if filter == "":
        response.status_code = 400
        response.body = "must provide filter provided."
        return

    conn = db_pool.getconn()
    try:
        cursor = conn.cursor()

        offset = (page_num - 1) * page_size
        query_str = f"SELECT * FROM public.trips WHERE {filter.where_clause} order by trip_start_timestamp desc LIMIT {page_size} OFFSET {offset};"
        cursor.execute(query_str)
        rows = cursor.fetchall()
        trips = []
        for row in rows:
            trips.append(row)
        return trips
    except Exception as e:
        print("Error in selecting subset", e)
        response.status_code = 500
        raise e
    finally:
        try:
            cursor.close()
        except:
            pass
        db_pool.putconn(conn)


def validate_time_range(start_time, end_time) :
    if not start_time or not end_time or start_time == "" or end_time == "":
        return Response(content="start/end time are required / invalid", media_type="text/plain", status_code= 400)
    return None
