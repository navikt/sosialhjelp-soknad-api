package no.nav.sbl.sosialhjelp.midlertidig;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import no.nav.sbl.dialogarena.sendsoknad.domain.WebSoknad;
import no.nav.sbl.dialogarena.sendsoknad.domain.transformer.sosialhjelp.*;
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
import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.springframework.stereotype.Component;

import javax.inject.Inject;
import java.nio.charset.StandardCharsets;
import java.time.*;
import java.util.List;
import java.util.Map;

import static no.nav.sbl.soknadsosialhjelp.json.JsonSosialhjelpValidator.ensureValidInternalSoknad;
import static org.slf4j.LoggerFactory.getLogger;

@Component
public class WebSoknadConverter {
    private static final Logger logger = getLogger(WebSoknadConverter.class);

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

    public SoknadUnderArbeid mapWebSoknadTilSoknadUnderArbeid(WebSoknad webSoknad) {
        if (webSoknad == null) {
            return null;
        }
        return new SoknadUnderArbeid()
                .withVersjon(1L)
                .withBehandlingsId(webSoknad.getBrukerBehandlingId())
                .withTilknyttetBehandlingsId(webSoknad.getBehandlingskjedeId())
                .withEier(webSoknad.getAktoerId())
                .withData(webSoknadTilJson(webSoknad))
                .withInnsendingStatus(webSoknad.getStatus())
                .withOpprettetDato(fraJodaDateTimeTilLocalDateTime(webSoknad.getOpprettetDato()))
                .withSistEndretDato(fraJodaDateTimeTilLocalDateTime(webSoknad.getSistLagret()));
    }

    byte[] webSoknadTilJson(WebSoknad webSoknad) {
        final JsonInternalSoknad jsonInternalSoknad = mapWebSoknadTilJsonSoknadInternal(webSoknad);
        return mapJsonSoknadInternalTilFil(jsonInternalSoknad);
    }

    public JsonInternalSoknad mapWebSoknadTilJsonSoknadInternal(WebSoknad webSoknad) {
        final List<JsonVedlegg> jsonVedlegg = sosialhjelpVedleggTilJson.opprettJsonVedleggFraWebSoknad(webSoknad);
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

    JsonSoknadsmottaker settRiktigSoknadsmottaker(WebSoknad soknad) {
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

    byte[] mapJsonSoknadInternalTilFil(JsonInternalSoknad jsonInternalSoknad) {
        try {
            final String internalSoknad = writer.writeValueAsString(jsonInternalSoknad);
            ensureValidInternalSoknad(internalSoknad);
            return internalSoknad.getBytes(StandardCharsets.UTF_8);
        } catch (JsonProcessingException e) {
            logger.error("Kunne ikke konvertere søknadsobjekt til tekststreng", e);
        }
        return null;
    }

    LocalDateTime fraJodaDateTimeTilLocalDateTime(DateTime jodaDateTime) {
        if (jodaDateTime == null) {
            return null;
        }
        return LocalDateTime.ofInstant(Instant.ofEpochMilli(jodaDateTime.toInstant().getMillis()), ZoneId.systemDefault());
    }
}
