from libcloud.storage.types import Provider
from libcloud.storage.providers import get_driver

class LakeStorage:
    def __init__(self, config):
        storage_type = config['lake']['storage_type']
        container_from_conf = config['lake']['container']
        self.lake_driver = None
        self.storage_type = storage_type
        self.container = container_from_conf

        if storage_type == Provider.LOCAL:
            cls = get_driver(self.storage_type)
            self.lake_driver = cls(key=container_from_conf)
            self.container = ""
        elif storage_type == "s3":
            cls = get_driver(storage_type)
            self.lake_driver = cls(config['s3']['access_key'], config['s3']['secret_key'])
        else:
            raise Exception(f"Sorry, lake type {storage_type} not supored yet")

    def get_connector(self):
        return self.lake_driver

    def close(self):
        del self.lake_driver

    @staticmethod
    def get_lib_cloud_provider(url: str) -> str:
        if url.startswith("file://"):
            return "local"
        return url[0: url.index(':')]
