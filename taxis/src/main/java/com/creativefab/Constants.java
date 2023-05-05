package com.creativefab;

public class Constants {

    //src config

    public final static String SRC_API_URL = "SRC_API_URL";
    public final static String SRC_ACCESS_TOKEN = "SRC_ACCESS_TOKEN";

    //pull config
    public final static String PULL_BATCH_SIZE = "PULL_BATCH_SIZE";
    public final static String TIME_BUCKET_TYPE = "TIME_BUCKET_TYPE";//How often to pull
    public final static String PULL_FILE_SIZE = "PULL_FILE_SIZE";//SIZE OF EACH FILE
    public final static String LAKE_CONTAINER = "LAKE_CONTAINER";//bucket name in lake
    public final static String LAKE_STORAGE_TYPE = "LAKE_STORAGE_TYPE";//LAKE STORAGE TYPE
    public final static String STAGING_PATH = "STAGING_PATH";//STAGIN AREA before upload to lake_path

}
