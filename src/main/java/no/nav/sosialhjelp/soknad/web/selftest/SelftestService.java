package no.nav.sosialhjelp.soknad.web.selftest;

import no.nav.sosialhjelp.soknad.web.selftest.domain.Selftest;
import no.nav.sosialhjelp.soknad.web.selftest.domain.SelftestEndpoint;
import no.nav.sosialhjelp.soknad.web.utils.MiljoUtils;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

import javax.inject.Inject;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.function.Predicate;

import static java.util.Optional.ofNullable;
import static java.util.stream.Collectors.toList;

@Component
public class SelftestService {

    private static final Predicate<Pingable.Ping> KRITISK_FEIL = ping -> ping.harFeil() && ping.getMetadata().isKritisk();
    private static final Predicate<Pingable.Ping> HAR_FEIL = Pingable.Ping::harFeil;

    public static final int STATUS_OK = 0;
    public static final int STATUS_ERROR = 1;
    public static final int STATUS_WARNING = 2;

    @Inject
    private ApplicationContext appContext;

    protected List<Pingable.Ping> result;
    private volatile long lastResultTime;

    public Selftest lagSelftest() {
        return new Selftest()
                .setApplication(MiljoUtils.getNaisAppName())
                .setVersion(MiljoUtils.getNaisAppImage())
                .setTimestamp(LocalDateTime.now().format(DateTimeFormatter.ISO_DATE_TIME))
                .setAggregateResult(getAggregertStatus())
                .setChecks(result.stream()
                        .map(this::lagSelftestEndpoint)
                        .collect(toList())
                );
    }

    protected Integer getAggregertStatus() {
        boolean harKritiskFeil = result.stream().anyMatch(KRITISK_FEIL);
        boolean harFeil = result.stream().anyMatch(HAR_FEIL);

        if (harKritiskFeil) {
            return STATUS_ERROR;
        } else if (harFeil) {
            return STATUS_WARNING;
        }
        return STATUS_OK;
    }

    private SelftestEndpoint lagSelftestEndpoint(Pingable.Ping ping) {
        return new SelftestEndpoint()
                .setEndpoint(ping.getMetadata().getEndepunkt())
                .setDescription(ping.getMetadata().getBeskrivelse())
                .setErrorMessage(ping.getFeilmelding())
                .setCritical(ping.getMetadata().isKritisk())
                .setResult(ping.harFeil() ? STATUS_ERROR : STATUS_OK)
                .setResponseTime(String.format("%dms", ping.getResponstid()))
                .setStacktrace(ofNullable(ping.getFeil())
                        .map(ExceptionUtils::getStackTrace)
                        .orElse(null)
                );
    }

}
