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
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.Optional;

import static no.nav.sbl.soknadsosialhjelp.json.JsonSosialhjelpValidator.ensureValidInternalSoknad;
import static org.apache.commons.lang3.StringUtils.isEmpty;
import static org.slf4j.LoggerFactory.getLogger;


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
    
    public void settInnsendingstidspunktPaSoknad(SoknadUnderArbeid soknadUnderArbeid) {
        if (soknadUnderArbeid == null) {
            throw new RuntimeException("Søknad under arbeid mangler");
        }
        if (soknadUnderArbeid.erEttersendelse()){
            return;
        }
        soknadUnderArbeid.getJsonInternalSoknad().getSoknad().setInnsendingstidspunkt(OffsetDateTime.now(ZoneOffset.UTC).toString());
        soknadUnderArbeidRepository.oppdaterSoknadsdata(soknadUnderArbeid, soknadUnderArbeid.getEier());
    }

    public SoknadUnderArbeid oppdaterEllerOpprettSoknadUnderArbeid(SoknadUnderArbeid soknadUnderArbeid, String eier) {
        if (soknadUnderArbeid == null) {
            throw new RuntimeException("Søknad under arbeid mangler");
        } else if (isEmpty(soknadUnderArbeid.getBehandlingsId())) {
            throw new RuntimeException("Søknad under arbeid mangler behandlingsId");
        }
        Optional<SoknadUnderArbeid> soknadUnderArbeidOptional = soknadUnderArbeidRepository.hentSoknad(soknadUnderArbeid.getBehandlingsId(), eier);
        if (soknadUnderArbeidOptional.isPresent()) {
            SoknadUnderArbeid soknadUnderArbeidFraDB = soknadUnderArbeidOptional.get();
            soknadUnderArbeidFraDB.withJsonInternalSoknad(soknadUnderArbeid.getJsonInternalSoknad());
            soknadUnderArbeidRepository.oppdaterSoknadsdata(soknadUnderArbeidFraDB, eier);
        } else {
            soknadUnderArbeidRepository.opprettSoknad(soknadUnderArbeid, eier);
        }
        Optional<SoknadUnderArbeid> oppdatertSoknadUnderArbeidOptional = soknadUnderArbeidRepository.hentSoknad(soknadUnderArbeid.getBehandlingsId(), eier);
        if (!oppdatertSoknadUnderArbeidOptional.isPresent()) {
            throw new RuntimeException("Kunne ikke hente oppdatert søknad under arbeid fra database");
        }
        return oppdatertSoknadUnderArbeidOptional.get();
    }

    /*public JsonInternalSoknad hentJsonInternalSoknadFraSoknadUnderArbeid(SoknadUnderArbeid soknadUnderArbeid) {
        if (soknadUnderArbeid == null || soknadUnderArbeid.getJsonInternalSoknad() == null) {
            return null;
        }
        try {
            return mapper.readValue(soknadUnderArbeid.getData(), JsonInternalSoknad.class);
        } catch (IOException e) {
            logger.error("Kunne ikke finne søknad", e);
            throw new RuntimeException(e);
        }
    }*/

    public JsonInternalSoknad mapDataToJsonInternalSoknad(byte[] data){
        if (data == null){
            return null;
        }
        try {
            return mapper.readValue(data, JsonInternalSoknad.class);
        } catch (IOException e) {
            logger.error("Kunne ikke finne søknad", e);
            throw new RuntimeException(e);
        }
    }

    SoknadUnderArbeid oppdaterOrgnummerOgNavEnhetsnavnPaInternalSoknad(SoknadUnderArbeid soknadUnderArbeid, String orgnummer, String navEnhetsnavn) {
        soknadUnderArbeid.getJsonInternalSoknad().setMottaker(new JsonSoknadsmottaker()
                .withOrganisasjonsnummer(orgnummer)
                .withNavEnhetsnavn(navEnhetsnavn));
        return soknadUnderArbeid;
    }

    public byte[] mapJsonSoknadInternalTilFil(JsonInternalSoknad jsonInternalSoknad) {
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
