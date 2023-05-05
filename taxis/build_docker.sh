#!/bin/sh
VER=1.0
cd "$(dirname "$0")"
mvn clean package
docker build -t taxis/puller:$VER -t taxis/puller:latest .

#check for security vulnerabilities
docker scout cves taxis/puller:$VER
