package com.creativefab;

import com.creativefab.ingest.persist.TimeBucketType;
import org.junit.Assert;
import org.junit.Test;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;

public class TimeBucketUtilTest {

    @Test
    public void testGetPrevTimeBucketStartTime15MinDuration(){
        LocalDateTime time = LocalDateTime.of(2023, 12, 2, 13, 7);
        Assert.assertEquals(LocalDateTime.of(2023, 12, 2, 12, 45),
                TimebucketUtil.getPrevBucketStartTime(time, TimeBucketType.FIFTEEN_MIN));

        time = LocalDateTime.of(2023, 12, 2, 13, 35);
        Assert.assertEquals(LocalDateTime.of(2023, 12, 2, 13, 15),
                TimebucketUtil.getPrevBucketStartTime(time, TimeBucketType.FIFTEEN_MIN));
    }
    @Test
    public void testGetPrevTimeBucketStartTime30MinDuration(){
        LocalDateTime time = LocalDateTime.of(2023, 12, 2, 13, 7);
        Assert.assertEquals(LocalDateTime.of(2023, 12, 2, 12, 30),
                TimebucketUtil.getPrevBucketStartTime(time, TimeBucketType.HALF_HOURLY));

        time = LocalDateTime.of(2023, 12, 2, 13, 35);
        Assert.assertEquals(LocalDateTime.of(2023, 12, 2, 13, 00),
                TimebucketUtil.getPrevBucketStartTime(time, TimeBucketType.HALF_HOURLY));
    }

    @Test
    public void testGetPrevTimeBucketStartTime60MinDuration(){
        LocalDateTime time = LocalDateTime.of(2023, 12, 2, 13, 7);
        Assert.assertEquals(LocalDateTime.of(2023, 12, 2, 12, 00),
                TimebucketUtil.getPrevBucketStartTime(time, TimeBucketType.HOURLY));

        time = LocalDateTime.of(2023, 12, 2, 13, 35);
        Assert.assertEquals(LocalDateTime.of(2023, 12, 2, 12, 00),
                TimebucketUtil.getPrevBucketStartTime(time, TimeBucketType.HOURLY));
    }

    @Test
    public void testGetPrevTimeBucketStartTime1DayDuration(){
        LocalDateTime time = LocalDateTime.of(2023, 12, 2, 00, 7);
        Assert.assertEquals(LocalDateTime.of(2023, 12, 1, 00, 00),
                TimebucketUtil.getPrevBucketStartTime(time, TimeBucketType.DAILY));

        time = LocalDateTime.of(2023, 12, 2, 13, 35);
        Assert.assertEquals(LocalDateTime.of(2023, 12, 1, 00, 00),
                TimebucketUtil.getPrevBucketStartTime(time, TimeBucketType.DAILY));

        LocalDateTime localDateTime = LocalDateTime.now();

// get the time zone ID for your local time zone
        ZoneId localZoneId = ZoneId.systemDefault();

// convert the LocalDateTime object to a ZonedDateTime object in your local time zone
        ZonedDateTime localZonedDateTime = localDateTime.atZone(localZoneId);

// convert the ZonedDateTime object to an Instant object in UTC
        Instant utcInstant = localZonedDateTime.toInstant();

        System.out.println("Local DateTime: " + localDateTime);
        System.out.println("UTC Instant: " + utcInstant);
    }

    @Test
    public void testGetUTCTime(){
        LocalDateTime time = LocalDateTime.of(2023, 12, 02, 14, 7);
        Assert.assertEquals(LocalDateTime.of(2023, 12, 02, 06, 7),
                TimebucketUtil.convertToUtc(time));
    }
}
