package com.jfposton.ytdlp.dto;

public class DownloadRequest {
    private String url;
    private String destinationPath;

    public DownloadRequest() {
    }

    public DownloadRequest(String url, String destinationPath) {
        this.url = url;
        this.destinationPath = destinationPath;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getDestinationPath() {
        return destinationPath;
    }

    public void setDestinationPath(String destinationPath) {
        this.destinationPath = destinationPath;
    }
}
