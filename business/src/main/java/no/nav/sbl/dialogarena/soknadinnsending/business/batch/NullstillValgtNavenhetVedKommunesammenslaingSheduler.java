package no.nav.sbl.dialogarena.soknadinnsending.business.batch;

import no.nav.metrics.MetricsFactory;
import no.nav.metrics.Timer;
import no.nav.sbl.dialogarena.sendsoknad.domain.util.ServiceUtils;
import no.nav.sbl.soknadsosialhjelp.soknad.JsonSoknad;
import no.nav.sbl.soknadsosialhjelp.soknad.internal.JsonSoknadsmottaker;
import no.nav.sbl.sosialhjelp.domain.SoknadUnderArbeid;
import no.nav.sbl.sosialhjelp.soknadunderbehandling.SoknadUnderArbeidRepository;
import org.slf4j.Logger;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import javax.inject.Inject;
import java.time.LocalDateTime;
import java.util.List;

import static org.slf4j.LoggerFactory.getLogger;

/**
 * Denne servicen er laget kun for å nullstille valget av navenheter på
 * påbegynte søknader i forbindelse med kommunesammenslåingen 01.01.2020.
 * For alle søknader påbegynt før 27.12.2020 vil vi slette navenhet-valget
 * fordi mange kommuner vil få nye kommunenummer og navenheter vil få nye
 * organisasjonsnummre.
 */

@Service
public class NullstillValgtNavenhetVedKommunesammenslaingSheduler {
    private static final Logger log = getLogger(NullstillValgtNavenhetVedKommunesammenslaingSheduler.class);

    private static final String KLOKKEN_TO_OM_NATTET_DEN_30_OG_31_DESEMBER = "0 0 02 30-31 12 *";
    private static final String DEBUG_KLOKKE  = "0 */2 * 27 12 *";
    private static final String DEBUG_KLOKKE2 = "* 30-50/5 15 28 12 *";

    @Inject
    private SoknadUnderArbeidRepository soknadUnderArbeidRepository;

    @Scheduled(cron = DEBUG_KLOKKE2)
    public void nullstillNavenhet() {
        if (ServiceUtils.isScheduledTasksDisabled()) {
            log.warn("Scheduler is disabled");
            return;
        }

        int vellykket = 0;
        if (Boolean.valueOf(System.getProperty("sendsoknad.batch.enabled", "true"))) {
            log.info("Starter nullstilling av valgt navenhet på påbegynte søknader");
            Timer batchTimer = MetricsFactory.createTimer("sosialhjelp.debug.nullstill.navenhet");
            batchTimer.start();

            try {
                vellykket = nullstillNavEnhet();
            } catch (RuntimeException e) {
                log.error("Nullstilling av navenhet feilet", e);
                batchTimer.setFailed();
            } finally {
                batchTimer.stop();
                batchTimer.addFieldToReport("vellykket", vellykket);
                batchTimer.report();
                log.info("Nullstilling av navenhet fullført: {} vellykket", vellykket);
            }

        } else {
            log.warn("Batch disabled. Må sette environment property sendsoknad.batch.enabled til true for å sette den på igjen");
        }
    }


    private int nullstillNavEnhet() {
        List<SoknadUnderArbeid> soknadUnderArbeidList = soknadUnderArbeidRepository.hentAlleSoknaderUnderArbeidSiste15Dager();
        log.info("Forsøker å nullstille navenhet på {} påbegynte søknader", soknadUnderArbeidList.size());

        int antallNullstilte = 0;
        for (SoknadUnderArbeid soknad : soknadUnderArbeidList) {
            if (!isMottakerNullstilt(soknad)) {
                log.info("Forsøker å nullstille navenhet på behandlingsId {}", soknad.getBehandlingsId());
                nullstillMottaker(soknad);
                antallNullstilte++;
                log.info("Ferdig med nullstille navenhet på behandlingsId {}", soknad.getBehandlingsId());
            } else {
                log.info("Navenhet er allerede nullstilt på behandlingsid {}", soknad.getBehandlingsId());
            }
        }

        log.info("Nullstilte valgt navenhet på {} av {} soknadUnerArbeid", antallNullstilte, soknadUnderArbeidList.size());
        return antallNullstilte;
    }

    private boolean isMottakerNullstilt(SoknadUnderArbeid soknadUnderArbeid) {
        if (soknadUnderArbeid.getJsonInternalSoknad() == null ) return true;

        JsonSoknadsmottaker internalMottaker = soknadUnderArbeid.getJsonInternalSoknad().getMottaker();
        boolean isInternalMottakerNullstilt = internalMottaker == null || (
                isNullOrEmpty(internalMottaker.getNavEnhetsnavn())
                && isNullOrEmpty(internalMottaker.getOrganisasjonsnummer()));


        no.nav.sbl.soknadsosialhjelp.soknad.JsonSoknadsmottaker soknadMottaker = null;
        if (soknadUnderArbeid.getJsonInternalSoknad().getSoknad() != null) {
            soknadMottaker = soknadUnderArbeid.getJsonInternalSoknad().getSoknad().getMottaker();
        }
        boolean isSoknadMottakerNullstilt = soknadMottaker == null || (
                isNullOrEmpty(soknadMottaker.getEnhetsnummer())
                && isNullOrEmpty(soknadMottaker.getKommunenummer())
                && isNullOrEmpty(soknadMottaker.getNavEnhetsnavn()));

        return isInternalMottakerNullstilt && isSoknadMottakerNullstilt;
    }

    private void nullstillMottaker(SoknadUnderArbeid soknadUnderArbeid) {
        JsonSoknadsmottaker internalMottaker = soknadUnderArbeid.getJsonInternalSoknad().getMottaker();
        if (internalMottaker != null) {
            internalMottaker.withNavEnhetsnavn("").withOrganisasjonsnummer("");
        }

        JsonSoknad soknad = soknadUnderArbeid.getJsonInternalSoknad().getSoknad();
        if (soknad != null) {
            soknad.setMottaker(new no.nav.sbl.soknadsosialhjelp.soknad.JsonSoknadsmottaker());

            if (soknad.getData() != null && soknad.getData().getPersonalia() != null && soknad.getData().getPersonalia().getOppholdsadresse() != null) {
                soknad.getData().getPersonalia().getOppholdsadresse().setAdresseValg(null);
            }
        }

        soknadUnderArbeidRepository.oppdaterSoknadsdata(soknadUnderArbeid, soknadUnderArbeid.getEier());
    }

    private boolean isNullOrEmpty(String string) {
        return string == null || string.equals("");
    }
}