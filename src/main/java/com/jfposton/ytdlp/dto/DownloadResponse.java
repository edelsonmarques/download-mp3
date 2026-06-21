package com.jfposton.ytdlp.dto;

public class DownloadResponse {
    private String message;
    private String output;
    private boolean success;

    public DownloadResponse() {
    }

    public DownloadResponse(String message, String output, boolean success) {
        this.message = message;
        this.output = output;
        this.success = success;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getOutput() {
        return output;
    }

    public void setOutput(String output) {
        this.output = output;
    }

    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }
}
