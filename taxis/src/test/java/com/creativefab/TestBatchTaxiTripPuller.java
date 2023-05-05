package com.creativefab;

import com.creativefab.ingest.BatchTaxiTripPuller;
import com.creativefab.ingest.persist.PersistorFactory;
import org.apache.commons.io.FileUtils;
import org.junit.Assert;
import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Properties;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static com.creativefab.ingest.BatchTaxiTripPuller.FOR_DEMO;

public class TestBatchTaxiTripPuller {
    @Test
    public void testPull() throws Exception {

        Properties config = new Properties();
        String configFile = TestBatchTaxiTripPuller.class.getClassLoader().getResource("test.properties").getPath();
        config.load(Files.newInputStream(new File(configFile).toPath()));
        try {
            FileUtils.deleteDirectory(new File(config.getProperty(Constants.STAGING_PATH)));
        } catch (IOException e) {
        }
        FileUtils.forceMkdir(new File(config.getProperty(Constants.STAGING_PATH)));

        try {
            BatchTaxiTripPuller puller = new BatchTaxiTripPuller(config);
            System.setProperty(FOR_DEMO, "true");

            puller.setStartTime(LocalDateTime.of(2022,04, 01, 00, 00));
            puller.setPersister(PersistorFactory.get(config.getProperty(Constants.STAGING_PATH), config));
            puller.pullAndSave();
            Assert.assertEquals(1, Files.list(Paths.get("/tmp/foo/2022-04-01/00/")).collect(Collectors.toList()).size());
        } finally {
            System.setProperty(FOR_DEMO, "false");
        }
    }
}
