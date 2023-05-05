package com.creativefab.model;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.opencsv.bean.AbstractBeanField;
import com.opencsv.exceptions.CsvConstraintViolationException;
import com.opencsv.exceptions.CsvDataTypeMismatchException;
import com.opencsv.exceptions.CsvRequiredFieldEmptyException;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Setter @Getter
public class CentroidLocation {
    private String type;
    private List<Double> coordinates;

    public static class CentroidConvertor extends AbstractBeanField {

        @Override
        protected Object convert(String s) throws CsvDataTypeMismatchException, CsvConstraintViolationException {
            //POINT (-87.9030396611 41.9790708201)
            String type = s.substring(0, s.lastIndexOf('(')-1);
            String coods = s.substring(s.lastIndexOf('(') + 1, s.lastIndexOf(')'));
            String coodArr[] = coods.split(" ");
            List<Double> coordinates = new ArrayList<>();
            for(String cood : coodArr){
                coordinates.add(Double.parseDouble(cood));
            }
            CentroidLocation centroidLocation = new CentroidLocation();
            centroidLocation.setType(type);
            centroidLocation.setCoordinates(coordinates);
            return centroidLocation;
        }

        @Override
        protected String convertToWrite(Object value) throws CsvDataTypeMismatchException, CsvRequiredFieldEmptyException {
            StringBuilder b = new StringBuilder();
            CentroidLocation loc = ((CentroidLocation)value);
            b.append(loc.getType());
            b.append(" ");
            b.append("(");
            for(int i =0; i< loc.getCoordinates().size();i++){
                b.append(loc.getCoordinates().get(i));
                if(i+1 != loc.getCoordinates().size()){
                    b.append(" ");
                }
            }
            b.append(")");
            return b.toString();
        }
    }

//    public static void main(String[] args) throws JsonProcessingException {
//        String j = "{\"trip_id\":\"4ab70db829e3eb87d0b53abce8838841cd3b8b70\",\"taxi_id\":\"fe806ca7a45786db86ed61fe4c027b1f5a5a4b5c9f359509fc1c7a6aff2eab95f70f3cfc1f2cfaa918fe61bdf174e3bc94ee370504ac80edbf558f1c94cfcf5b\",\"trip_start_timestamp\":\"2023-04-01T00:00:00.000\",\"trip_end_timestamp\":\"2023-04-01T00:00:00.000\",\"trip_seconds\":\"208\",\"trip_miles\":\"0.88\",\"pickup_community_area\":\"6\",\"dropoff_community_area\":\"6\",\"fare\":\"5.5\",\"tips\":\"0\",\"tolls\":\"0\",\"extras\":\"1\",\"trip_total\":\"6.5\",\"payment_type\":\"Cash\",\"company\":\"Chicago Independents\",\"pickup_centroid_latitude\":\"41.944226601\",\"pickup_centroid_longitude\":\"-87.655998182\",\"pickup_centroid_location\":{\"type\":\"Point\",\"coordinates\":[-87.6559981815,41.9442266014]},\"dropoff_centroid_latitude\":\"41.944226601\",\"dropoff_centroid_longitude\":\"-87.655998182\",\"dropoff_centroid_location\":{\"type\":\"Point\",\"coordinates\":[-87.6559981815,41.9442266014]}}\n";
//        TaxiTrip tt =  new ObjectMapper().readValue(j, TaxiTrip.class);
//        int a= 10;
//        System.out.println(new ObjectMapper().writeValueAsString(tt));
//    }
}
