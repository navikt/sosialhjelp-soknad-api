package no.nav.sosialhjelp.soknad.consumer;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.read.ListAppender;
import no.nav.sosialhjelp.soknad.consumer.adresse.AdresseSokConsumerImpl;

import static org.slf4j.LoggerFactory.getLogger;

public final class LoggingTestUtils {

    private LoggingTestUtils() {
        
    }
    
    
    public static ListAppender<ILoggingEvent> createTestLogAppender(Level threshold) {
        final ListAppender<ILoggingEvent> listAppender = new ListAppender<ILoggingEvent>() {
            @Override
            protected void append(ILoggingEvent e) {
                if (e.getLevel().isGreaterOrEqual(threshold)) {
                    super.append(e);
                }
            }
        };
        listAppender.start();
        
        final Logger logger = (Logger) getLogger(AdresseSokConsumerImpl.class);
        logger.addAppender(listAppender);
        
        return listAppender;
    }
}
