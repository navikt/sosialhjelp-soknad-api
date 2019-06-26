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

import static com.flipkart.zjsonpatch.DiffFlags.OMIT_COPY_OPERATION;
import static com.flipkart.zjsonpatch.DiffFlags.OMIT_MOVE_OPERATION;
import static no.nav.sbl.soknadsosialhjelp.json.JsonSosialhjelpValidator.ensureValidSoknad;
import static org.slf4j.LoggerFactory.getLogger;


@Component
public class SoknadUnderArbeidService {
    private static final Logger logger = getLogger(SoknadUnderArbeidService.class);

    private final ObjectMapper mapper;
    private final ObjectWriter writer;
    {
        mapper = new ObjectMapper();
        mapper.addMixIn(JsonAdresse.class, AdresseMixIn.class);
        writer = mapper.writerWithDefaultPrettyPrinter();
    }

    @Inject
    private SoknadUnderArbeidRepository soknadUnderArbeidRepository;
    
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

    public void logDifferences(SoknadUnderArbeid soknadUnderArbeid, SoknadUnderArbeid soknadUnderArbeid_2, String melding) {
        JsonSoknad soknad = soknadUnderArbeid_2.getJsonInternalSoknad().getSoknad();
        JsonSoknad soknadKonvertert = soknadUnderArbeid.getJsonInternalSoknad().getSoknad();
        sortOkonomi(soknad.getData().getOkonomi());
        sortArbeid(soknad.getData().getArbeid());
        sortOkonomi(soknadKonvertert.getData().getOkonomi());
        sortArbeid(soknadKonvertert.getData().getArbeid());
        if (!soknad.equals(soknadKonvertert)){
            try {
                byte[] jsonSoknad = mapJsonSoknadTilFil(soknad);
                byte[] jsonSoknadKonvertert = mapJsonSoknadTilFil(soknadKonvertert);
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

    public byte[] mapJsonSoknadTilFil(JsonSoknad jsonSoknad) {
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
