package com.creativefab.ingest.persist;

import java.util.Properties;

public class PersistorFactory {
    public static DataPersister get(String stagingAreaUrl, Properties config) throws Exception {
        if(stagingAreaUrl.startsWith("file://")){
            return new GenericCloudPersister(config);
        }else if (stagingAreaUrl.startsWith("s3://")){
            throw new UnsupportedOperationException("s3 lake not supported. check LAKE_URL.  file://");
        }else {
            throw new UnsupportedOperationException("lake not supported. check LAKE_URL. s3:// or file://");
        }
    }
}
