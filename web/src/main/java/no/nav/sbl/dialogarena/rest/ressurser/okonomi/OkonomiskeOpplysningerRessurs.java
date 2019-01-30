package no.nav.sbl.dialogarena.rest.ressurser.okonomi;

import no.nav.metrics.aspects.Timed;
import no.nav.modig.core.context.SubjectHandler;
import no.nav.sbl.dialogarena.rest.ressurser.LegacyHelper;
import no.nav.sbl.dialogarena.rest.ressurser.VedleggFrontend;
import no.nav.sbl.soknadsosialhjelp.soknad.JsonInternalSoknad;
import no.nav.sbl.soknadsosialhjelp.soknad.okonomi.JsonOkonomi;
import no.nav.sbl.soknadsosialhjelp.soknad.okonomi.JsonOkonomiopplysninger;
import no.nav.sbl.soknadsosialhjelp.soknad.okonomi.opplysning.JsonOkonomiOpplysningUtbetaling;
import no.nav.sbl.soknadsosialhjelp.soknad.okonomi.opplysning.JsonOkonomiOpplysningUtgift;
import no.nav.sbl.soknadsosialhjelp.soknad.okonomi.oversikt.JsonOkonomioversiktFormue;
import no.nav.sbl.soknadsosialhjelp.soknad.okonomi.oversikt.JsonOkonomioversiktInntekt;
import no.nav.sbl.soknadsosialhjelp.soknad.okonomi.oversikt.JsonOkonomioversiktUtgift;
import no.nav.sbl.soknadsosialhjelp.vedlegg.JsonFiler;
import no.nav.sbl.soknadsosialhjelp.vedlegg.JsonVedlegg;
import org.springframework.stereotype.Controller;

import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static javax.ws.rs.core.MediaType.APPLICATION_JSON;

@Controller
@Path("/soknader/{behandlingsId}/okonomiskeOpplysninger")
@Timed
@Produces(APPLICATION_JSON)
public class OkonomiskeOpplysningerRessurs {

    final static private Map<String,String> tilleggsinfoToJsonType = new HashMap<String,String>();
    static {
        tilleggsinfoToJsonType.put("aksjer", "verdipapirer");
        tilleggsinfoToJsonType.put("annetbarnutgift", "annenBarneutgift");
        tilleggsinfoToJsonType.put("annetboutgift", "annenBoutgift");
        tilleggsinfoToJsonType.put("annetinntekter", "annen");
        tilleggsinfoToJsonType.put("annetverdi", "annet");
        tilleggsinfoToJsonType.put("barnehage", "barnehage");
        tilleggsinfoToJsonType.put("betaler", "barnebidrag");
        tilleggsinfoToJsonType.put("brukskonto", "brukskonto");
        tilleggsinfoToJsonType.put("bsu", "bsu");
        tilleggsinfoToJsonType.put("campingvogn", "campingvogn");
        tilleggsinfoToJsonType.put("eiendom", "salg");
        tilleggsinfoToJsonType.put("forsikringsutbetaling", "forsikring");
        tilleggsinfoToJsonType.put("fritidsaktivitet", "barnFritidsaktiviteter");
        tilleggsinfoToJsonType.put("fritidseiendom", "fritidseiendom");
        tilleggsinfoToJsonType.put("husleie", "husleie");
        tilleggsinfoToJsonType.put("kjopekontrakt", "bolig");
        tilleggsinfoToJsonType.put("kjoretoy", "kjoretoy");
        tilleggsinfoToJsonType.put("kommunaleavgifter", "kommunalAvgift");
        tilleggsinfoToJsonType.put("livsforsikring", "livsforsikringssparedel");
        tilleggsinfoToJsonType.put("mottar", "barnebidrag");
        tilleggsinfoToJsonType.put("oppvarming", "oppvarming");
        tilleggsinfoToJsonType.put("sfo", "sfo");
        tilleggsinfoToJsonType.put("sparekonto", "sparekonto");
        tilleggsinfoToJsonType.put("strom", "strom");
        tilleggsinfoToJsonType.put("tannbehandling", "barnTannregulering");
        tilleggsinfoToJsonType.put("utbytte", "utbytte");
    }

    @Inject
    private LegacyHelper legacyHelper;

    @GET
    public VedleggFrontends hentOkonomiskeOpplysninger(@PathParam("behandlingsId") String behandlingsId){
        final String eier = SubjectHandler.getSubjectHandler().getUid();
        final JsonInternalSoknad soknad = legacyHelper.hentSoknad(behandlingsId, eier).getJsonInternalSoknad();
        List<JsonVedlegg> jsonVedleggs = soknad.getVedlegg().getVedlegg();
        final JsonOkonomi jsonOkonomi = soknad.getSoknad().getData().getOkonomi();

        if (jsonVedleggs != null && !jsonVedleggs.isEmpty()){
            return new VedleggFrontends().withVedleggFrontends(jsonVedleggs.stream()
                    .map(vedlegg -> mapToVedleggFrontend(vedlegg, jsonOkonomi)).collect(Collectors.toList()));
        }
        return new VedleggFrontends();
    }

    private VedleggFrontend mapToVedleggFrontend(JsonVedlegg vedlegg, JsonOkonomi jsonOkonomi) {
        final String beskrivelse = getBeskrivelse(jsonOkonomi.getOpplysninger(), vedlegg.getType(), vedlegg.getTilleggsinfo());
        final List<Integer> belop = getBelop(jsonOkonomi, vedlegg.getType(), vedlegg.getTilleggsinfo());
        final List<Integer> avdrag = getAvdrag(jsonOkonomi, vedlegg.getTilleggsinfo());
        final List<Integer> renter = getRenter(jsonOkonomi, vedlegg.getTilleggsinfo());
        final List<String> filer = vedlegg.getFiler().stream().map(this::mapToFilnavn).collect(Collectors.toList());
        return new VedleggFrontend().withType(vedlegg.getType() + "." + vedlegg.getTilleggsinfo())
                .withGruppe(getGruppe(vedlegg.getType(), vedlegg.getTilleggsinfo()))
                .withBeskrivelse(beskrivelse)
                .withBelop(belop)
                .withAvdrag(avdrag)
                .withRenter(renter)
                .withVedleggStatus(vedlegg.getStatus())
                .withFilNavn(filer);
    }

    private String getBeskrivelse(JsonOkonomiopplysninger jsonOkonomiopplysninger, String type, String tilleggsinfo) {
        switch (tilleggsinfo){
            case "annetboutgift":
            case "fritidsaktivitet":
            case "annetbarnutgift":
                return mapToBrukerangittSubstring(getTittelFromUtgiftType(jsonOkonomiopplysninger, tilleggsinfoToJsonType.get(tilleggsinfo)));
            case "annet":
                if (type.equals("annet")){
                    return mapToBrukerangittSubstring(getTittelFromUtgiftType(jsonOkonomiopplysninger, "annen"));
                }
            default:
                return null;
        }
    }

    private String getGruppe(String type, String tilleggsinfo) {
        switch (tilleggsinfo){
            case "arbeid":
                if(type.equals("lonnslipp")){
                    return "inntekt";
                } else if (type.equals("sluttoppgjor")){
                    return "arbeid";
                }
            case "husleiekontrakt":
                return "bosituasjon";
            case "vedtak":
                if(type.equals("bostotte")){
                    return "inntekt";
                } else if (type.equals("student")){
                    return "arbeid";
                }
            case "annet":
                if (type.equals("kontooversikt")){
                    return "inntekt";
                } else if (type.equals("annet")){
                    return "utgifter";
                }
            case "mottar":
            case "betaler":
            case "barn":
                return "familie";
            case "eiendom":
            case "forsikringsutbetaling":
            case "annetinntekter":
            case "utbytte":
            case "kjoretoy":
            case "campingvogn":
            case "fritidseiendom":
            case "brukskonto":
            case "bsu":
            case "sparekonto":
            case "kjopekontrakt":
            case "livsforsikring":
            case "annetverdi":
            case "aksjer":
                return "inntekt";
            case "sfo":
            case "barnehage":
            case "husleie":
            case "annetbarnutgift":
            case "annetboutgift":
            case "tannbehandling":
            case "kommunaleavgifter":
            case "fritidsaktivitet":
            case "oppvarming":
            case "strom":
                return "utgifter";
        }
        return null;
    }

    private List<Integer> getBelop(JsonOkonomi jsonOkonomi, String type, String tilleggsinfo) {
        switch (tilleggsinfo){
            case "mottar":
                return getBelopListFromInntekt(jsonOkonomi, "barnebidrag");
            case "vedtak":
                if(type.equals("bostotte")){
                    return getBelopListFromInntekt(jsonOkonomi, "bostotte");
                } else if (type.equals("student")){
                    return getBelopListFromInntekt(jsonOkonomi, "studielanOgStipend");
                }
            case "arbeid":
                if(type.equals("lonnslipp")){
                    return getBelopListFromInntekt(jsonOkonomi, "jobb");
                } else if (type.equals("sluttoppgjor")){
                    return getBelopListFromUtbetaling(jsonOkonomi, "sluttoppgjoer");
                }
            case "eiendom":
            case "forsikringsutbetaling":
            case "annetinntekter":
            case "utbytte":
                return getBelopListFromUtbetaling(jsonOkonomi, tilleggsinfoToJsonType.get(tilleggsinfo));
            case "sfo":
            case "barnehage":
            case "betaler":
            case "husleie":
                return getBelopListFromOversiktUtgift(jsonOkonomi, tilleggsinfoToJsonType.get(tilleggsinfo));
            case "kjoretoy":
            case "campingvogn":
            case "fritidseiendom":
            case "brukskonto":
            case "bsu":
            case "sparekonto":
            case "kjopekontrakt":
            case "livsforsikring":
            case "annetverdi":
            case "aksjer":
                return getBelopListFromFormue(jsonOkonomi, tilleggsinfoToJsonType.get(tilleggsinfo));
            case "annet":
                if (type.equals("kontooversikt")){
                    return getBelopListFromFormue(jsonOkonomi, "belop");
                } else if (type.equals("annet")){
                    return getBelopListFromOpplysningerUtgift(jsonOkonomi, "annen");
                }
            case "annetbarnutgift":
            case "annetboutgift":
            case "tannbehandling":
            case "kommunaleavgifter":
            case "fritidsaktivitet":
            case "oppvarming":
            case "strom":
                return getBelopListFromOpplysningerUtgift(jsonOkonomi, tilleggsinfoToJsonType.get(tilleggsinfo));
            default:
                return null;
        }
    }

    private List<Integer> getAvdrag(JsonOkonomi jsonOkonomi, String tilleggsinfo) {
        if (tilleggsinfo.equals("avdraglaan")){
            return getBelopListFromOversiktUtgift(jsonOkonomi, "boliglanAvdrag");
        }
        return null;
    }

    private List<Integer> getRenter(JsonOkonomi jsonOkonomi, String tilleggsinfo) {
        if (tilleggsinfo.equals("avdraglaan")){
            return getBelopListFromOversiktUtgift(jsonOkonomi, "boliglanRenter");
        }
        return null;
    }

    private List<Integer> getBelopListFromUtbetaling(JsonOkonomi jsonOkonomi, String jsonType) {
        return jsonOkonomi.getOpplysninger().getUtbetaling().stream()
                .filter(inntekt -> inntekt.getType().equals(jsonType))
                .map(this::getBelopFromUtbetaling).collect(Collectors.toList());
    }

    private List<Integer> getBelopListFromOpplysningerUtgift(JsonOkonomi jsonOkonomi, String jsonType) {
        return jsonOkonomi.getOpplysninger().getUtgift().stream()
                .filter(formue -> formue.getType().equals(jsonType))
                .map(this::getBelopFromOpplysningerUtgift).collect(Collectors.toList());
    }

    private List<Integer> getBelopListFromInntekt(JsonOkonomi jsonOkonomi, String jsonType) {
        return jsonOkonomi.getOversikt().getInntekt().stream()
                .filter(inntekt -> inntekt.getType().equals(jsonType))
                .map(this::getBelopFromInntekt).collect(Collectors.toList());
    }

    private List<Integer> getBelopListFromOversiktUtgift(JsonOkonomi jsonOkonomi, String jsonType) {
        return jsonOkonomi.getOversikt().getUtgift().stream()
                .filter(utgift -> utgift.getType().equals(jsonType))
                .map(this::getBelopFromOversiktUtgift).collect(Collectors.toList());
    }

    private List<Integer> getBelopListFromFormue(JsonOkonomi jsonOkonomi, String jsonType) {
        return jsonOkonomi.getOversikt().getFormue().stream()
                .filter(formue -> formue.getType().equals(jsonType))
                .map(this::getBelopFromFormue).collect(Collectors.toList());
    }

    private Integer getBelopFromUtbetaling(JsonOkonomiOpplysningUtbetaling utbetaling) {
        if (utbetaling.getBelop() != null){
            return utbetaling.getBelop();
        } else if (utbetaling.getBrutto() != null){
            return new Integer(String.valueOf(utbetaling.getBrutto()));
        } else if (utbetaling.getNetto() != null) {
            return new Integer(String.valueOf(utbetaling.getNetto()));
        }
        return null;
    }

    private Integer getBelopFromOpplysningerUtgift(JsonOkonomiOpplysningUtgift utgift) {
        return utgift.getBelop();
    }

    private Integer getBelopFromInntekt(JsonOkonomioversiktInntekt inntekt) {
        if (inntekt.getBrutto() != null){
            return inntekt.getBrutto();
        } else if (inntekt.getNetto() != null) {
            return inntekt.getNetto();
        }
        return null;
    }

    private Integer getBelopFromOversiktUtgift(JsonOkonomioversiktUtgift utgift) {
        return utgift.getBelop();
    }

    private Integer getBelopFromFormue(JsonOkonomioversiktFormue formue) {
         return formue.getBelop();
    }

    private String getTittelFromUtgiftType(JsonOkonomiopplysninger okonomiopplysninger, String type) {
        return okonomiopplysninger.getUtgift().stream()
                .filter(utgift -> utgift.getType().equals(type)).findFirst().get().getTittel();
    }

    private String mapToBrukerangittSubstring(String tittel) {
        return tittel.substring(tittel.lastIndexOf(":") + 2);
    }

    private String mapToFilnavn(JsonFiler jsonFil) {
        return jsonFil.getFilnavn();
    }

    @XmlAccessorType(XmlAccessType.FIELD)
    public static final class VedleggFrontends {
        public List<VedleggFrontend> vedleggFrontends;

        public VedleggFrontends withVedleggFrontends(List<VedleggFrontend> vedleggFrontends) {
            this.vedleggFrontends = vedleggFrontends;
            return this;
        }
    }
}
