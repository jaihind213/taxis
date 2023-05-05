package com.creativefab.ingest.persist;

import com.creativefab.LifeCycleException;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.Properties;

public class ConsolePersister extends AbstractPersister {

    public static final ConsolePersister UTIL_PERSISTER  = new ConsolePersister();

    public ConsolePersister() {
        super(new Properties());
    }

    @Override
    public void persist(String line, Map map) throws Exception {
        System.out.println(line);
    }

    @Override
    public void start() throws LifeCycleException {
    }
}
