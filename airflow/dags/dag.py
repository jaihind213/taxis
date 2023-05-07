from datetime import datetime
from airflow import DAG
from airflow.operators.bash_operator import BashOperator

default_args = {
    'owner': 'airflow',
    'start_date': datetime(2023, 5, 6)
}

dag = DAG('taxi_dag', default_args=default_args, schedule_interval=None)

java_task = BashOperator(
    task_id='data_puller_hr_bucket',
    bash_command='java -cp /tmp/taxi_puller.jar com.creativefab.ingest.BatchTaxiTripPuller /tmp/config.properties 2022-04-01 00',
    dag=dag
)

spark_postgres_task = BashOperator(
    task_id='data_load_postgres',
    #bash_command='$SPARK_HOME/bin/spark-submit --driver-memory 128m  --master "local[*]"  --jars /tmp/postgresql-42.6.0.jar /tmp/postgres_load_csv.py /tmp/config.ini 2022-04-01 00',
    bash_command='python /tmp/postgres_load_csv.py /tmp/config.ini 2022-04-01 00',
    #bash_command='ls -lah  /usr/lib/jvm/java-11-openjdk-arm64 ; ls -lah $JAVA_HOME/bin', 
    dag=dag
)

spark_influx_task = BashOperator(
    task_id='data_load_influx',
    bash_command='python /tmp/influx_load_csv.py /tmp/config.ini 2022-04-01 00',
    dag=dag
)


java_task >> spark_postgres_task >> spark_influx_task
