package io.airbrake.javabrake;

public class PayLoad {
    Object data;
    String path;
    int retryCount;
    
    public PayLoad(Object data, String path, int retryCount) {
        this.data = data;
        this.path = path;
        this.retryCount = retryCount;
    } 
}
