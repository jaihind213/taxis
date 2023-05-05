package com.creativefab.ingest.persist;

import com.creativefab.Life;
import com.creativefab.model.TaxiTrip;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

public interface DataPersister extends Life {

    public void persist(String record, Map<String, Object> meta) throws Exception;

    public void setBucketStartTime(LocalDateTime bucketStartTime);
    public void setFormat(String format);
}
