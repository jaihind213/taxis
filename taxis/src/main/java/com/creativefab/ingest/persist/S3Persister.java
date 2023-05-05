package com.creativefab.ingest.persist;

import com.creativefab.LifeCycleException;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.Properties;

public class S3Persister extends AbstractPersister{

    public S3Persister(Properties config, LocalDateTime bucketStartTime) {
        super(config );
    }

    @Override
    public void persist(String record, Map<String, Object> meta) throws Exception {
        throw new UnsupportedOperationException("S3 Persisting: todo");
    }

    @Override
    public void start() throws LifeCycleException {
        super.start();
        //todo
    }

    @Override
    public void stop() throws LifeCycleException {
        super.stop();
        //todo
    }
}
