import configparser
import logging
import os
import sys
from contextlib import asynccontextmanager

import api
from fastapi import FastAPI, Depends

import config
import db_pool
from config import app_config


# def read_config():
#     return app_config

# def get_db_pool():
#     return db_pool.conn_pool

@asynccontextmanager
async def shutdown_hook(app: FastAPI):
    yield
    # cleanup now
    db_pool.conn_pool.closeall()


app = FastAPI(lifespan=shutdown_hook)

if __name__ == "__main__":
    arguments = sys.argv[0:]
    config_file = arguments[1]

    # dep setup
    config.setup_app_config(os.getcwd() + "/default.ini")
    config.setup_app_config(config_file)
    db_pool.create_pool(config.get_config())

    logging.basicConfig(level=logging._nameToLevel[(config.get_config()['logging']['level']).upper()])

    # start server
    import uvicorn

    app.include_router(api.router, prefix="/v1",
                       dependencies=[Depends(config.get_config), Depends(db_pool.get_db_pool)])
    uvicorn.run(app, host=app_config['serve']['host'], port=int(app_config['serve']['port']),
                log_level=app_config['logging']['level'])
