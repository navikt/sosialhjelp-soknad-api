//package no.nav.sosialhjelp.soknad.consumer.common.rest;
//
//public class RestConfig {
//
//    private static final int DEFAULT_CONNECT_TIMEOUT = 5000;
//    private static final int DEFAULT_READ_TIMEOUT = 15000;
//
//    private final int connectTimeout;
//    private final int readTimeout;
//    private final boolean disableMetrics;
//    private final boolean disableParameterLogging;
//
//    public RestConfig(Builder builder) {
//        this.connectTimeout = builder.connectTimeout;
//        this.readTimeout = builder.readTimeout;
//        this.disableMetrics = builder.disableMetrics;
//        this.disableParameterLogging = builder.disableParameterLogging;
//    }
//
//    public int getConnectTimeout() {
//        return connectTimeout;
//    }
//
//    public int getReadTimeout() {
//        return readTimeout;
//    }
//
//    public boolean getDisableMetrics() {
//        return disableMetrics;
//    }
//
//    public boolean getDisableParameterLogging() {
//        return disableParameterLogging;
//    }
//
//    public static class Builder {
//        private int connectTimeout = DEFAULT_CONNECT_TIMEOUT;
//        private int readTimeout = DEFAULT_READ_TIMEOUT;
//        private boolean disableMetrics;
//        private boolean disableParameterLogging;
//
//        public Builder(){
//            // no-op
//        }
//
//        public Builder withConnectTimeout(int connectTimeout) {
//            this.connectTimeout = connectTimeout;
//            return this;
//        }
//
//        public Builder withReadTimeout(int readTimeout) {
//            this.readTimeout = readTimeout;
//            return this;
//        }
//
//        public Builder withDisableMetrics(boolean disableMetrics) {
//            this.disableMetrics = disableMetrics;
//            return this;
//        }
//
//        public Builder withDisableParameterLogging(boolean disableParameterLogging) {
//            this.disableParameterLogging = disableParameterLogging;
//            return this;
//        }
//
//        public RestConfig build() {
//            return new RestConfig(this);
//        }
//
//    }
//}
