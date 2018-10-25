package no.nav.sbl.dialogarena.soknadinnsending.consumer;

import static org.slf4j.LoggerFactory.getLogger;

import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.read.ListAppender;
import no.nav.sbl.dialogarena.soknadinnsending.consumer.adresse.AdresseSokConsumerImpl;

public final class LoggingTestUtils {

    private LoggingTestUtils() {
        
    }
    
    
    public static ListAppender<ILoggingEvent> createTestLogAppender() {
        final ListAppender<ILoggingEvent> listAppender = new ListAppender<ILoggingEvent>();
        listAppender.start();
        
        final Logger logger = (Logger) getLogger(AdresseSokConsumerImpl.class);
        logger.addAppender(listAppender);
        
        return listAppender;
    }
}
