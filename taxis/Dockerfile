FROM openjdk:8-alpine

WORKDIR /app
RUN mkdir /app/conf
RUN mkdir -p /tmp/staging/csv
RUN mkdir -p /tmp/trips_bucket/csv

COPY ./target/taxi_puller.jar .
COPY ./config.properties ./conf/

ENTRYPOINT ["java", "-cp", "taxi_puller.jar", "com.creativefab.ingest.BatchTaxiTripPuller"]

#config/date,hour
CMD ["/app/conf/config.properties", "2022-04-01", "00"]

#mount your volume to /tmp/trips_bucket