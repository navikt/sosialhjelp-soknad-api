package no.nav.sbl.dialogarena.sendsoknad.domain.transformer.sosialhjelp;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import no.nav.sbl.dialogarena.sendsoknad.domain.AlternativRepresentasjon;
import no.nav.sbl.dialogarena.sendsoknad.domain.Vedlegg;
import no.nav.sbl.dialogarena.sendsoknad.domain.WebSoknad;
import no.nav.sbl.dialogarena.sendsoknad.domain.exception.AlleredeHandtertException;
import no.nav.sbl.dialogarena.sendsoknad.domain.transformer.AlternativRepresentasjonTransformer;
import no.nav.sbl.dialogarena.sendsoknad.domain.transformer.AlternativRepresentasjonType;
import no.nav.sbl.soknadsosialhjelp.json.JsonSosialhjelpValidationException;
import no.nav.sbl.soknadsosialhjelp.json.JsonSosialhjelpValidator;
import no.nav.sbl.soknadsosialhjelp.vedlegg.JsonFiler;
import no.nav.sbl.soknadsosialhjelp.vedlegg.JsonVedlegg;
import no.nav.sbl.soknadsosialhjelp.vedlegg.JsonVedleggSpesifikasjon;
import org.slf4j.Logger;

import java.util.*;

import static no.nav.sbl.dialogarena.sendsoknad.domain.Vedlegg.Status.LastetOpp;
import static org.slf4j.LoggerFactory.getLogger;

public class SosialhjelpVedleggTilJson implements AlternativRepresentasjonTransformer {

    private static final Logger logger = getLogger(SosialhjelpVedleggTilJson.class);

    public AlternativRepresentasjon transform(WebSoknad webSoknad) {
        List<JsonVedlegg> vedlegg;
        if (webSoknad.erEttersending()) {
            vedlegg = grupperVedleggForEttersendelse(webSoknad);
        } else {
            vedlegg = grupperVedleggFiler(webSoknad);
        }

        String json;
        
        try {
            JsonVedleggSpesifikasjon jsonObjekt = new JsonVedleggSpesifikasjon().withVedlegg(vedlegg);
            leggPaGarbageDataForAHindreValidering(jsonObjekt);
            json = new ObjectMapper().writeValueAsString(jsonObjekt);
            JsonSosialhjelpValidator.ensureValidVedlegg(json);
        } catch (JsonSosialhjelpValidationException | JsonProcessingException e) {
            logger.error("Kunne ikke generere XML for {}", webSoknad.getBrukerBehandlingId(), e);
            throw new AlleredeHandtertException();
        }

        return new AlternativRepresentasjon()
                .medRepresentasjonsType(getRepresentasjonsType())
                .medMimetype("application/json")
                .medFilnavn("vedlegg.json")
                .medUuid(UUID.randomUUID().toString())
                .medContent(json.getBytes());
    }

    private List<JsonVedlegg> grupperVedleggForEttersendelse(WebSoknad webSoknad) {
        List<Vedlegg> alleVedlegg = webSoknad.getVedlegg();

        Map<String, JsonVedlegg> vedleggMap = new HashMap<>();

        for (Vedlegg vedlegg : alleVedlegg) {
            if (vedlegg.getInnsendingsvalg().erIkke(LastetOpp)) {
                continue;
            }

            String sammensattNavn = vedlegg.getSkjemaNummer() + "|" + vedlegg.getSkjemanummerTillegg();
            JsonVedlegg jsonVedlegg = vedleggMap.get(sammensattNavn);

            if (jsonVedlegg == null) {
                jsonVedlegg = new JsonVedlegg()
                        .withType(vedlegg.getSkjemaNummer())
                        .withTilleggsinfo(vedlegg.getSkjemanummerTillegg())
                        .withFiler(new ArrayList<>());
                vedleggMap.put(sammensattNavn, jsonVedlegg);
            }

            jsonVedlegg.getFiler().add(new JsonFiler()
                    .withFilnavn(vedlegg.getFilnavn())
                    .withSha512(vedlegg.getSha512())
            );


        }

        return new ArrayList<>(vedleggMap.values());
    }

    protected List<JsonVedlegg> grupperVedleggFiler(WebSoknad webSoknad) {
        List<Vedlegg> vedlegg = webSoknad.getVedlegg();

        Map<Long, JsonVedlegg> vedleggMap = new HashMap<>();

        for (Vedlegg v : vedlegg) {
            if (v.getInnsendingsvalg().erIkke(LastetOpp)) {
                continue;
            }

            Long proxyFaktumId = v.getFaktumId();
            Long belopFaktumId = webSoknad.getFaktumMedId(proxyFaktumId + "").getParrentFaktum();

            JsonVedlegg jsonVedlegg = vedleggMap.get(belopFaktumId);
            if (jsonVedlegg == null) {
                jsonVedlegg = new JsonVedlegg()
                        .withType(v.getSkjemaNummer())
                        .withTilleggsinfo(v.getSkjemanummerTillegg())
                        .withFiler(new ArrayList<>());
                vedleggMap.put(belopFaktumId, jsonVedlegg);
            }

            jsonVedlegg.getFiler().add(new JsonFiler()
                    .withFilnavn(v.getFilnavn())
                    .withSha512(v.getSha512())
            );
        }
        return new ArrayList<>(vedleggMap.values());
    }

    private void leggPaGarbageDataForAHindreValidering(JsonVedleggSpesifikasjon jsonVedleggSpesifikasjon) {
        String propname = "garbage" + ((int) (Math.random() * 10e6));
        String forklaring = "Tester støtte for vilkårlige felter";
        jsonVedleggSpesifikasjon.withAdditionalProperty(propname, forklaring);
    }

    @Override
    public AlternativRepresentasjonType getRepresentasjonsType() {
        return AlternativRepresentasjonType.JSON;
    }

    @Override
    public AlternativRepresentasjon apply(WebSoknad webSoknad) {
        return transform(webSoknad);
    }

}
