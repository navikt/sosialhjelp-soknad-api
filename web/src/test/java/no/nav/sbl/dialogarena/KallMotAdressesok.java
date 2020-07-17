package no.nav.sbl.dialogarena;

import static no.nav.sbl.dialogarena.soknadinnsending.business.db.config.DatabaseTestContext.buildDataSource;
import static org.slf4j.LoggerFactory.getLogger;

import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import javax.naming.NamingException;
import javax.sql.DataSource;

import org.apache.log4j.MDC;
import org.slf4j.Logger;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.mock.jndi.SimpleNamingContextBuilder;

import no.nav.sbl.dialogarena.config.SoknadinnsendingConfig;
import no.nav.sbl.dialogarena.sendsoknad.domain.adresse.AdresseSokConsumer;
import no.nav.sbl.dialogarena.sendsoknad.domain.adresse.AdresseSokConsumer.AdresseData;
import no.nav.sbl.dialogarena.sendsoknad.domain.adresse.AdresseSokConsumer.Sokedata;
import no.nav.sbl.dialogarena.server.SoknadsosialhjelpServer;


/**
 * Verktøy for å ytelsesteste TPS.
 * 
 * Velg miljø gjennom å oppdatere "tps.adresse.url" i "environment-test.properties".
 * 
 * Se/endre main-metoden etter behov.
 */
public class KallMotAdressesok {
    private static final Logger logger = getLogger(KallMotAdressesok.class);

    public static final int PORT = 8181;

    private final PrintWriter report;
    private AdresseSokConsumer adresseSokConsumer;
    
    
    private KallMotAdressesok(PrintWriter report, AdresseSokConsumer adresseSokConsumer) {
        this.report = report;
        this.adresseSokConsumer = adresseSokConsumer;
    }
    
    
    private List<AdresseData> finnAdresse(String adressesok) {
        return adresseSokConsumer.sokAdresse(new Sokedata().withAdresse(adressesok)).adresseDataList;
    }
    
    private void writeReport(String line) {
        synchronized (report) {
            report.println(line);
            report.flush();
        }
    }
    
    private void testAdressekall(String adresse) {
        final long begin = System.currentTimeMillis();
        try {
            final List<AdresseData> adresser = finnAdresse(adresse);
            final long timeSpent = System.currentTimeMillis() - begin;
            writeReport(timeSpent + " " + adresser.size() + " " + adresse);
        } catch (RuntimeException e) {
            logger.warn("Feil ved adressekall", e);
            writeReport("F " + adresse + "; " + e.getClass().getSimpleName() + " " + e.getMessage());
        }
    }
    
    private static ApplicationContext initializeContext() throws IOException, NamingException {
        SoknadsosialhjelpServer.setFrom("environment-test.properties");
        //SubjectHandler.setSubjectHandlerService(new StaticSubjectHandlerService());
        final DataSource dataSource = buildDataSource("hsqldb.properties");

        final SimpleNamingContextBuilder builder = new SimpleNamingContextBuilder();
        builder.bind("jdbc/SoknadInnsendingDS", dataSource);
        builder.activate();

        return new AnnotationConfigApplicationContext(SoknadinnsendingConfig.class);
    }

    private static void setupCloseShutdownHook(final PrintWriter report) {
        Runtime.getRuntime().addShutdownHook(new Thread() {
            @Override
            public void run() {
                synchronized (report) {
                    report.close();
                }
            }
        });
    }

    private static String lagRapportfilnavn(final int NUMBER_PARALLELL_REQUESTS) {
        return "testrun-parallell-" + NUMBER_PARALLELL_REQUESTS + "-" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd-HHmmss")) + ".txt";
    }


    private static int tallMellom(final Random r, final int min, final int max) {
        return min + r.nextInt(max - min);
    }
    
    public static void main(String[] args) throws Exception {
        final int NUMBER_PARALLELL_REQUESTS = 2;
        final int PAUSE_BETWEEN_CALLS = 0;
        final int MINIMUM_FRAGMENT_LENGDE = 4;
        
        final ApplicationContext context = initializeContext();
        final AdresseSokConsumer adresseSokConsumer = context.getBean(AdresseSokConsumer.class);
        
        final PrintWriter report = new PrintWriter(lagRapportfilnavn(NUMBER_PARALLELL_REQUESTS));
        final KallMotAdressesok app = new KallMotAdressesok(report, adresseSokConsumer);
        
        final List<String> gater = Collections.unmodifiableList(Files.readAllLines(Paths.get("src/test/resources/gatenavn.txt")));
        final ExecutorService executorService = Executors.newFixedThreadPool(NUMBER_PARALLELL_REQUESTS);
        
        setupCloseShutdownHook(report);
        
        for (int j=0; j<NUMBER_PARALLELL_REQUESTS; j++) {
            final int threadNum = j;
            executorService.submit(() -> {
                try {
                    final Random r = new Random();
                    for (int i=0; i<100; i++) {
                        final String CALL_ID = "callId";
                        MDC.put(CALL_ID, "test" + threadNum + "," + i);
                        final String tilfeldigGatenavn = gater.get(r.nextInt(gater.size()));
                        final String tilfeldigGatenavnFragment = tilfeldigGatenavn.substring(0, tallMellom(r, MINIMUM_FRAGMENT_LENGDE, tilfeldigGatenavn.length()));
                        app.testAdressekall(tilfeldigGatenavnFragment);
                        MDC.remove(CALL_ID);
                        Thread.sleep(PAUSE_BETWEEN_CALLS);
                    }
                } catch (InterruptedException e) {
                    logger.warn("Interrupted", e);
                }
            });
        }
        
        executorService.shutdown();
        executorService.awaitTermination(5, TimeUnit.HOURS);
        report.flush();
        report.close();
        System.exit(0);
    }
}
