#!/bin/sh
VER=1.0
cd "$(dirname "$0")"
mvn clean package
docker build -t jaihind213/taxis_puller:$VER -t jaihind213/taxis_puller:latest .

#check for security vulnerabilities
docker scout cves jaihind213/taxis_puller:$VER
