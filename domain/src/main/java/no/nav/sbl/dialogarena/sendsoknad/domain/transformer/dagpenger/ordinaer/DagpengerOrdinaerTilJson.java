package no.nav.sbl.dialogarena.sendsoknad.domain.transformer.dagpenger.ordinaer;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.joda.JodaModule;
import no.nav.sbl.dialogarena.sendsoknad.domain.AlternativRepresentasjon;
import no.nav.sbl.dialogarena.sendsoknad.domain.WebSoknad;
import no.nav.sbl.dialogarena.sendsoknad.domain.exception.AlleredeHandtertException;
import no.nav.sbl.dialogarena.sendsoknad.domain.transformer.AlternativRepresentasjonTransformer;
import no.nav.sbl.dialogarena.sendsoknad.domain.transformer.AlternativRepresentasjonType;
import org.slf4j.Logger;

import java.util.UUID;

import static org.slf4j.LoggerFactory.getLogger;

public class DagpengerOrdinaerTilJson implements AlternativRepresentasjonTransformer {

    private static final Logger logger = getLogger(DagpengerOrdinaerTilJson.class);

    public DagpengerOrdinaerTilJson() {
    }

    @Override
    public AlternativRepresentasjonType getRepresentasjonsType() {
        return AlternativRepresentasjonType.JSON;
    }

    @Override
    public AlternativRepresentasjon apply(WebSoknad webSoknad) {
        JsonDagpengerSoknad jsonSoknad = transform(webSoknad);

        try {
            ObjectMapper mapper = new ObjectMapper();
            mapper.registerModule(new JodaModule());
            mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
            String json = mapper.writeValueAsString(jsonSoknad);

            return new AlternativRepresentasjon()
                    .medRepresentasjonsType(getRepresentasjonsType())
                    .medMimetype("application/json")
                    .medFilnavn("DagpengerOrdinaer.json")
                    .medUuid(UUID.randomUUID().toString())
                    .medContent(json.getBytes());

        } catch (JsonProcessingException e) {
            logger.error("Kunne ikke generere soknads-JSON for {}", webSoknad.getBrukerBehandlingId(), e);
            throw new AlleredeHandtertException();
        }
    }


    protected JsonDagpengerSoknad transform(WebSoknad webSoknad) {
        return JsonDagpengerSoknadConverter.tilJsonSoknad(webSoknad);
        }
    }
