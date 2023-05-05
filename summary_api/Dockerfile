# Use an official Python runtime as a parent image
FROM python:3.9-alpine

USER 0
RUN mkdir /app
WORKDIR /app
RUN mkdir /app/conf

# Copy the current directory contents into the container at /app
COPY . /app/
COPY config.ini /app/conf/config.ini

# Install any needed packages specified in requirements.txt
RUN apk add --virtual .make-deps musl-dev g++ libpq-dev libffi-dev && pip install --no-cache-dir -r requirements.txt && apk del g++

# Make port 80 available to the world outside this container
EXPOSE 8000

# Run app.py when the container launches
CMD ["python", "/app/server.py", "/app/conf/config.ini"]
