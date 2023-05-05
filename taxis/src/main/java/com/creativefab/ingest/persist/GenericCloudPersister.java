package com.creativefab.ingest.persist;

import com.creativefab.Constants;
import com.creativefab.LifeCycleException;

import java.io.IOException;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;
import java.util.Properties;

/**
 * persist to lake (s3:// or file:// or az:// etc... as supported apache Jcloud)
 */
public class GenericCloudPersister extends AbstractPersister {

    private GZipCsvStreamWriter zipCsvStreamWriter;

    public GenericCloudPersister(Properties config) throws IOException {
        super(config);
    }


    @Override
    public void persist(String record, Map<String, Object> meta) throws Exception {
        zipCsvStreamWriter.writeRecord(record,  meta);
    }

    @Override
    public void stop() throws LifeCycleException {
        super.stop();
        try {
            if(zipCsvStreamWriter != null){
                zipCsvStreamWriter.close();
            }
        } catch (Exception e) {
            throw new LifeCycleException("Error stopping GenericCloudPersister", e);
        }
    }

    @Override
    public void start() throws LifeCycleException {
        super.start();
        try {
            prepareZipWriter();
        } catch (IOException e) {
            throw new LifeCycleException("Error starting GenericCloudPersister", e);
        }
    }

    private void prepareZipWriter() throws IOException {
        String name = "trips_" + System.currentTimeMillis();

        this.writePath = getWritePath(config.getProperty(Constants.STAGING_PATH), TimeBucketType.from(config.getProperty(Constants.TIME_BUCKET_TYPE, "DAILY")), bucketStartTime);
        URI uri = URI.create(this.writePath);
        Path path = Paths.get(uri);
        Files.createDirectories(path);

        this.zipCsvStreamWriter = new GZipCsvStreamWriter(this.writePath,name + "." + format, name + ".zip", name + "." + format);
        this.zipCsvStreamWriter.setFileSizeBytes(Integer.parseInt(config.getProperty(Constants.PULL_FILE_SIZE, "67108864")));
    }
}
