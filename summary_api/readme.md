# Summary Api(s)

## Description

As per the assignment requirements, we need an Api to get trip summaries and statistics.

This project houses the following:

*  Api to query trip summaries
*  Api to query subset of data.

The above is implemented in python using HTTP protocol.

## Requirements
* mamba (https://mamba.readthedocs.io/en/latest/installation.html)
* docker

## Setup
```
mamba create -n summary_api python=3.9 poetry
mamba activate summary_api
pip3 install six
poetry install

#start docker postgres gis
#make sure to run batch pull , start postgres and run db loader jobs

```

### How to run & API docs?
```
mamba activate summary_api
python server.py config.ini
#access  http://localhost:8000/docs FOR API DOCS
#OR
#1. start postgres docker. 
#2. make sure to run taxi pull job, loader jobs
#3. modify config.ini, set postgres host to IP of your machine not localhost.
```



### Docker image

```
sh build_docker.sh
```
````
## Tests``
``
```
#start docker postgres gis as shown in set
mamba activate summary_api
poetry install
poetry run pytest
```

