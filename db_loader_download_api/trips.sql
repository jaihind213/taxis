CREATE TABLE trips (
	trip_id TEXT NOT NULL,
	taxi_id TEXT NOT NULL,
	trip_start_timestamp TIMESTAMP,
	trip_end_timestamp TIMESTAMP,
	trip_seconds INTEGER,
	trip_miles FLOAT(2),
	pickup_census_tract bigint,
	dropoff_census_tract bigint,
	pickup_community_area bigint,
	dropoff_community_area bigint,
	fare DOUBLE PRECISION,
	tips DOUBLE PRECISION,
	tolls DOUBLE PRECISION,
	extras DOUBLE PRECISION,
	trip_total DOUBLE PRECISION,
	payment_type VARCHAR(30),
	company VARCHAR(100) NOT NULL,
	pickup_centroid_latitude DOUBLE PRECISION,
	pickup_centroid_longitude DOUBLE PRECISION,
	pickup_centroid_location GEOMETRY,
	dropoff_centroid_latitude DOUBLE PRECISION,
	dropoff_centroid_longitude DOUBLE PRECISION,
	dropoff_centroid_location GEOMETRY,
	computed_region_vrxf_vc4k INT,
	currency VARCHAR(10) default 'USD',
	PRIMARY KEY (company, trip_id)
);

ALTER TABLE trips
ALTER COLUMN trip_start_timestamp TYPE TIMESTAMP WITH TIME ZONE,
ALTER COLUMN trip_end_timestamp TYPE TIMESTAMP WITH TIME ZONE;
CREATE INDEX idx_trip_start_timestamp ON trips(trip_start_timestamp ASC);

/** https://gist.github.com/aveek22/daf288a18480ab91be7a17cf33ad7cfc **/
