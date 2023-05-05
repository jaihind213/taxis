package com.creativefab;

import com.creativefab.ingest.persist.TimeBucketType;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.temporal.ChronoUnit;

public class TimebucketUtil {

    public static LocalDateTime getPrevBucketStartTime(LocalDateTime currentTime, TimeBucketType bucketType){
        LocalDateTime prevBucketStartTime = null;
        LocalDateTime currTimeBucketStartTime = getTimeBucketStartTimeFor(currentTime, bucketType.getDurationMin());
        return getTimeBucketStartTimeFor(currTimeBucketStartTime.minusMinutes(1), bucketType.getDurationMin());
    }
    public static LocalDateTime getTimeBucketStartTimeFor(LocalDateTime timestamp, int intervalMin) {
        long minute = timestamp.getMinute();
        long minuteBucket = (minute / intervalMin) * intervalMin;
        if(intervalMin == 24*60){
            return timestamp.truncatedTo(ChronoUnit.DAYS)
                    .withMinute((int) minuteBucket)
                    .withSecond(0)
                    .withNano(0);
        }
        return timestamp.truncatedTo(ChronoUnit.HOURS)
                .withMinute((int) minuteBucket)
                .withSecond(0)
                .withNano(0);
    }

    //send all requests with regard to utc time to chicago
    public static LocalDateTime convertToUtc(LocalDateTime localTimeZoneDateTime){

        // get the time zone ID for your local time zone
        ZoneId localZoneId = ZoneId.systemDefault();

        // convert the LocalDateTime object to a ZonedDateTime object in your local time zone
        ZonedDateTime localZonedDateTime = localTimeZoneDateTime.atZone(localZoneId);

        // convert the ZonedDateTime object to an Instant object in UTC
        Instant utcInstant = localZonedDateTime.toInstant();

        //System.out.println("Local DateTime: " + localTimeZoneDateTime);
        //System.out.println("UTC Instant: " + utcInstant);
        return LocalDateTime.ofInstant(utcInstant, ZoneId.of("UTC"));
    }
}
