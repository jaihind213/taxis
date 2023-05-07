# Use an official Python runtime as a parent image
FROM python:3.9-alpine

USER 0
RUN mkdir /app
WORKDIR /app
RUN mkdir /app/conf
RUN mkdir  /app/lake


# Copy the current directory contents into the container at /app
COPY . /app/
#in prod, dont copy the trips lake folder. its for demo
#COPY trips_lake/ /app/lake/
COPY config_docker.ini /app/conf/config.ini

# Install any needed packages specified in requirements.txt
RUN apk add --virtual .make-deps musl-dev g++ libffi-dev && pip install --no-cache-dir -r requirements.txt && apk del g++

# Make port 80 available to the world outside this container
EXPOSE 50051

# Run app.py when the container launches
CMD ["python", "/app/grpc_server.py", "/app/conf/config.ini"]
