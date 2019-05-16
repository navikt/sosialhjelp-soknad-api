package no.nav.sbl.dialogarena.sendsoknad.domain.transformer.sosialhjelp;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import no.nav.sbl.dialogarena.sendsoknad.domain.AlternativRepresentasjon;
import no.nav.sbl.dialogarena.sendsoknad.domain.Faktum;
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
import java.util.stream.Collectors;

import static java.util.Map.Entry.comparingByValue;
import static java.util.stream.Collectors.toMap;
import static no.nav.sbl.dialogarena.sendsoknad.domain.Vedlegg.Status.LastetOpp;
import static no.nav.sbl.dialogarena.sendsoknad.domain.Vedlegg.Status.VedleggAlleredeSendt;
import static no.nav.sbl.dialogarena.sendsoknad.domain.Vedlegg.Status.VedleggKreves;
import static org.slf4j.LoggerFactory.getLogger;

public class SosialhjelpVedleggTilJson implements AlternativRepresentasjonTransformer {

    private static final Logger logger = getLogger(SosialhjelpVedleggTilJson.class);

    public JsonVedleggSpesifikasjon toJsonVedleggSpesifikasjon(WebSoknad webSoknad) {
        final List<JsonVedlegg> vedlegg = opprettJsonVedleggFraWebSoknad(webSoknad);
        return new JsonVedleggSpesifikasjon().withVedlegg(vedlegg);
    }

    public AlternativRepresentasjon transform(WebSoknad webSoknad) {
        String json;
        try {
            JsonVedleggSpesifikasjon jsonObjekt = toJsonVedleggSpesifikasjon(webSoknad);
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

    public List<JsonVedlegg> opprettJsonVedleggFraWebSoknad(WebSoknad webSoknad) {
        List<JsonVedlegg> vedlegg;
        if (webSoknad.erEttersending()) {
            vedlegg = grupperVedleggForEttersendelse(webSoknad);
        } else {
            vedlegg = grupperVedleggFiler(webSoknad);
        }
        return vedlegg;
    }

    private List<JsonVedlegg> grupperVedleggForEttersendelse(WebSoknad webSoknad) {
        List<Vedlegg> alleVedlegg = webSoknad.getVedlegg();

        Map<String, JsonVedlegg> vedleggMap = new HashMap<>();
        Map<String, Map<String, Long>> jsonVedleggMapMedTidspunkt = new HashMap<>();
        //type, <sha512, Tidspunkt>

        for (Vedlegg vedlegg : alleVedlegg) {
            if (vedlegg.getInnsendingsvalg().erIkke(LastetOpp) && vedlegg.getInnsendingsvalg().erIkke(VedleggAlleredeSendt)
                    && vedlegg.getInnsendingsvalg().erIkke(VedleggKreves)) {
                continue;
            }

            String sammensattNavn = vedlegg.getSkjemaNummer() + "|" + vedlegg.getSkjemanummerTillegg();
            JsonVedlegg jsonVedlegg = vedleggMap.get(sammensattNavn);

            if (jsonVedlegg == null) {
                jsonVedlegg = new JsonVedlegg()
                        .withType(vedlegg.getSkjemaNummer())
                        .withTilleggsinfo(vedlegg.getSkjemanummerTillegg())
                        .withStatus(vedlegg.getInnsendingsvalg().name())
                        .withFiler(new ArrayList<>());
                vedleggMap.put(sammensattNavn, jsonVedlegg);
            }

            leggTilFilOgSorter(jsonVedleggMapMedTidspunkt, vedlegg, jsonVedlegg);
        }

        return new ArrayList<>(vedleggMap.values());
    }

    List<JsonVedlegg> grupperVedleggFiler(WebSoknad webSoknad) {
        List<Vedlegg> vedlegg = webSoknad.getVedlegg();

        Map<Long, JsonVedlegg> vedleggMap = new HashMap<>();
        Map<String, Map<String, Long>> jsonVedleggMapMedTidspunkt = new HashMap<>();
        //type, <sha512, Tidspunkt>

        for (Vedlegg v : vedlegg) {
            if (v.getInnsendingsvalg().erIkke(LastetOpp) && v.getInnsendingsvalg().erIkke(VedleggAlleredeSendt)
                    && v.getInnsendingsvalg().erIkke(VedleggKreves)) {
                continue;
            }

            Long proxyFaktumId = v.getFaktumId();
            if (proxyFaktumId == null) {
                throw new NullPointerException("vedlegg.getFaktumId==null");
            }
            Faktum faktumMedId = webSoknad.getFaktumMedId(String.valueOf(proxyFaktumId));
            if (faktumMedId == null) {
                String faktumIder = webSoknad.getFakta().stream().map(Faktum::getKey).collect(Collectors.joining(", "));
                throw new NullPointerException(String.format("faktumMedId==null proxyFaktumId=%s tilgjengelige faktumider=%s", proxyFaktumId, faktumIder));
            }
            Long belopFaktumId = faktumMedId.getParrentFaktum();

            JsonVedlegg jsonVedlegg = vedleggMap.get(belopFaktumId);
            if (jsonVedlegg == null) {
                jsonVedlegg = new JsonVedlegg()
                        .withType(v.getSkjemaNummer())
                        .withTilleggsinfo(v.getSkjemanummerTillegg())
                        .withStatus(v.getInnsendingsvalg().name())
                        .withFiler(new ArrayList<>());
                vedleggMap.put(belopFaktumId, jsonVedlegg);
            }

            leggTilFilOgSorter(jsonVedleggMapMedTidspunkt, v, jsonVedlegg);
        }
        return new ArrayList<>(vedleggMap.values());
    }

    private void leggTilFilOgSorter(Map<String, Map<String, Long>> jsonVedleggMapMedTidspunkt, Vedlegg v, JsonVedlegg jsonVedlegg) {
        if (v.getInnsendingsvalg().er(LastetOpp)) {
            String sammensattNavn = v.getSkjemaNummer() + "|" + v.getSkjemanummerTillegg();
            Map<String, Long> tidspunkter = jsonVedleggMapMedTidspunkt.get(sammensattNavn);
            if (tidspunkter == null) {
                jsonVedleggMapMedTidspunkt.put(sammensattNavn, new HashMap<>());
                tidspunkter = jsonVedleggMapMedTidspunkt.get(sammensattNavn);
            }
            tidspunkter.put(v.getSha512(), v.getOpprettetDato());

            jsonVedlegg.getFiler().add(new JsonFiler()
                    .withFilnavn(v.getFilnavn())
                    .withSha512(v.getSha512()));

            List<JsonFiler> jsonFilerSortert = new ArrayList<>(jsonVedlegg.getFiler().size());

            Map<String, Long> sortedTidspunkter = tidspunkter
                    .entrySet()
                    .stream()
                    .sorted(comparingByValue())
                    .collect(toMap(Map.Entry::getKey, Map.Entry::getValue, (e1, e2) -> e2,
                            LinkedHashMap::new));

            for (Map.Entry<String, Long> entry : sortedTidspunkter.entrySet()) {
                jsonFilerSortert.add(hentFilSomMatcherTidspunkt(jsonVedlegg, entry));
            }

            jsonVedlegg.setFiler(jsonFilerSortert);
        }
    }

    private JsonFiler hentFilSomMatcherTidspunkt(JsonVedlegg jsonVedlegg, Map.Entry<String, Long> entry) {
        return jsonVedlegg.getFiler().stream().filter(jsonFiler -> jsonFiler.getSha512().equals(entry.getKey())).findFirst().get();
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
