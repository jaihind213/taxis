package com.creativefab.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.opencsv.bean.AbstractBeanField;
import com.opencsv.bean.CsvBindByName;
import com.opencsv.bean.CsvCustomBindByName;
import com.opencsv.exceptions.CsvConstraintViolationException;
import com.opencsv.exceptions.CsvDataTypeMismatchException;
import com.opencsv.exceptions.CsvRequiredFieldEmptyException;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Setter @Getter
public class TaxiTrip {
        @JsonProperty("trip_id") @CsvBindByName
        private String tripId;

        @JsonProperty("taxi_id") @CsvBindByName
        private String taxiId;
        @JsonProperty("trip_start_timestamp") @CsvBindByName
        private String tripStartTimestamp;
        @JsonProperty("trip_end_timestamp") @CsvBindByName
        private String tripEndTimestamp;
        @JsonProperty("trip_seconds") @CsvBindByName
        private Integer tripSeconds;

        @JsonProperty("trip_miles") @CsvBindByName
        private Float tripMiles;

        @JsonProperty("pickup_census_tract") @CsvBindByName
        private Long pickupCensusTract;
        @JsonProperty("dropoff_census_tract") @CsvBindByName
        private Long dropoffCensusTract;

        @JsonProperty("pickup_community_area") @CsvBindByName
        private Long pickupCommunityArea;
        @JsonProperty("dropoff_community_area") @CsvBindByName
        private Long dropoffCommunityArea;

        @CsvBindByName
        private Double fare;
        @CsvBindByName
        private Double tips;
        @CsvBindByName
        private Double tolls;
        @CsvBindByName
        private Double extras;

        @CsvBindByName @JsonProperty("trip_total")
        private Double tripTotal;

        @CsvBindByName @JsonProperty("payment_type")
        private String paymentType;

        @CsvBindByName
        private String company;


        @CsvBindByName @JsonProperty("pickup_centroid_latitude")
        private Double pickupCentroidLatitude;
        @CsvBindByName @JsonProperty("pickup_centroid_longitude")
        private Double pickupCentroidLongitude;

        @JsonProperty("pickup_centroid_location")
        @CsvCustomBindByName(converter = CentroidLocation.CentroidConvertor.class)
        private CentroidLocation pickupCentroidLocation;

        @CsvBindByName @JsonProperty("dropoff_centroid_latitude")
        private Double dropoffCentroidLatitude;
        @CsvBindByName @JsonProperty("dropoff_centroid_longitude")
        private Double dropoffCentroidLongitude;

        @JsonProperty("dropoff_centroid_location")
        @CsvCustomBindByName(converter = CentroidLocation.CentroidConvertor.class)
        private CentroidLocation dropoffCentroidLocation;

        @CsvBindByName @JsonProperty(":@computed_region_vrxf_vc4k")
        private Integer communityAreas; //:@computed_region_vrxf_vc4k

        @CsvBindByName
        private String currency = "USD";//default.

}


