#!/bin/bash
cd "$(dirname "$0")"
poetry export --without-hashes --format=requirements.txt > requirements.txt
docker build -t jaihind213/summary-api:1.0 -t jaihind213/summary-api:latest .
echo `pwd`
