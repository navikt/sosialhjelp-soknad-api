package no.nav.sbl.dialogarena.rest.ressurser.okonomi;

import no.nav.metrics.aspects.Timed;
import no.nav.modig.core.context.SubjectHandler;
import no.nav.sbl.dialogarena.rest.ressurser.LegacyHelper;
import no.nav.sbl.dialogarena.rest.ressurser.VedleggFrontend;
import no.nav.sbl.dialogarena.sendsoknad.domain.WebSoknad;
import no.nav.sbl.dialogarena.sikkerhet.Tilgangskontroll;
import no.nav.sbl.dialogarena.soknadinnsending.business.service.FaktaService;
import no.nav.sbl.dialogarena.soknadinnsending.business.service.soknadservice.SoknadService;
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
import no.nav.sbl.sosialhjelp.domain.SoknadUnderArbeid;
import no.nav.sbl.sosialhjelp.soknadunderbehandling.SoknadUnderArbeidRepository;
import org.springframework.stereotype.Controller;

import javax.inject.Inject;
import javax.ws.rs.*;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import java.util.*;
import java.util.stream.Collectors;

import static javax.ws.rs.core.MediaType.APPLICATION_JSON;

@Controller
@Path("/soknader/{behandlingsId}/okonomiskeOpplysninger")
@Timed
@Produces(APPLICATION_JSON)
public class OkonomiskeOpplysningerRessurs {

    final static private Map<String,String> tilleggsinfoToJsonType = new HashMap<>();
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

    private final static Set<String> opplysningerUtgift = new HashSet<>();
    static  {
        opplysningerUtgift.addAll(Arrays.asList("annetbarnutgift", "annetboutgift", "tannbehandling", "kommunaleavgifter",
                "fritidsaktivitet", "oppvarming", "strom"));
    }

    private final static Set<String> oversiktUtgift = new HashSet<>();
    static  {
        oversiktUtgift.addAll(Arrays.asList("sfo", "barnehage", "betaler", "husleie"));
    }

    private final static Set<String> formue = new HashSet<>();
    static  {
        formue.addAll(Arrays.asList("kjoretoy", "campingvogn", "fritidseiendom",
                "brukskonto", "bsu", "sparekonto", "kjopekontrakt", "livsforsikring",
                "annetverdi", "aksjer"));
    }

    private final static Set<String> utbetaling = new HashSet<>();
    static  {
        utbetaling.addAll(Arrays.asList("eiendom", "forsikringsutbetaling", "annetinntekter", "utbytte"));
    }

    @Inject
    private LegacyHelper legacyHelper;

    @Inject
    private Tilgangskontroll tilgangskontroll;

    @Inject
    private SoknadService soknadService;

    @Inject
    private FaktaService faktaService;

    @Inject
    private SoknadUnderArbeidRepository soknadUnderArbeidRepository;

    @GET
    public VedleggFrontends hentOkonomiskeOpplysninger(@PathParam("behandlingsId") String behandlingsId){
        final String eier = SubjectHandler.getSubjectHandler().getUid();
        final JsonInternalSoknad soknad = legacyHelper.hentSoknad(behandlingsId, eier).getJsonInternalSoknad();
        List<JsonVedlegg> jsonVedleggs = soknad.getVedlegg().getVedlegg();
        final JsonOkonomi jsonOkonomi = soknad.getSoknad().getData().getOkonomi();

        if (jsonVedleggs != null && !jsonVedleggs.isEmpty()){
            return new VedleggFrontends().withOkonomiskeOpplysninger(jsonVedleggs.stream()
                    .map(vedlegg -> mapToVedleggFrontend(vedlegg, jsonOkonomi)).collect(Collectors.toList()));
        }
        return new VedleggFrontends();
    }

    @PUT
    public void updateOkonomiskOpplysning(@PathParam("behandlingsId") String behandlingsId, VedleggFrontend vedleggFrontend){
        tilgangskontroll.verifiserAtBrukerKanEndreSoknad(behandlingsId);
        update(behandlingsId, vedleggFrontend);
        legacyUpdate(behandlingsId, vedleggFrontend);
    }

    private void update(String behandlingsId, VedleggFrontend vedleggFrontend) {
        final String eier = SubjectHandler.getSubjectHandler().getUid();
        final SoknadUnderArbeid soknad = soknadUnderArbeidRepository.hentSoknad(behandlingsId, eier).get();
        final JsonOkonomi jsonOkonomi = soknad.getJsonInternalSoknad().getSoknad().getData().getOkonomi();

    }

    private void legacyUpdate(String behandlingsId, VedleggFrontend vedleggFrontend) {
        final WebSoknad webSoknad = soknadService.hentSoknad(behandlingsId, false, false);
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
        if (tilleggsinfo.equals("mottar") || tilleggsinfo.equals("betaler") || tilleggsinfo.equals("barn")){
            return "familie";
        } else if (tilleggsinfo.equals("husleiekontrakt")){
            return "bosituasjon";
        } else if (tilleggsinfo.equals("arbeid") && type.equals("sluttoppgjor")){
            return "arbeid";
        } else if (tilleggsinfo.equals("vedtak") && type.equals("student")){
            return "arbeid";
        } else if (tilleggsinfo.equals("arbeid") && type.equals("lonnslipp")){
            return "inntekt";
        } else if (tilleggsinfo.equals("vedtak") && type.equals("bostotte")){
            return "inntekt";
        } else if (tilleggsinfo.equals("annet") && type.equals("kontooversikt")){
            return "inntekt";
        } else if (tilleggsinfo.equals("annet") && type.equals("annet")){
            return "utgifter";
        }

        if (utbetaling.contains(tilleggsinfo) || formue.contains(tilleggsinfo)){
            return "inntekt";
        }
        if (opplysningerUtgift.contains(tilleggsinfo) || oversiktUtgift.contains(tilleggsinfo)){
            return "utgifter";
        }

        return null;
    }

    private List<Integer> getBelop(JsonOkonomi jsonOkonomi, String type, String tilleggsinfo) {
        if (utbetaling.contains(tilleggsinfo)){
            return getBelopListFromUtbetaling(jsonOkonomi, tilleggsinfoToJsonType.get(tilleggsinfo));
        }
        if (opplysningerUtgift.contains(tilleggsinfo)){
            return getBelopListFromOpplysningerUtgift(jsonOkonomi, tilleggsinfoToJsonType.get(tilleggsinfo));
        }
        if (oversiktUtgift.contains(tilleggsinfo)){
            return getBelopListFromOversiktUtgift(jsonOkonomi, tilleggsinfoToJsonType.get(tilleggsinfo));
        }
        if (formue.contains(tilleggsinfo)){
            return getBelopListFromFormue(jsonOkonomi, tilleggsinfoToJsonType.get(tilleggsinfo));
        }

        if (tilleggsinfo.equals("mottar")){
            return getBelopListFromInntekt(jsonOkonomi, "barnebidrag");
        } else if (tilleggsinfo.equals("vedtak") && type.equals("bostotte")){
            return getBelopListFromInntekt(jsonOkonomi, "bostotte");
        } else if (tilleggsinfo.equals("vedtak") && type.equals("student")){
            return getBelopListFromInntekt(jsonOkonomi, "studielanOgStipend");
        } else if (tilleggsinfo.equals("arbeid") && type.equals("lonnslipp")){
            return getBelopListFromInntekt(jsonOkonomi, "jobb");
        } else if (tilleggsinfo.equals("arbeid") && type.equals("sluttoppgjor")){
            return getBelopListFromUtbetaling(jsonOkonomi, "sluttoppgjoer");
        } else if (tilleggsinfo.equals("annet") && type.equals("kontooversikt")){
            return getBelopListFromFormue(jsonOkonomi, "belop");
        } else if (tilleggsinfo.equals("annet") && type.equals("annet")){
            return getBelopListFromOpplysningerUtgift(jsonOkonomi, "annen");
        }

        return null;
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
        public List<VedleggFrontend> okonomiskeOpplysninger;

        public VedleggFrontends withOkonomiskeOpplysninger(List<VedleggFrontend> okonomiskeOpplysninger) {
            this.okonomiskeOpplysninger = okonomiskeOpplysninger;
            return this;
        }
    }
}
