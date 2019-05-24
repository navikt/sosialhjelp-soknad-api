package no.nav.sbl.sosialhjelp;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import com.flipkart.zjsonpatch.DiffFlags;
import com.flipkart.zjsonpatch.JsonDiff;
import no.nav.sbl.soknadsosialhjelp.json.AdresseMixIn;
import no.nav.sbl.soknadsosialhjelp.soknad.JsonSoknad;
import no.nav.sbl.soknadsosialhjelp.soknad.adresse.JsonAdresse;
import no.nav.sbl.soknadsosialhjelp.soknad.arbeid.JsonArbeid;
import no.nav.sbl.soknadsosialhjelp.soknad.arbeid.JsonArbeidsforhold;
import no.nav.sbl.soknadsosialhjelp.soknad.internal.JsonSoknadsmottaker;
import no.nav.sbl.soknadsosialhjelp.soknad.okonomi.JsonOkonomi;
import no.nav.sbl.soknadsosialhjelp.soknad.okonomi.opplysning.JsonOkonomiOpplysningUtbetaling;
import no.nav.sbl.soknadsosialhjelp.soknad.okonomi.opplysning.JsonOkonomiOpplysningUtgift;
import no.nav.sbl.soknadsosialhjelp.soknad.okonomi.opplysning.JsonOkonomibekreftelse;
import no.nav.sbl.soknadsosialhjelp.soknad.okonomi.oversikt.JsonOkonomioversiktFormue;
import no.nav.sbl.soknadsosialhjelp.soknad.okonomi.oversikt.JsonOkonomioversiktInntekt;
import no.nav.sbl.soknadsosialhjelp.soknad.okonomi.oversikt.JsonOkonomioversiktUtgift;
import no.nav.sbl.sosialhjelp.domain.SoknadUnderArbeid;
import no.nav.sbl.sosialhjelp.soknadunderbehandling.SoknadUnderArbeidRepository;
import org.slf4j.Logger;
import org.springframework.stereotype.Component;

import javax.inject.Inject;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.Comparator;
import java.util.EnumSet;
import java.util.Optional;

import static com.flipkart.zjsonpatch.DiffFlags.OMIT_COPY_OPERATION;
import static com.flipkart.zjsonpatch.DiffFlags.OMIT_MOVE_OPERATION;
import static no.nav.sbl.soknadsosialhjelp.json.JsonSosialhjelpValidator.ensureValidSoknad;
import static org.apache.commons.lang3.StringUtils.isEmpty;
import static org.slf4j.LoggerFactory.getLogger;


@Component
public class SoknadUnderArbeidService {
    private static final Logger logger = getLogger(SoknadUnderArbeidService.class);

    @Inject
    private SoknadUnderArbeidRepository soknadUnderArbeidRepository;

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
        soknadUnderArbeid.getJsonInternalSoknad().getSoknad().setInnsendingstidspunkt(nowWithMilliseconds());
        soknadUnderArbeidRepository.oppdaterSoknadsdata(soknadUnderArbeid, soknadUnderArbeid.getEier());
    }

    private String nowWithMilliseconds() {
        final OffsetDateTime now = OffsetDateTime.now(ZoneOffset.UTC);
        if (now.getNano() == 0) {
            return now.plusNanos(1_000_000).toString();
        }
        return now.toString();
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

    SoknadUnderArbeid oppdaterOrgnummerOgNavEnhetsnavnPaInternalSoknad(SoknadUnderArbeid soknadUnderArbeid, String orgnummer, String navEnhetsnavn) {
        soknadUnderArbeid.getJsonInternalSoknad().setMottaker(new JsonSoknadsmottaker()
                .withOrganisasjonsnummer(orgnummer)
                .withNavEnhetsnavn(navEnhetsnavn));
        return soknadUnderArbeid;
    }

    public void logDifferences(SoknadUnderArbeid soknadUnderArbeid, SoknadUnderArbeid soknadUnderArbeid_2, String melding) {
        ObjectMapper mapper = new ObjectMapper();
        mapper.addMixIn(JsonAdresse.class, AdresseMixIn.class);
        ObjectWriter writer = mapper.writerWithDefaultPrettyPrinter();
        JsonSoknad soknad = soknadUnderArbeid_2.getJsonInternalSoknad().getSoknad();
        JsonSoknad soknadKonvertert = soknadUnderArbeid.getJsonInternalSoknad().getSoknad();
        sortOkonomi(soknad.getData().getOkonomi());
        sortArbeid(soknad.getData().getArbeid());
        sortOkonomi(soknadKonvertert.getData().getOkonomi());
        sortArbeid(soknadKonvertert.getData().getArbeid());
        if (!soknad.equals(soknadKonvertert)){
            try {
                byte[] jsonSoknad = mapJsonSoknadTilFil(soknad, writer);
                byte[] jsonSoknadKonvertert = mapJsonSoknadTilFil(soknadKonvertert, writer);
                JsonNode beforeNode = mapper.readTree(jsonSoknadKonvertert);
                JsonNode afterNode = mapper.readTree(jsonSoknad);
                EnumSet<DiffFlags> flags = EnumSet.of(OMIT_MOVE_OPERATION, OMIT_COPY_OPERATION);
                JsonNode patch = JsonDiff.asJson(beforeNode, afterNode, flags);
                String diffs = patch.toString();
                if (!"[]".equals(diffs)){
                    logger.info(melding + diffs);
                }
            } catch (IOException ignored) { }
        }
    }

    private void sortArbeid(JsonArbeid arbeid) {
        arbeid.getForhold().sort(Comparator.comparing(JsonArbeidsforhold::getArbeidsgivernavn));
    }

    public void sortOkonomi(JsonOkonomi okonomi) {
        okonomi.getOpplysninger().getBekreftelse().sort(Comparator.comparing(JsonOkonomibekreftelse::getType));
        okonomi.getOpplysninger().getUtbetaling().sort(Comparator.comparing(JsonOkonomiOpplysningUtbetaling::getType));
        okonomi.getOpplysninger().getUtgift().sort(Comparator.comparing(JsonOkonomiOpplysningUtgift::getType));
        okonomi.getOversikt().getInntekt().sort(Comparator.comparing(JsonOkonomioversiktInntekt::getType));
        okonomi.getOversikt().getUtgift().sort(Comparator.comparing(JsonOkonomioversiktUtgift::getType));
        okonomi.getOversikt().getFormue().sort(Comparator.comparing(JsonOkonomioversiktFormue::getType));
    }

    private byte[] mapJsonSoknadTilFil(JsonSoknad jsonSoknad, ObjectWriter writer) {
        try {
            final String soknad = writer.writeValueAsString(jsonSoknad);
            ensureValidSoknad(soknad);
            return soknad.getBytes(StandardCharsets.UTF_8);
        } catch (JsonProcessingException e) {
            logger.error("Kunne ikke konvertere soknad.json til tekststreng", e);
            throw new RuntimeException(e);
        }
    }

}
