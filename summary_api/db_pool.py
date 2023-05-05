from psycopg2 import pool

conn_pool = None


def create_pool(config):
    global conn_pool
    conn_pool = pool.SimpleConnectionPool(
        config['postgres']['min_connections'],  # min number of connections
        config['postgres']['max_connections'],  # max number of connections
        user=config['postgres']['user'],
        password=config['postgres']['password'],
        host=config['postgres']['host'],
        port=int(config['postgres']['port']),
        database=config['postgres']['database'],
    )


def get_db_pool():
    return conn_pool
