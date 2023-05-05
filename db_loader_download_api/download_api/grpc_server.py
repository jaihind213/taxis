import configparser
import logging
import os
import sys
import time

import grpc
from concurrent import futures
import string

import download_request_pb2
import download_request_pb2_grpc
import lake


class FileService(download_request_pb2_grpc.FileServiceServicer):
    """
    file download service in grpc
    """
    def __init__(self, config: list[str]):
        self.chunk_size = int(config['serve']['chunk_size'])
        self.lake_storage = lake.LakeStorage(config)
        self.container_path = self.lake_storage.container

    def prepare_src_path(self, date : string, hour : string, data_format="csv") -> string:
        if not hour:
            #return "/".join([self.container_path, date])
            return "/".join([data_format, date])
        else:
            return "/".join([data_format, date, hour])

    def close(self):
        self.lake_storage.close()

    def GetFolderFiles(self, request, context):
        """
        returns data in files present in the specified date/hour time bucket in the trips lake

        :param request:
        :type DownloadRequest: refer proto py files

        :return: DownloadResponse which is data for multiple files
        """
        start = time.time()
        lake_connector = self.lake_storage.lake_driver
        date_bucket = request.date
        hour_bucket = request.hour
        data_format = request.data_format
        if not data_format or data_format == "":
            data_format = "csv"
        logging.info(f'got request data_format: {data_format}, date: {date_bucket}, hour: {hour_bucket}')
        path = self.prepare_src_path(date_bucket, hour_bucket, data_format)
        try:
            files = lake_connector.list_container_objects(lake_connector.get_container(self.lake_storage.container), path)
            logging.info(f'got files: {files}')
            for file in files:
                for chunk in file.as_stream(chunk_size=self.chunk_size):
                    if not chunk:
                        break
                    logging.debug(file.name, len(chunk))
                    yield download_request_pb2.DownloadResponse(file_name=file.name, file_contents=chunk)
        except Exception as e:
            context.set_details(str(e))
            context.set_code(grpc.StatusCode.INTERNAL)
        end = time.time()
        logging.info(f'completed request for date: {date_bucket}, hour: {hour_bucket} in {end-start} sec')

    def GetFolderFiles2(self, request, context):
        """
        returns data in files present in the specified date/hour time bucket in the trips lake

        :param request:
        :type DownloadRequest: refer proto py files

        :return: DownloadResponse which is data for multiple files
        """
        start = time.time()
        lake_connector = self.lake_storage.lake_driver
        date_bucket = request.date
        hour_bucket = request.hour
        logging.info(f'got request date: {date_bucket}, hour: {hour_bucket}')
        path = "/Users/vishnuch/work/gitcode/taxis/db_loader_download_api/download_api/trips_lake/" + self.prepare_src_path(date_bucket, hour_bucket)
        try:
            for filename in os.listdir(path):
                file_path = os.path.join(path, filename)
                if os.path.isfile(file_path):
                    with open(file_path, 'rb') as f:
                        bytes_read = f.read(self.chunk_size)
                        while bytes_read:
                            yield download_request_pb2.DownloadResponse(file_name=filename, file_contents=bytes_read)
                            bytes_read = f.read(self.chunk_size)

        except Exception as e:
            context.set_details(str(e))
            context.set_code(grpc.StatusCode.INTERNAL)
        end = time.time()
        logging.info(f'completed request for date: {date_bucket}, hour: {hour_bucket} in {end-start} sec')


def get_port_and_num_workers(config) -> [int, int]:
    port = config['serve']['port']
    if port is None:
        port = 50051
    else:
        port = int(port)
    max_workers = config['serve']['max_workers']
    if max_workers is None:
        max_workers = 10
    else:
        max_workers = int(max_workers)
    return port, max_workers

def serve(config: list[str]):
    """
    starts the server on port, which serves download data requests for certain date/hour
    """
    port, max_workers = get_port_and_num_workers(config)
    logging.info(f'serving on port: {port}, num_workers = {max_workers}')

    server = grpc.server(futures.ThreadPoolExecutor(max_workers=max_workers))
    download_request_pb2_grpc.add_FileServiceServicer_to_server(FileService(config), server)
    server.add_insecure_port('[::]:' + str(port))
    #server.add_insecure_port('0.0.0.0:' + str(port))
    server.start()
    server.wait_for_termination()
    #on shutdown hook close file service todo


if __name__ == '__main__':
    arguments = sys.argv[0:]
    config_file = arguments[1]
    config = configparser.ConfigParser()
    with open(config_file) as fh:
        config.read_file(fh)
    logging.basicConfig(level=logging._nameToLevel[config['logging']['level']])
    logging.info(f'lake type:{config["lake"]["storage_type"]}, lake container: {config["lake"]["container"]}')
    serve(config)
