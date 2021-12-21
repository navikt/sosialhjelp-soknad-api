//package no.nav.sosialhjelp.soknad.web.selftest;
//
//import no.nav.sosialhjelp.soknad.web.selftest.domain.Selftest;
//import no.nav.sosialhjelp.soknad.web.selftest.domain.SelftestEndpoint;
//import no.nav.sosialhjelp.soknad.web.utils.MiljoUtils;
//import org.apache.commons.lang3.exception.ExceptionUtils;
//import org.slf4j.Logger;
//import org.springframework.context.ApplicationContext;
//import org.springframework.stereotype.Component;
//
//import java.time.LocalDateTime;
//import java.time.format.DateTimeFormatter;
//import java.util.Collection;
//import java.util.List;
//import java.util.function.Function;
//import java.util.function.Predicate;
//
//import static java.util.Optional.ofNullable;
//import static java.util.stream.Collectors.toList;
//import static org.slf4j.LoggerFactory.getLogger;
//
//@Component
//public class SelftestService {
//
//    private static final Logger log = getLogger(SelftestService.class);
//
//    private static final Predicate<Pingable.Ping> KRITISK_FEIL = ping -> ping.harFeil() && ping.getMetadata().isKritisk();
//    private static final Predicate<Pingable.Ping> HAR_FEIL = Pingable.Ping::harFeil;
//
//    public static final int STATUS_OK = 0;
//    public static final int STATUS_ERROR = 1;
//    public static final int STATUS_WARNING = 2;
//
//    private List<Pingable.Ping> result;
//    private volatile long lastResultTime;
//
//    private final ApplicationContext appContext;
//
//    public SelftestService(ApplicationContext appContext) {
//        this.appContext = appContext;
//    }
//
//    public Selftest lagSelftest() {
//        doPing();
//
//        return new Selftest()
//                .setApplication(MiljoUtils.getNaisAppName())
//                .setVersion(MiljoUtils.getNaisAppImage())
//                .setTimestamp(LocalDateTime.now().format(DateTimeFormatter.ISO_DATE_TIME))
//                .setAggregateResult(getAggregertStatus())
//                .setChecks(result.stream()
//                        .map(this::lagSelftestEndpoint)
//                        .collect(toList())
//                );
//    }
//
//    private void doPing() {
//        long requestTime = System.currentTimeMillis();
//        // Beskytter pingables mot mange samtidige/tette requester.
//        // Særlig viktig hvis det tar lang tid å utføre alle pingables
//        synchronized (this) {
//            if (requestTime > lastResultTime) {
//                result = getPingables().stream().map(PING).collect(toList());
//                lastResultTime = System.currentTimeMillis();
//            }
//        }
//    }
//
//    private Collection<Pingable> getPingables() {
//        return appContext.getBeansOfType(Pingable.class).values();
//    }
//
//    private static final Function<Pingable, Pingable.Ping> PING = pingable -> {
//        long startTime = System.currentTimeMillis();
//        var ping = pingable.ping();
//        ping.setResponstid(System.currentTimeMillis() - startTime);
//        if (!ping.erVellykket()) {
//            log.warn("Feil ved SelfTest av {}", ping.getMetadata().getEndepunkt(), ping.getFeil());
//        }
//        return ping;
//    };
//
//    private Integer getAggregertStatus() {
//        boolean harKritiskFeil = result.stream().anyMatch(KRITISK_FEIL);
//        boolean harFeil = result.stream().anyMatch(HAR_FEIL);
//
//        if (harKritiskFeil) {
//            return STATUS_ERROR;
//        } else if (harFeil) {
//            return STATUS_WARNING;
//        }
//        return STATUS_OK;
//    }
//
//    private SelftestEndpoint lagSelftestEndpoint(Pingable.Ping ping) {
//        return new SelftestEndpoint()
//                .setEndpoint(ping.getMetadata().getEndepunkt())
//                .setDescription(ping.getMetadata().getBeskrivelse())
//                .setErrorMessage(ping.getFeilmelding())
//                .setCritical(ping.getMetadata().isKritisk())
//                .setResult(ping.harFeil() ? STATUS_ERROR : STATUS_OK)
//                .setResponseTime(String.format("%dms", ping.getResponstid()))
//                .setStacktrace(ofNullable(ping.getFeil())
//                        .map(ExceptionUtils::getStackTrace)
//                        .orElse(null)
//                );
//    }
//
//}
