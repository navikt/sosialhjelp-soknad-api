package no.nav.sbl.dialogarena.soknadinnsending.business.service.soknadservice;

import com.fasterxml.jackson.databind.ObjectMapper;
import no.nav.modig.core.exception.ApplicationException;
import no.nav.sbl.dialogarena.sendsoknad.domain.PersonAlder;
import no.nav.sbl.dialogarena.sendsoknad.domain.mock.MockUtils;
import no.nav.sbl.dialogarena.sendsoknad.domain.oidc.OidcFeatureToggleUtils;
import no.nav.sbl.dialogarena.soknadinnsending.business.batch.oppgave.OppgaveHandterer;
import no.nav.sbl.dialogarena.soknadinnsending.business.domain.SoknadMetadata;
import no.nav.sbl.dialogarena.soknadinnsending.business.service.HenvendelseService;
import no.nav.sbl.dialogarena.soknadinnsending.business.service.digisosapi.DigisosApiService;
import no.nav.sbl.soknadsosialhjelp.json.JsonSosialhjelpObjectMapper;
import no.nav.sbl.soknadsosialhjelp.vedlegg.JsonVedlegg;
import no.nav.sbl.sosialhjelp.domain.SoknadUnderArbeid;
import no.nav.sbl.sosialhjelp.domain.Vedleggstatus;
import no.nav.sbl.sosialhjelp.soknadunderbehandling.SoknadUnderArbeidRepository;
import org.slf4j.Logger;
import org.springframework.stereotype.Component;

import javax.inject.Inject;
import java.util.List;
import java.util.stream.Collectors;

import static no.nav.sbl.dialogarena.soknadinnsending.business.util.JsonVedleggUtils.getVedleggFromInternalSoknad;
import static org.slf4j.LoggerFactory.getLogger;

@Component
public class FiksIoSoknadService {

    private static final Logger logger = getLogger(FiksIoSoknadService.class);
    private final ObjectMapper objectMapper = JsonSosialhjelpObjectMapper.createObjectMapper();
    @Inject
    private HenvendelseService henvendelseService;

    @Inject
    private DigisosApiService digisosApiService;

    @Inject
    private OppgaveHandterer oppgaveHandterer;

    @Inject
    private SoknadMetricsService soknadMetricsService;


    @Inject
    private SoknadUnderArbeidRepository soknadUnderArbeidRepository;


    public void sendSoknad(String behandlingsId) {
        String eier = OidcFeatureToggleUtils.getUserId();
        SoknadUnderArbeid soknadUnderArbeid = soknadUnderArbeidRepository.hentSoknad(behandlingsId, eier);
        if (soknadUnderArbeid.erEttersendelse() && getVedleggFromInternalSoknad(soknadUnderArbeid).isEmpty()) {
            logger.error("Kan ikke sende inn ettersendingen med ID {0} uten å ha lastet opp vedlegg", behandlingsId);
            throw new ApplicationException("Kan ikke sende inn ettersendingen uten å ha lastet opp vedlegg");
        }
        logger.info("Starter innsending av søknad med behandlingsId {}", behandlingsId);

        SoknadMetadata.VedleggMetadataListe vedlegg = convertToVedleggMetadataListe(soknadUnderArbeid);
        henvendelseService.oppdaterMetadataVedAvslutningAvSoknad(behandlingsId, vedlegg, soknadUnderArbeid);
        oppgaveHandterer.leggTilOppgave(behandlingsId, eier);

        // send sokand    soknadUnderArbeid
        if (digisosApiService.hentKommuneInfo("0301").getKanMottaSoknader()) {
            String kommunenummer = soknadUnderArbeid.getJsonInternalSoknad().getSoknad().getMottaker().getKommunenummer();
            digisosApiService.sendOgKrypter(digisosApiService.lagDokumentListe(soknadUnderArbeid), kommunenummer, behandlingsId, "token");
        }

        soknadMetricsService.sendtSoknad(soknadUnderArbeid.erEttersendelse());
        if (!soknadUnderArbeid.erEttersendelse() && !MockUtils.isTillatMockRessurs()) {
            logAlderTilKibana(eier);
        }
    }


    private SoknadMetadata.VedleggMetadataListe convertToVedleggMetadataListe(SoknadUnderArbeid soknadUnderArbeid) {
        final List<JsonVedlegg> jsonVedleggs = getVedleggFromInternalSoknad(soknadUnderArbeid);
        SoknadMetadata.VedleggMetadataListe vedlegg = new SoknadMetadata.VedleggMetadataListe();

        vedlegg.vedleggListe = jsonVedleggs.stream().map(FiksIoSoknadService::mapJsonVedleggToVedleggMetadata).collect(Collectors.toList());

        return vedlegg;
    }

    private static SoknadMetadata.VedleggMetadata mapJsonVedleggToVedleggMetadata(JsonVedlegg jsonVedlegg) {
        SoknadMetadata.VedleggMetadata m = new SoknadMetadata.VedleggMetadata();
        m.skjema = jsonVedlegg.getType();
        m.tillegg = jsonVedlegg.getTilleggsinfo();
        m.filnavn = jsonVedlegg.getType();
        m.status = Vedleggstatus.valueOf(jsonVedlegg.getStatus());
        return m;
    }

    private static void logAlderTilKibana(String eier) {
        int age = new PersonAlder(eier).getAlder();
        if (age > 0 && age < 30) {
            logger.info("DIGISOS-1164: UNDER30 - Soknad sent av bruker med alder: " + age);
        } else {
            logger.info("DIGISOS-1164: OVER30 - Soknad sent av bruker med alder:" + age);
        }
    }
}