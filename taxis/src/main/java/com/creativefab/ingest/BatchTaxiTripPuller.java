package com.creativefab.ingest;

import com.creativefab.Constants;
import com.creativefab.LifeCycleException;
import com.creativefab.TimebucketUtil;
import com.creativefab.ingest.persist.BlobStore;
import com.creativefab.ingest.persist.DataPersister;
import com.creativefab.ingest.persist.PersistorFactory;
import com.creativefab.ingest.persist.TimeBucketType;
import com.socrata.builders.SoqlQueryBuilder;
import com.socrata.model.soql.SoqlQuery;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;


@Slf4j
public class BatchTaxiTripPuller {

    public static String FOR_DEMO = "__FOR_DEMO";// this flag should be set only for demo purposes only
    private static final String EMPTY_RESPONSE = "[]";
    Properties config;

    @Setter @Getter
    private LocalDateTime startTime;
    @Setter @Getter
    private LocalDateTime endTime;

    @Setter
    private DataPersister persister;

    public BatchTaxiTripPuller(Properties config) {
        this.config = config;
    }

    public void pullAndSave() throws Exception {

        TimeBucketType bucketType = TimeBucketType.from(this.config.getProperty(Constants.TIME_BUCKET_TYPE,"HOURLY"));
        if(persister == null){
            throw new IllegalArgumentException("persister not set. so cant save data.");
        }
        if(this.startTime != null & this.endTime != null){
            if(this.startTime.isAfter(this.endTime)){
                throw new IllegalArgumentException("bucket start time is after end time. illegal arguments");
            }
            if(!this.startTime.plusMinutes(bucketType.getDurationMin()).equals(this.endTime)){
                throw new IllegalArgumentException("bucket start time:"+ startTime +" + bucket min duration(" + bucketType.getDurationMin() + ") != bucket end time: " + endTime  );
            }
        }
        if(this.startTime == null){
            //read previous bucket
            this.startTime = TimebucketUtil.convertToUtc(TimebucketUtil.getPrevBucketStartTime(LocalDateTime.now(), bucketType));
            //since start time in utc, no need to convert end time. just add duration.
            this.endTime = this.startTime.plusMinutes(bucketType.getDurationMin());
        }
        if(endTime == null){
            this.endTime = this.startTime.plusMinutes(bucketType.getDurationMin());
        }

        String appToken = this.config.getProperty(Constants.SRC_ACCESS_TOKEN, "soda_token_not_set");
        String srcUrl = this.config.getProperty(Constants.SRC_API_URL, "https://data.cityofchicago.org/resource/wrvz-psew.csv");
        int limit = Integer.parseInt(this.config.getProperty(Constants.PULL_BATCH_SIZE, "10000"));
        Map<String, Object> recMeta = new HashMap<>();


        this.persister.setBucketStartTime(this.startTime);
        String responeFormat = getResponseFormatSrcApi(srcUrl);
        this.persister.setFormat(responeFormat);
        this.persister.start();


        boolean hasData = true;
        int offset = 0;
        while (hasData){
            SoqlQuery query = getQuery(this.startTime, this.endTime, offset, limit);
            String encodedQuery = URLEncoder.encode(query.toString(), "UTF-8");
            String urlString = srcUrl + "?$query="  + encodedQuery;
            URL url = new URL(urlString);
            HttpURLConnection connection = null;

            try {
                connection = (HttpURLConnection) url.openConnection();
                connection.setRequestMethod("GET");
                connection.setRequestProperty("X-App-Token", appToken);

                InputStreamReader inputStreamReader = null;
                BufferedReader bufferedReader= null;
                try {
                    inputStreamReader = new InputStreamReader(connection.getInputStream());
                    log.info("completed a read with status code:", connection.getResponseCode());
                    System.out.println( connection.getResponseCode() +"---" + urlString);

                    if(connection.getResponseCode() != 200){
                        log.error("error reading from src. got response code:" , connection.getResponseCode());
                        String errorResponse = "";
                        try (BufferedReader in = new BufferedReader(new InputStreamReader(connection.getErrorStream()))) {
                            String line;
                            while ((line = in.readLine()) != null) {
                                errorResponse += line;
                            }
                        }
                        log.error("error reading from src:", errorResponse);
                        hasData = false;
                        break;
                    }

                    bufferedReader = new BufferedReader(inputStreamReader);
                    String line;
                    int numRecsRecevied = 0;
                    while ((line = bufferedReader.readLine()) != null) {
                        if(EMPTY_RESPONSE.equalsIgnoreCase(line)){
                            hasData=false;
                            log.info("no more data to read.");
                            break;
                        } else {
                            if("csv".equalsIgnoreCase(responeFormat)){
                                if(numRecsRecevied == 0){
                                    recMeta.put("HEADER", true);
                                    persister.persist(line, recMeta);
                                    recMeta.remove("HEADER");
                                }else{
                                    persister.persist(line, recMeta);
                                }
                                numRecsRecevied++;
                            }else{
                                //json
                                try {
                                    persister.persist(santizeJsonLine(line), recMeta);
                                } catch (Exception e) {
                                    log.error("error persisting json: ", e);
                                    throw e;
                                }
                            }
                        }
                    }
                    if(numRecsRecevied <= 1 || numRecsRecevied < limit){
                        hasData = false;
                        break;
                    }
                } finally {
                    if(bufferedReader!= null){
                        try {
                            bufferedReader.close();
                        } catch (IOException didOurBest) {}
                    }
                    if(inputStreamReader!= null){
                        try {
                            inputStreamReader.close();
                        } catch (IOException didOurBest) {}
                    }
                }
            } finally {
                if(connection != null){
                    connection.disconnect();
                }
            }

            offset = offset + limit;
        }

        try {
            try {
                persister.stop();
            } catch (LifeCycleException e) {
            }
        } catch (Exception e) {
            log.error("error closing the persistor" ,e);
        }
    }

    static String santizeJsonLine(String line){
        String output = line;
        if(output.startsWith("[")) output =  output.substring(1);
        if(output.startsWith(",")) output = output.substring(1);
        if(output.startsWith("]")) output = output.substring(0, line.length());
        return output;
    }

    static String getResponseFormatSrcApi(String url){
        return url.endsWith("csv") ? "csv" : "json";
    }

    static SoqlQuery getQuery(LocalDateTime startTime, LocalDateTime endTime, int offset, int limit) {
        final boolean forDemo = "true".equals(System.getProperty(FOR_DEMO, "false"));
        if(forDemo){
            limit = 5;
        }
        SoqlQueryBuilder builder = new SoqlQueryBuilder().addSelectPhrase("*")
                //.setWhereClause(("trip_start_timestamp between '2022-04-01T00:15:00.000' and '2022-04-01T00:30:00.000'"))
                .setWhereClause(("trip_start_timestamp between '" + startTime.toString() +"' and '" + endTime.toString() +"'" ))
                //.setWhereClause(("trip_id = '18c743a9f888d4234c06bef97d981027ecf79cb8'" ))
                .setOffset(offset)
                .setLimit(limit);

        if (forDemo) {
            System.err.println("application started in demo mode!!!");
            System.err.println("application started in demo mode!!!");
            System.err.println("application started in demo mode!!!");
            builder.setWhereClause("trip_start_timestamp between '2022-04-01T00:15:00.000' and '2022-04-01T00:30:00.000'");
        }
        return builder.build();
    }


    public static void main(String[] args) throws Exception {
        if(args.length != 3){
            throw new IllegalArgumentException("need 3 args, (path to config file, YYYY-MM-DD & HH)");
        }
        String configFile = args[0]; //absolute path
        String date = args[1]; //YYYY-MM-DD
        String hour = args[2]; //HH

        String[] timeParts = date.split("-");
        if(timeParts.length != 3){
            throw new IllegalArgumentException("argument 1 (date) not in YYYY-MM-DD format");
        }

        hour = hour.length() < 2 ? "0" + hour : hour;
        Properties config = new Properties();
        config.load(Files.newInputStream(new File(configFile).toPath()));

        BatchTaxiTripPuller puller = new BatchTaxiTripPuller(config);
        BlobStore blobStore = new BlobStore(config);

        puller.setStartTime(LocalDateTime.of(Integer.parseInt(timeParts[0]), Integer.parseInt(timeParts[1]), Integer.parseInt(timeParts[2]), Integer.parseInt(hour), 00, 00));
        puller.setPersister(PersistorFactory.get(config.getProperty(Constants.STAGING_PATH), config));
        puller.pullAndSave();

        URI finalStagingPath = URI.create(config.getProperty(Constants.STAGING_PATH)+ "/"+ date +"/"+hour);

        try (Stream<Path> stream = Files.list(Paths.get(finalStagingPath))) {
         Set<String> paths =  stream
                    .filter(file -> !Files.isDirectory(file))
                    .map(Path::toAbsolutePath)
                    .map(Path::toString)
                    .collect(Collectors.toSet());
         for(String path : paths){
             blobStore.put(new File(path), date, hour);
         }
        }
        blobStore.close();
    }

}
