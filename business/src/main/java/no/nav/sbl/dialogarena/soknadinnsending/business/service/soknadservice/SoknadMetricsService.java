package no.nav.sbl.dialogarena.soknadinnsending.business.service.soknadservice;

import no.nav.metrics.Event;
import no.nav.sbl.dialogarena.sendsoknad.domain.Vedlegg;
import no.nav.sbl.dialogarena.sendsoknad.domain.kravdialoginformasjon.DagpengerGjenopptakInformasjon;
import no.nav.sbl.dialogarena.sendsoknad.domain.kravdialoginformasjon.DagpengerOrdinaerInformasjon;
import no.nav.sbl.dialogarena.sendsoknad.domain.kravdialoginformasjon.KravdialogInformasjonHolder;
import no.nav.sbl.dialogarena.soknadinnsending.business.db.soknad.SoknadRepository;
import no.nav.sbl.dialogarena.soknadinnsending.business.util.DagpengerUtils;
import org.slf4j.Logger;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import javax.inject.Inject;
import javax.inject.Named;
import java.util.List;
import java.util.Map;

import static org.slf4j.LoggerFactory.getLogger;

@Service
public class SoknadMetricsService {

    private static final Logger logger = getLogger(SoknadMetricsService.class);

    private static final long RAPPORTERINGS_RATE = 15 * 60 * 1000; // hvert kvarter

    @Inject
    private MetricsEventFactory metricsEventFactory;

    @Inject
    @Named("soknadInnsendingRepository")
    private SoknadRepository lokalDb;

    @Inject
    private KravdialogInformasjonHolder kravdialogInformasjonHolder;

    public void startetSoknad(String skjemanummer, boolean erEttersending) {
        rapporterSoknad("soknad.start", skjemanummer, erEttersending);
    }

    public void sendtSoknad(String skjemanummer, boolean erEttersending) {
        rapporterSoknad("soknad.send", skjemanummer, erEttersending);
    }

    public void avbruttSoknad(String skjemanummer, boolean erEttersending) {
        rapporterSoknad("soknad.avbrutt", skjemanummer, erEttersending);
    }

    private void rapporterSoknad(String name, String skjemanummer, boolean erEttersending) {
        String soknadstype = getSoknadstype(skjemanummer, erEttersending);

        Event event = metricsEventFactory.createEvent(name);
        event.addFieldToReport("soknadstype", soknadstype);
        event.report();
    }

    private String getSoknadstype(String skjemanummer, boolean erEttersending) {
        String type = kravdialogInformasjonHolder.hentKonfigurasjon(skjemanummer).getSoknadTypePrefix();
        return (erEttersending ? "ettersending." : "") + type;
    }

    @Scheduled(fixedRate = RAPPORTERINGS_RATE)
    public void rapporterSoknadDatabaseStatus() {
        logger.info("Henter databasestatus for å rapportere metrics");
        Map<String, Integer> statuser = lokalDb.hentDatabaseStatus();

        for (Map.Entry<String, Integer> entry : statuser.entrySet()) {
            logger.info("Databasestatus for {} er {}", entry.getKey(), entry.getValue());
            Event event = metricsEventFactory.createEvent("status.database." + entry.getKey());
            event.addFieldToReport("antall", entry.getValue());
            event.report();
        }
    }

    public void rapporterKompletteOgIkkeKompletteSoknader(List<Vedlegg> innsendteVedlegg, List<Vedlegg> ikkeInnsendteVedlegg, String skjemanummer) {
        if(DagpengerOrdinaerInformasjon.erDagpengerOrdinaer(skjemanummer)
                || DagpengerGjenopptakInformasjon.erDagpengerGjenopptak(skjemanummer)) {

            String skjematype = DagpengerUtils.konverterSkjemanummerTilTittel(skjemanummer);

            Event eventForInnsendteSkjema = metricsEventFactory.createEvent("soknad.innsendteskjema");
            eventForInnsendteSkjema.addFieldToReport("soknad.innsendtskjema.komplett", erSoknadKomplett(ikkeInnsendteVedlegg));
            eventForInnsendteSkjema.addTagToReport("skjematype", skjematype);
            eventForInnsendteSkjema.report();

            rapporterStatusPaaVedlegg(innsendteVedlegg, "soknad.sendtVedlegg", skjematype);
            rapporterStatusPaaVedlegg(ikkeInnsendteVedlegg, "soknad.ikkeSendtVedlegg", skjematype);
        }
    }

    private String erSoknadKomplett(List<Vedlegg> ikkeInnsendteVedlegg) {
        return Boolean.toString(ikkeInnsendteVedlegg.isEmpty());
    }

    private void rapporterStatusPaaVedlegg(List<Vedlegg> vedleggsListe, String eventNavn, String skjematype) {
        if (vedleggsListe.isEmpty()) {
            return;
        }

        for (Vedlegg vedlegg : vedleggsListe) {
            Event eventForInnsendteVedlegg = MetricsFactory.createEvent(eventNavn);

            eventForInnsendteVedlegg.addTagToReport("vedleggskjemanummer", vedlegg.getSkjemaNummer());
            eventForInnsendteVedlegg.addTagToReport("innsendingsvalg", vedlegg.getInnsendingsvalg().name());
            eventForInnsendteVedlegg.addTagToReport("skjematype", skjematype);
            eventForInnsendteVedlegg.report();
        }
    }
}
