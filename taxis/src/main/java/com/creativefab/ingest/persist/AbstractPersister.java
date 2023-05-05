package com.creativefab.ingest.persist;

import com.creativefab.Constants;
import com.creativefab.Life;
import com.creativefab.LifeCycleException;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Properties;

public abstract class AbstractPersister implements DataPersister {
    @Getter @Setter
    protected LocalDateTime bucketStartTime;
    protected Properties config;

    protected String writePath;
    protected TimeBucketType cadence;
    protected String format;

    public AbstractPersister(Properties config) {
        this.config = config;
        this.cadence = TimeBucketType.from(config.getProperty(Constants.TIME_BUCKET_TYPE,"DAILY"));
    }

    public static String getWritePath(String lakePath, TimeBucketType dataBucketType, LocalDateTime bucketStartTime){
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        String formattedDate = bucketStartTime.format(formatter);
        if(dataBucketType == TimeBucketType.HOURLY){
            int hour = bucketStartTime.getHour();
            String hrString = String.valueOf(hour);
            hrString = hrString.length() == 2 ? hrString : "0" + hrString;
            return lakePath + "/" + formattedDate + "/" + hrString;
        }else if (dataBucketType == TimeBucketType.HALF_HOURLY || dataBucketType == TimeBucketType.FIFTEEN_MIN){
            int hour = bucketStartTime.getHour();
            String hrString = String.valueOf(hour);
            hrString = hrString.length() == 2 ? hrString : "0" + hrString;

            int min = bucketStartTime.getMinute();
            String minString = String.valueOf(min);
            minString = minString.length() == 2 ? minString : "0" + minString;
            return lakePath + "/" + formattedDate + "/" + hrString  +"/" + minString;
        }
        return lakePath + "/" + formattedDate;//else daily
    }

    public void setBucketStartTime(LocalDateTime bucketStartTime){
        this.bucketStartTime = bucketStartTime;
    }

    @Override
    public void start() throws LifeCycleException {
        if(bucketStartTime == null){
            throw new LifeCycleException("BucketStartTime not set for persister");
        }
    }

    @Override
    public void stop() throws LifeCycleException {

    }

    @Override
    public void setFormat(String format) {
        this.format = format;
    }
}
