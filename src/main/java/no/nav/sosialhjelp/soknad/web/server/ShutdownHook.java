//package no.nav.sosialhjelp.soknad.web.server;
//
//import org.slf4j.Logger;
//import org.slf4j.LoggerFactory;
//
//public class ShutdownHook extends Thread {
//
//    private static final Logger LOGGER = LoggerFactory.getLogger(ShutdownHook.class);
//
//    public ShutdownHook(Jetty jetty) {
//        super(new Hook(jetty));
//    }
//
//    private static class Hook implements Runnable {
//        private final Jetty jetty;
//
//        public Hook(Jetty jetty) {
//            this.jetty = jetty;
//        }
//
//        @Override
//        public void run() {
//            try {
//                LOGGER.info("shutdown initialized for sosialhjelp-soknad-api, allowing incoming requests for 7 seconds before continuing");
//                Thread.sleep(7000L);
//            } catch (InterruptedException e) {
//                e.printStackTrace();
//            }
//
//            LOGGER.info("shutting down server");
//            jetty.stop.run();
//            LOGGER.info("shutdown ok");
//        }
//    }
//}