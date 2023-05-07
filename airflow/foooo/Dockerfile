from apache/airflow:2.6.0-python3.9
USER 0
RUN apt-get update && apt-get install wget
RUN  wget https://archive.apache.org/dist/spark/spark-3.1.2/spark-3.1.2-bin-hadoop3.2.tgz -O /tmp/spark-3.1.2-bin-hadoop3.2.tgz 
RUN ls -lah /tmp
CMD ["/bin/bash"]
