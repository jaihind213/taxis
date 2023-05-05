package com.creativefab.ingest.persist;

import com.creativefab.Constants;
import org.jclouds.ContextBuilder;
import org.jclouds.blobstore.BlobStoreContext;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.Properties;

import org.jclouds.blobstore.domain.Blob;
import org.jclouds.blobstore.domain.BlobBuilder;
import org.jclouds.filesystem.reference.FilesystemConstants;
import org.jclouds.io.Payloads;

public class BlobStore {
    private final Properties config;
    private org.jclouds.blobstore.BlobStore blobStore;
    private  String container;
    private BlobStoreContext context;

    public BlobStore(Properties config) {
        this.config = config;
        this.container = config.getProperty(Constants.LAKE_CONTAINER);
        String storageType = config.getProperty(Constants.LAKE_STORAGE_TYPE);
        String provider = "";

        if(storageType.equalsIgnoreCase("local")){
            String path = config.getProperty(Constants.LAKE_CONTAINER);
            if(path.startsWith("file://")){
                path = path.substring(path.indexOf("://")+3);
            }
            String []spltis = path.split("/");
            this.container = spltis[spltis.length-1];
            path = "/";
            for(int i=0;i< spltis.length-1;i++){
                if(spltis[i].equalsIgnoreCase("")){
                    continue;
                }
                path = path + spltis[i] + "/";
            }
            this.config.setProperty(FilesystemConstants.PROPERTY_BASEDIR, path);
            provider = "filesystem";
        }else if(storageType.equalsIgnoreCase("s3")){
            provider = "aws-s3";
            //todo: put creds etc
        }
        this.context = ContextBuilder.newBuilder(provider)
                .overrides(config)
                .buildView(BlobStoreContext.class);
        this.blobStore = context.getBlobStore();
    }

    public void put(File file, String date, String hour){
        BlobBuilder blobBuilder = blobStore.blobBuilder(date +"/"+hour  +"/"+ file.getName());
        blobBuilder.payload(Payloads.newFilePayload(file));
        Blob blob = blobBuilder.build();
        this.blobStore.putBlob(this.container, blob);
    }


    public void close(){
        try {
            this.context.close();
        } catch (Exception ignore) {
        }
        this.blobStore = null;
    }

//    public static void main(String[] args) throws IOException {
//        Properties config = new Properties();
//        String configFile = "/Users/vishnuch/work/gitcode/taxis/taxis/src/test/resources/test.properties";
//                //BlobStore.class.getClassLoader().getResource("test.properties").getPath();
//        config.load(Files.newInputStream(new File(configFile).toPath()));
//        BlobStore blobStore = new BlobStore(config);
//        blobStore.put(new File("/Users/vishnuch/work/gitcode/taxis/taxis/src/test/resources/test.properties"), "1999-01-01", "00");
//    }
}

