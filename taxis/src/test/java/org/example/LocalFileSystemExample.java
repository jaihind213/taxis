package org.example;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.util.Properties;

import org.jclouds.ContextBuilder;
import org.jclouds.blobstore.BlobStore;
import org.jclouds.blobstore.BlobStoreContext;
import org.jclouds.blobstore.domain.Blob;
import org.jclouds.blobstore.options.PutOptions;
import org.jclouds.filesystem.reference.FilesystemConstants;
import org.jclouds.io.Payloads;

public class LocalFileSystemExample {
    public static void main(String[] args) throws IOException {
        Properties properties = new Properties();
        properties.setProperty(FilesystemConstants.PROPERTY_BASEDIR, "/tmp");
// setup the container name used by the provider (like bucket in S3)
        String containerName = "foo";

// get a context with filesystem that offers the portable BlobStore api
        BlobStoreContext context = ContextBuilder.newBuilder("filesystem")
                .overrides(properties)
                .buildView(BlobStoreContext.class);

// create a container in the default location
        BlobStore blobStore = context.getBlobStore();
        blobStore.createContainerInLocation(null, containerName);

// add blob
//        Blob blob = blobStore.newBlob("test");
//        blob.setPayload("test data");
//        blobStore.putBlob(containerName, blob);

// retrieve blob
        Blob blobRetrieved = blobStore.getBlob(containerName, "example.csv");
        int a  =10;

        try {
            InputStream input = blobRetrieved.getPayload().openStream();
            int data = input.read();
            while(data != -1) {
                System.out.print((char) data);
                data = input.read();
            }
            input.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        blobRetrieved.getPayload().close();
// delete blob
       // blobStore.removeBlob(containerName, "test");

//close context
        context.close();

    }
}




