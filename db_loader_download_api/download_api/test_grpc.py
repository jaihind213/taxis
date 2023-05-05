import configparser
import os
from concurrent import futures

import pytest
import grpc
import download_request_pb2
import download_request_pb2_grpc


@pytest.fixture(scope='module')
def grpc_add_to_server():
    from download_request_pb2_grpc import add_FileServiceServicer_to_server

    return add_FileServiceServicer_to_server


@pytest.fixture(scope='module')
def grpc_servicer():
    from grpc_server import FileService
    config = configparser.ConfigParser()
    try:
        with open(os.getcwd() + "/download_api/config_test.ini") as fh:
            config.read_file(fh)
            config['lake']['container'] = os.getcwd() + "/download_api/trips_lake/csv"
    except FileNotFoundError:
        with open(os.getcwd() + "/config_test.ini") as fh:
            config.read_file(fh)
            config['lake']['container'] = os.getcwd() + "/trips_lake"
    return FileService(config)


@pytest.fixture(scope='module')
def grpc_stub_cls(grpc_channel):
    from download_request_pb2_grpc import FileServiceStub
    return FileServiceStub


def test_download_api(grpc_stub):
    print(os.getcwd())
    request = download_request_pb2.DownloadRequest(date='2023-05-02', hour='12')
    responses = grpc_stub.GetFolderFiles(request)
    for response in responses:
        assert "sample_file.txt" in response.file_name
        assert response.file_contents.decode("utf-8") == 'hello world'
