package com.creativefab;

import com.creativefab.ingest.persist.BlobStore;
import org.apache.commons.io.FileUtils;
import org.junit.Assert;
import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Properties;
import java.util.stream.Collectors;

public class TestBlobStore {

    @Test
    public void testPutBlob() throws IOException {
        Properties config = new Properties();
        String configFile = TestBlobStore.class.getClassLoader().getResource("test.properties").getPath();

        try {
            FileUtils.deleteDirectory(new File("/tmp/trips_bucket"));
        } catch (IOException e) {
        }
        FileUtils.forceMkdir(new File("/tmp/trips_bucket"));

        //BlobStore.class.getClassLoader().getResource("test.properties").getPath();
        config.load(Files.newInputStream(new File(configFile).toPath()));
        BlobStore blobStore = new BlobStore(config);
        blobStore.put(new File(new File(configFile).toURI()), "1999-01-01", "00");
        Assert.assertEquals(1, Files.list(Paths.get("/tmp/trips_bucket/1999-01-01/00/")).collect(Collectors.toList()).size());
    }
}
