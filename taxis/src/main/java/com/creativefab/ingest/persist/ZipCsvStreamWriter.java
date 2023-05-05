package com.creativefab.ingest.persist;

import lombok.Setter;

import java.io.*;
import java.net.URI;
import java.util.HashMap;
import java.util.Map;
import java.util.zip.*;

//todo: if file entry is empty, dont have a zip at all. i.e on unzipping file, we get file of 0 bytes
public class ZipCsvStreamWriter {

    private final String csvFileName;
    private final String zipFileName;
    private final String zipEntryName;
    private final String path;

    @Setter
    private int fileSizeBytes = 64* 1024 * 1024;
    private OutputStreamWriter writer;
    private ZipOutputStream zipOutputStream;
    private ZipEntry zipEntry;
    private int fileIndex = 0;
    private long bytesWritten = 0;

    private String header;
    public ZipCsvStreamWriter(String path, String csvFileName, String zipFileName, String zipEntryName) throws IOException {
        this.csvFileName = csvFileName;
        this.zipFileName = zipFileName;
        this.zipEntryName = zipEntryName;
        this.path = path;

        URI uri = URI.create(path + "/" + getNextZipFileName());
        // Create a new ZipOutputStream and set the compression level to maximum
        zipOutputStream = new ZipOutputStream(new FileOutputStream(uri.getPath()));
        zipOutputStream.setLevel(Deflater.DEFAULT_COMPRESSION);

        // Create a new ZipEntry with the given name and add it to the ZipOutputStream
        zipEntry = new ZipEntry(zipEntryName);
        zipOutputStream.putNextEntry(zipEntry);

        // Create a new OutputStreamWriter with UTF-8 encoding and write the CSV header
        writer = new OutputStreamWriter(zipOutputStream, "UTF-8");
    }

    public void writeRecord(String record, Map<String, Object> meta) throws IOException {
        if(meta.containsKey("HEADER")){
            this.header = record;
        }
        if(bytesWritten > 0 && meta.containsKey("HEADER")){
            //already written header. ignore
            header = record;
            return;
        }
        // Write the CSV record to the OutputStreamWriter
        writer.write(record + "\n");
        bytesWritten += record.length();
        if (bytesWritten > fileSizeBytes) { // If the file size has exceeded 64MB
            rollover();
        }
    }

    private void rollover() throws IOException {
        // Flush and close the OutputStreamWriter and the ZipOutputStream
        writer.flush();
        zipOutputStream.closeEntry();
        zipOutputStream.close();
        writer.close();

        // Increment the file index and create a new ZipOutputStream with the next file name
        fileIndex++;
        URI uri = URI.create(path + "/" + getNextZipFileName());
        zipOutputStream = new ZipOutputStream(new FileOutputStream(uri.getPath()));
        zipOutputStream.setLevel(Deflater.DEFAULT_COMPRESSION);

        // Create a new ZipEntry with the given name and add it to the ZipOutputStream
        zipEntry = new ZipEntry(zipEntryName);
        zipOutputStream.putNextEntry(zipEntry);

        // Create a new OutputStreamWriter with UTF-8 encoding and write the CSV header
        writer = new OutputStreamWriter(zipOutputStream, "UTF-8");

        // Reset the bytesWritten counter
        if(header != null){
            writer.write(header +"\n");
            bytesWritten = header.length();
        }else{
            bytesWritten = 0;
        }
    }

    private String getNextZipFileName() {
        // Generate the next file name based on the file index
        return zipFileName.substring(0, zipFileName.lastIndexOf(".")) + "-" + fileIndex + ".zip";
    }

    public void close() throws IOException {
        // Flush and close the OutputStreamWriter and the ZipOutputStream
        writer.flush();
        zipOutputStream.closeEntry();
        zipOutputStream.close();
        writer.close();
    }


    public static void main(String[] args) throws IOException {
        GZipCsvStreamWriter writer = new GZipCsvStreamWriter("file:///tmp/foo" , "big.csv", "example.zip", "example.csv");
        for (int i = 0; i < 1000000; i++) {
            writer.writeRecord(new String("a,b,c,d,e,f,g,h,i"), new HashMap<>());
        }
        writer.close();
    }

}