from apache/airflow:2.6.0-python3.9
USER 0
#RUN apt-get update && apt-get install -y  build-essential python-dev 
#USER airflow

#RUN pip3 install --upgrade pip

#RUN pip3 install "apache-airflow==2.2.3" --constraint "https://raw.githubusercontent.com/apache/airflow/constraints-2.2.3/constraints-no-providers-3.9.txt"
#RUN pip3 install apache-airflow==1.10.10 
#USER 0

RUN apt update && \
    apt-get install -y openjdk-11-jdk && \
    apt-get install -y ant wget && \
    apt-get clean;
ENV JAVA_HOME /usr/lib/jvm/java-11-openjdk-arm64
RUN export JAVA_HOME

RUN wget https://archive.apache.org/dist/spark/spark-3.1.2/spark-3.1.2-bin-hadoop3.2.tgz -O /tmp/spark-3.1.2-bin-hadoop3.2.tgz  
RUN tar -xvf /tmp/spark-3.1.2-bin-hadoop3.2.tgz -C /tmp
#RUN tar -xvfz /tmp/spark-3.1.2-bin-hadoop3.2.tgz
ENV SPARK_HOME /tmp/spark-3.1.2-bin-hadoop3.2
RUN export SPARK_HOME
RUN chmod -R 777 $SPARK_HOME

USER airflow
RUN pip install psycopg
RUN pip install pyspark==3.1.3
RUN pip install influxdb influxdb_client

#RUN wget https://archive.apache.org/dist/spark/spark-3.1.2/spark-3.1.2-bin-hadoop3.2.tgz -O /tmp/spark-3.1.2-bin-hadoop3.2.tgz && tar -xvf /tmp/spark-3.1.2-bin-hadoop3.2.tgz
#RUN tar -xvfz /tmp/spark-3.1.2-bin-hadoop3.2.tgz
#ENV SPARK_HOME /tmp/spark-3.1.2-bin-hadoop3.2
#RUN export SPARK_HOME

RUN mkdir -p /tmp/staging/csv
RUN mkdir -p /tmp/trips_bucket/csv

COPY taxi_puller.jar /tmp/ 
COPY config.properties /tmp/ 
COPY postgres_load_csv.py /tmp/
copy influx_load_csv.py /tmp/
copy postgresql-42.6.0.jar /tmp/
#copy config.ini /tmp/config.ini
ENV PYTHONPATH /tmp/
RUN export PYTHONPATH 
