import os

import grpc

import download_request_pb2_grpc
import download_request_pb2
import time


def download_folder_files(server_url, destination_path, date, hour):
    with grpc.insecure_channel(server_url) as channel:
        stub = download_request_pb2_grpc.FileServiceStub(channel)
        request = download_request_pb2.DownloadRequest(date=date, hour=hour)
        responses = stub.GetFolderFiles(request)
        for response in responses:
            file_name = response.file_name
            #file_contents = response.file_contents
            with open(destination_path + "/" + file_name.replace("/", "_"), 'ab') as f:
                f.write(response.file_contents)
                    #print(response.file_name)



if __name__ == '__main__':
    download_path = "/tmp/downloaded"
    date = "2022-04-01"
    hour = "00"  # can be blank
    start = time.time()
    download_folder_files('localhost:50051', download_path, date, hour)
    print(time.time() - start)
