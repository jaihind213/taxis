import configparser

app_config = configparser.SafeConfigParser()


def setup_app_config(default_ini):
    with open(default_ini) as fh:
        app_config.read_file(fh)


def get_config():
    return app_config
