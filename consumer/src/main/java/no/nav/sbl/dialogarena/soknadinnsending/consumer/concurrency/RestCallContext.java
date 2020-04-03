package no.nav.sbl.dialogarena.soknadinnsending.consumer.concurrency;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import javax.ws.rs.client.Client;


public final class RestCallContext {
    
    private final Client client;
    private final ThreadPoolExecutor executorService;
    private final long executorTimeoutInMilliseconds;
    
    private RestCallContext(Client client, ThreadPoolExecutor executorService, long executorTimeoutInMilliseconds) {
        this.client = client;
        this.executorService = executorService;
        this.executorTimeoutInMilliseconds = executorTimeoutInMilliseconds;
    }
    
    
    public Client getClient() {
        return client;
    }
    
    public ExecutorService getExecutorService() {
        return executorService;
    }
    
    public long getExecutorTimeoutInMilliseconds() {
        return executorTimeoutInMilliseconds;
    }
    
    public int currentQueueSize() {
        return executorService.getQueue().size();
    }
    
    
    public static final class Builder {
        private Client client;
        private int concurrentRequests = 1;
        private int maximumQueueSize = 10;
        private long executorTimeoutInMilliseconds = 1000;
        
        public Builder withClient(Client client) {
            this.client = client;
            return this;
        }
        
        public Builder withConcurrentRequests(int concurrentRequests) {
            this.concurrentRequests = concurrentRequests;
            return this;
        }
        
        public Builder withMaximumQueueSize(int maximumQueueSize) {
            this.maximumQueueSize = maximumQueueSize;
            return this;
        }
        
        public Builder withExecutorTimeoutInMilliseconds(long executorTimeoutInMilliseconds) {
            this.executorTimeoutInMilliseconds = executorTimeoutInMilliseconds;
            return this;
        }
        
        public RestCallContext build() {
            if (client == null) {
                throw new IllegalArgumentException("client == null");
            }
            final ThreadPoolExecutor executorService = new ThreadPoolExecutor(
                    concurrentRequests,
                    concurrentRequests,
                    0L, TimeUnit.MILLISECONDS,
                    new LinkedBlockingQueue<>(maximumQueueSize));
            
            return new RestCallContext(client, executorService, executorTimeoutInMilliseconds);
        }
    }
}