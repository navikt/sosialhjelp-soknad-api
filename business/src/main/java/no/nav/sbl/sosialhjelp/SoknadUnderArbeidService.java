package no.nav.sbl.sosialhjelp;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import no.nav.sbl.soknadsosialhjelp.json.AdresseMixIn;
import no.nav.sbl.soknadsosialhjelp.soknad.JsonInternalSoknad;
import no.nav.sbl.soknadsosialhjelp.soknad.adresse.JsonAdresse;
import no.nav.sbl.soknadsosialhjelp.soknad.internal.JsonSoknadsmottaker;
import no.nav.sbl.sosialhjelp.domain.SoknadUnderArbeid;
import no.nav.sbl.sosialhjelp.soknadunderbehandling.SoknadUnderArbeidRepository;
import org.slf4j.Logger;
import org.springframework.stereotype.Component;

import javax.inject.Inject;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.format.DateTimeFormatter;

import static no.nav.sbl.soknadsosialhjelp.json.JsonSosialhjelpValidator.ensureValidInternalSoknad;
import static no.nav.sbl.sosialhjelp.pdf.HandlebarContext.SPRAK;
import static org.apache.commons.lang3.StringUtils.isEmpty;
import static org.slf4j.LoggerFactory.getLogger;
import static java.time.LocalDateTime.now;


@Component
public class SoknadUnderArbeidService {
    private static final Logger logger = getLogger(SoknadUnderArbeidService.class);
    private final ObjectMapper mapper;
    private final ObjectWriter writer;

    @Inject
    private SoknadUnderArbeidRepository soknadUnderArbeidRepository;

    public SoknadUnderArbeidService() {
        mapper = new ObjectMapper();
        mapper.addMixIn(JsonAdresse.class, AdresseMixIn.class);
        writer = mapper.writerWithDefaultPrettyPrinter();
    }

    public void settOrgnummerOgNavEnhetsnavnPaSoknad(SoknadUnderArbeid soknadUnderArbeid, String orgnummer, String navEnhetsnavn, String eier) {
        if (soknadUnderArbeid == null) {
            throw new RuntimeException("Søknad under arbeid mangler");
        }

        if (isEmpty(orgnummer) || isEmpty(navEnhetsnavn)) {
            throw new RuntimeException("Informasjon om orgnummer og NAV-enhet mangler");
        } else {
            SoknadUnderArbeid oppdatertSoknadUnderArbeid = oppdaterOrgnummerOgNavEnhetsnavnPaInternalSoknad(soknadUnderArbeid, orgnummer, navEnhetsnavn);
            soknadUnderArbeidRepository.oppdaterSoknadsdata(oppdatertSoknadUnderArbeid, eier);
        }
    }
    
    public void settInnsendingstidspunktPaaJsonInternalSoknad(SoknadUnderArbeid soknadUnderArbeid) {
        JsonInternalSoknad internalSoknad = hentJsonInternalSoknadFraSoknadUnderArbeid(soknadUnderArbeid);
        internalSoknad.getSoknad().setInnsendingstidspunkt(now().format(
                DateTimeFormatter.ofPattern("uuuu-MM-dd'T'HH:mm:ss.SSSX", SPRAK)));
    }

    public JsonInternalSoknad hentJsonInternalSoknadFraSoknadUnderArbeid(SoknadUnderArbeid soknadUnderArbeid) {
        if (soknadUnderArbeid == null || soknadUnderArbeid.getData() == null) {
            return null;
        }
        try {
            return mapper.readValue(soknadUnderArbeid.getData(), JsonInternalSoknad.class);
        } catch (IOException e) {
            logger.error("Kunne ikke finne søknad", e);
            throw new RuntimeException(e);
        }
    }

    SoknadUnderArbeid oppdaterOrgnummerOgNavEnhetsnavnPaInternalSoknad(SoknadUnderArbeid soknadUnderArbeid, String orgnummer, String navEnhetsnavn) {
        final JsonInternalSoknad jsonInternalSoknad = hentJsonInternalSoknadFraSoknadUnderArbeid(soknadUnderArbeid);
        jsonInternalSoknad.setMottaker(new JsonSoknadsmottaker()
                .withOrganisasjonsnummer(orgnummer)
                .withNavEnhetsnavn(navEnhetsnavn));
        final byte[] oppdatertSoknad = mapJsonSoknadInternalTilFil(jsonInternalSoknad);
        return soknadUnderArbeid.withData(oppdatertSoknad);
    }

    byte[] mapJsonSoknadInternalTilFil(JsonInternalSoknad jsonInternalSoknad) {
        try {
            final String internalSoknad = writer.writeValueAsString(jsonInternalSoknad);
            ensureValidInternalSoknad(internalSoknad);
            return internalSoknad.getBytes(StandardCharsets.UTF_8);
        } catch (JsonProcessingException e) {
            logger.error("Kunne ikke konvertere søknadsobjekt til tekststreng", e);
            throw new RuntimeException(e);
        }
    }
}
