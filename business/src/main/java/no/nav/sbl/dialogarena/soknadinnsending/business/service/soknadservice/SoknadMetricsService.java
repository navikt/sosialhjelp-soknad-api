package no.nav.sbl.dialogarena.soknadinnsending.business.service.soknadservice;

import no.nav.metrics.Event;
import no.nav.metrics.MetricsFactory;
import no.nav.sbl.dialogarena.sendsoknad.domain.Vedlegg;
import no.nav.sbl.dialogarena.sendsoknad.domain.kravdialoginformasjon.DagpengerGjenopptakInformasjon;
import no.nav.sbl.dialogarena.sendsoknad.domain.kravdialoginformasjon.DagpengerOrdinaerInformasjon;
import no.nav.sbl.dialogarena.sendsoknad.domain.kravdialoginformasjon.KravdialogInformasjonHolder;
import no.nav.sbl.dialogarena.soknadinnsending.business.db.soknad.SoknadRepository;
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

        Event event = MetricsFactory.createEvent(name);
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
            Event event = MetricsFactory.createEvent("status.database." + entry.getKey());
            event.addFieldToReport("antall", entry.getValue());
            event.report();
        }
    }

    public void rapporterKompletteOgIkkeKompletteSoknader(List<Vedlegg> ikkeInnsendteVedlegg, String skjemanummer) {

        if(DagpengerOrdinaerInformasjon.erDagpengerOrdinaer(skjemanummer)
                || DagpengerGjenopptakInformasjon.erDagpengerGjenopptak(skjemanummer)) {

            Event eventForInnsendteSkjema = MetricsFactory.createEvent("soknad.innsendteskjema");

            if (ikkeInnsendteVedlegg.isEmpty()) {
                eventForInnsendteSkjema.addFieldToReport("soknad.innsendtskjema.komplett", "true");
            } else {
                eventForInnsendteSkjema.addFieldToReport("soknad.innsendtskjema.komplett", "false");

                for (Vedlegg vedlegg : ikkeInnsendteVedlegg) {
                    Event eventForIkkeInnsendteVedlegg = MetricsFactory.createEvent("soknad.ikkeSendtVedlegg");

                    eventForIkkeInnsendteVedlegg.addTagToReport("vedleggskjemanummer", vedlegg.getSkjemaNummer());
                    eventForIkkeInnsendteVedlegg.addTagToReport("innsendingsvalg", vedlegg.getInnsendingsvalg().name());
                    eventForIkkeInnsendteVedlegg.addTagToReport("skjematype", konverterSkjemanummerTilTittel(skjemanummer));
                    eventForIkkeInnsendteVedlegg.report();
                }
            }
            logger.info(skjemanummer);
            eventForInnsendteSkjema.addTagToReport("skjematype", konverterSkjemanummerTilTittel(skjemanummer));
            eventForInnsendteSkjema.report();
        }
    }

    public String konverterSkjemanummerTilTittel(String skjemanummer) {
        switch (skjemanummer) {
            case "NAV 04-01.03":
                return "Ordinær";
            case "NAV 04-01.04":
                return "OrdinærPerm";
            case "NAV 04-16.03":
                return "Gjenopptak";
            case "NAV 04-16.04":
                return "GjenopptakPerm";
            default:
                return "Annet";
        }
    }
}
