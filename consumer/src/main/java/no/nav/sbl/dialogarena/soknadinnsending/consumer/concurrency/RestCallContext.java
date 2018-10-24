package no.nav.sbl.dialogarena.soknadinnsending.consumer.concurrency;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import javax.ws.rs.client.Client;


public final class RestCallContext {
    
    private final Client client;
    private final ExecutorService executorService;
    private final long timeoutInMilliseconds;

    
    private RestCallContext(Client client, ExecutorService executorService, long timeoutInMilliseconds) {
        this.client = client;
        this.executorService = executorService;
        this.timeoutInMilliseconds = timeoutInMilliseconds;
    }
    
    
    public Client getClient() {
        return client;
    }
    
    public ExecutorService getExecutorService() {
        return executorService;
    }
    
    public long getTimeoutInMilliseconds() {
        return timeoutInMilliseconds;
    }
    
    
    public static final class Builder {
        private Client client;
        private int concurrentRequests = 1;
        private int maximumQueueSize = 10;
        private long timeoutInMilliseconds = 1000;
        
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
        
        public Builder withTimeoutInMilliseconds(long timeoutInMilliseconds) {
            this.timeoutInMilliseconds = timeoutInMilliseconds;
            return this;
        }
        
        public RestCallContext build() {
            if (client == null) {
                throw new IllegalArgumentException("client == null");
            }
            final ExecutorService executorService = new ThreadPoolExecutor(
                    concurrentRequests,
                    concurrentRequests,
                    0L, TimeUnit.MILLISECONDS,
                    new LinkedBlockingQueue<Runnable>(maximumQueueSize));
            
            return new RestCallContext(client, executorService, timeoutInMilliseconds);
        }
    }
}