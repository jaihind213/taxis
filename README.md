# Taxis project

Kindly refer to this google doc for details:

https://docs.google.com/document/d/1n5mfQs5PvCbaZUr6vTGI6o70-aet5w3QOAZZ6uj11VA/edit#

other links:

ARCH diagram: 

https://excalidraw.com/#json=TNoRNJAtdeok8OG2hDlc5,AM_cVUHYZRFi9SftNxKtpQ

DEMO:
cd airflow;
set HOST_IP in demo.sh
sh demo.sh
#once airflow starts
#go to browser http://localhost:8080
#login airflow/airflow
#trigger the dag

#then open http://localhost:8000/docs
#try out api - v1/trips/{to_from}/community_area/{community_area}
# set community_area = 8
# to_from = from
# company = all
# start_time = 2022-04-01 16:45:00.000 +0800
# end_time = 2022-04-01 16:50:00.000 +0800

#walah u get results

DEMO video:
(just incase u face issues)
