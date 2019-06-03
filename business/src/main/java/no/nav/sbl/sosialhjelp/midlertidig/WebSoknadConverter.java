package no.nav.sbl.sosialhjelp.midlertidig;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import no.nav.sbl.dialogarena.sendsoknad.domain.WebSoknad;
import no.nav.sbl.dialogarena.sendsoknad.domain.oidc.OidcFeatureToggleUtils;
import no.nav.sbl.dialogarena.sendsoknad.domain.transformer.sosialhjelp.FiksMetadataTransformer;
import no.nav.sbl.dialogarena.sendsoknad.domain.transformer.sosialhjelp.InputSource;
import no.nav.sbl.dialogarena.sendsoknad.domain.transformer.sosialhjelp.SosialhjelpVedleggTilJson;
import no.nav.sbl.dialogarena.sendsoknad.domain.transformer.sosialhjelp.json.JsonSoknadConverter;
import no.nav.sbl.dialogarena.soknadinnsending.business.service.soknadservice.EkstraMetadataService;
import no.nav.sbl.dialogarena.soknadsosialhjelp.message.NavMessageSource;
import no.nav.sbl.soknadsosialhjelp.json.AdresseMixIn;
import no.nav.sbl.soknadsosialhjelp.soknad.JsonInternalSoknad;
import no.nav.sbl.soknadsosialhjelp.soknad.adresse.JsonAdresse;
import no.nav.sbl.soknadsosialhjelp.soknad.internal.JsonSoknadsmottaker;
import no.nav.sbl.soknadsosialhjelp.vedlegg.JsonVedlegg;
import no.nav.sbl.soknadsosialhjelp.vedlegg.JsonVedleggSpesifikasjon;
import no.nav.sbl.sosialhjelp.InnsendingService;
import no.nav.sbl.sosialhjelp.domain.SendtSoknad;
import no.nav.sbl.sosialhjelp.domain.SoknadUnderArbeid;
import no.nav.sbl.sosialhjelp.soknadunderbehandling.SoknadUnderArbeidRepository;
import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.springframework.stereotype.Component;

import javax.inject.Inject;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.slf4j.LoggerFactory.getLogger;

@Component
public class WebSoknadConverter {
    private static final Logger logger = getLogger(WebSoknadConverter.class);

    @Inject
    private SoknadUnderArbeidRepository soknadUnderArbeidRepository;
    @Inject
    private NavMessageSource messageSource;
    @Inject
    private EkstraMetadataService ekstraMetadataService;
    @Inject
    private InnsendingService innsendingService;
    private final SosialhjelpVedleggTilJson sosialhjelpVedleggTilJson;
    private final ObjectMapper mapper;
    private final ObjectWriter writer;

    public WebSoknadConverter() {
        sosialhjelpVedleggTilJson = new SosialhjelpVedleggTilJson();
        mapper = new ObjectMapper();
        mapper.addMixIn(JsonAdresse.class, AdresseMixIn.class);
        writer = mapper.writerWithDefaultPrettyPrinter();
    }

    public SoknadUnderArbeid mapWebSoknadTilSoknadUnderArbeid(WebSoknad webSoknad, boolean medVedlegg) {
        if (webSoknad == null) {
            return null;
        }
        return new SoknadUnderArbeid()
                .withVersjon(1L)
                .withBehandlingsId(webSoknad.getBrukerBehandlingId())
                .withTilknyttetBehandlingsId(webSoknad.getBehandlingskjedeId())
                .withEier(webSoknad.getAktoerId())
                .withJsonInternalSoknad(mapWebSoknadTilJsonSoknadInternal(webSoknad, medVedlegg))
                .withInnsendingStatus(webSoknad.getStatus())
                .withOpprettetDato(fraJodaDateTimeTilLocalDateTime(webSoknad.getOpprettetDato()))
                .withSistEndretDato(fraJodaDateTimeTilLocalDateTime(webSoknad.getSistLagret()));
    }

    JsonInternalSoknad mapWebSoknadTilJsonSoknadInternal(WebSoknad webSoknad, boolean medVedlegg) {
        List<JsonVedlegg> jsonVedlegg = new ArrayList<>();

        final String eier = OidcFeatureToggleUtils.getUserId();
        final Optional<SoknadUnderArbeid> soknadNyModell = soknadUnderArbeidRepository.hentSoknad(webSoknad.getBrukerBehandlingId(), eier);
        if (soknadNyModell.get().getJsonInternalSoknad().getVedlegg() != null) {
            jsonVedlegg = soknadNyModell.get().getJsonInternalSoknad().getVedlegg().getVedlegg();
        }

        if (webSoknad.erEttersending()) {
            return new JsonInternalSoknad()
                    .withVedlegg(new JsonVedleggSpesifikasjon().withVedlegg(jsonVedlegg))
                    .withMottaker(settRiktigSoknadsmottaker(webSoknad));
        }
        return new JsonInternalSoknad()
                .withSoknad(JsonSoknadConverter.tilJsonSoknad(new InputSource(webSoknad, messageSource)))
                .withVedlegg(new JsonVedleggSpesifikasjon().withVedlegg(jsonVedlegg))
                .withMottaker(settRiktigSoknadsmottaker(webSoknad));
    }

    public JsonSoknadsmottaker settRiktigSoknadsmottaker(WebSoknad soknad) {
        final Map<String, String> ekstraMetadata = ekstraMetadataService.hentEkstraMetadata(soknad);
        String orgnummer;
        String navEnhetsnavn;
        if (soknad.erEttersending()) {
            SendtSoknad sendtSoknadSomEttersendesPa = innsendingService.finnSendtSoknadForEttersendelse(new SoknadUnderArbeid()
                    .withTilknyttetBehandlingsId(soknad.getBehandlingskjedeId())
                    .withEier(soknad.getAktoerId()));
            orgnummer = sendtSoknadSomEttersendesPa.getOrgnummer();
            navEnhetsnavn = sendtSoknadSomEttersendesPa.getNavEnhetsnavn();
        } else {
            orgnummer = ekstraMetadata.get(FiksMetadataTransformer.FIKS_ORGNR_KEY);
            navEnhetsnavn = ekstraMetadata.get(FiksMetadataTransformer.FIKS_ENHET_KEY);
        }
        return new JsonSoknadsmottaker()
                .withOrganisasjonsnummer(orgnummer)
                .withNavEnhetsnavn(navEnhetsnavn);
    }

    LocalDateTime fraJodaDateTimeTilLocalDateTime(DateTime jodaDateTime) {
        if (jodaDateTime == null) {
            return null;
        }
        return LocalDateTime.ofInstant(Instant.ofEpochMilli(jodaDateTime.toInstant().getMillis()), ZoneId.systemDefault());
    }
}
