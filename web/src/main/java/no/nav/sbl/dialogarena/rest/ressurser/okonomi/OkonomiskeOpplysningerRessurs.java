package no.nav.sbl.dialogarena.rest.ressurser.okonomi;

import no.nav.metrics.aspects.Timed;
import no.nav.modig.core.context.SubjectHandler;
import no.nav.sbl.dialogarena.rest.ressurser.LegacyHelper;
import no.nav.sbl.dialogarena.rest.ressurser.VedleggFrontend;
import no.nav.sbl.dialogarena.rest.ressurser.VedleggRadFrontend;
import no.nav.sbl.dialogarena.sendsoknad.domain.WebSoknad;
import no.nav.sbl.dialogarena.sikkerhet.Tilgangskontroll;
import no.nav.sbl.dialogarena.soknadinnsending.business.service.FaktaService;
import no.nav.sbl.dialogarena.soknadinnsending.business.service.soknadservice.SoknadService;
import no.nav.sbl.soknadsosialhjelp.soknad.JsonInternalSoknad;
import no.nav.sbl.soknadsosialhjelp.soknad.okonomi.JsonOkonomi;
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
    private final static Set<String> oversiktUtgift = new HashSet<>();
    private final static Set<String> formue = new HashSet<>();
    private final static Set<String> utbetaling = new HashSet<>();

    static  {
        opplysningerUtgift.addAll(Arrays.asList("annetbarnutgift", "annetboutgift", "tannbehandling", "kommunaleavgifter",
                "fritidsaktivitet", "oppvarming", "strom"));
        oversiktUtgift.addAll(Arrays.asList("sfo", "barnehage", "betaler", "husleie"));
        formue.addAll(Arrays.asList("kjoretoy", "campingvogn", "fritidseiendom",
                "brukskonto", "bsu", "sparekonto", "kjopekontrakt", "livsforsikring",
                "annetverdi", "aksjer"));
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
        final List<JsonVedlegg> jsonVedleggs = soknad.getVedlegg().getVedlegg();
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
        final List<String> filer = vedlegg.getFiler().stream().map(JsonFiler::getFilnavn)
                .collect(Collectors.toList());
        final List<VedleggRadFrontend> rader = getRader(jsonOkonomi, vedlegg.getType(), vedlegg.getTilleggsinfo());

        return new VedleggFrontend().withType(vedlegg.getType() + "." + vedlegg.getTilleggsinfo())
                .withGruppe(getGruppe(vedlegg.getType(), vedlegg.getTilleggsinfo()))
                .withRader(rader)
                .withVedleggStatus(vedlegg.getStatus())
                .withFilNavn(filer);
    }

    private List<VedleggRadFrontend> getRader(JsonOkonomi jsonOkonomi, String type, String tilleggsinfo) {
        // Spesialtilfelle for avdrag og renter
        if (tilleggsinfo.equals("avdraglaan")){
            final List<VedleggRadFrontend> avdragRad = getRadListFromOversiktUtgift(jsonOkonomi, "boliglanAvdrag");
            final List<VedleggRadFrontend> renterRad = getRadListFromOversiktUtgift(jsonOkonomi, "boliglanRenter");

            if (avdragRad != null){
                for (int i = 0; i < avdragRad.size(); i++){
                    avdragRad.get(i).withRenter(renterRad.get(i).renter);
                }
            }

            return avdragRad;
        }

        if (utbetaling.contains(tilleggsinfo)){
            return getRadListFromUtbetaling(jsonOkonomi, tilleggsinfoToJsonType.get(tilleggsinfo));
        }
        if (opplysningerUtgift.contains(tilleggsinfo)){
            return getRadListFromOpplysningerUtgift(jsonOkonomi, tilleggsinfoToJsonType.get(tilleggsinfo));
        }
        if (oversiktUtgift.contains(tilleggsinfo)){
            return getRadListFromOversiktUtgift(jsonOkonomi, tilleggsinfoToJsonType.get(tilleggsinfo));
        }
        if (formue.contains(tilleggsinfo)){
            return getRadListFromFormue(jsonOkonomi, tilleggsinfoToJsonType.get(tilleggsinfo));
        }

        if (tilleggsinfo.equals("mottar")){
            return getRadListFromInntekt(jsonOkonomi, "barnebidrag");
        } else if (tilleggsinfo.equals("vedtak") && type.equals("bostotte")){
            return getRadListFromInntekt(jsonOkonomi, "bostotte");
        } else if (tilleggsinfo.equals("vedtak") && type.equals("student")){
            return getRadListFromInntekt(jsonOkonomi, "studielanOgStipend");
        } else if (tilleggsinfo.equals("arbeid") && type.equals("lonnslipp")){
            return getRadListFromInntekt(jsonOkonomi, "jobb");
        } else if (tilleggsinfo.equals("arbeid") && type.equals("sluttoppgjor")){
            return getRadListFromUtbetaling(jsonOkonomi, "sluttoppgjoer");
        } else if (tilleggsinfo.equals("annet") && type.equals("kontooversikt")){
            return getRadListFromFormue(jsonOkonomi, "belop");
        } else if (tilleggsinfo.equals("annet") && type.equals("annet")){
            return getRadListFromOpplysningerUtgift(jsonOkonomi, "annen");
        }

        return null;
    }

    private List<VedleggRadFrontend> getRadListFromUtbetaling(JsonOkonomi jsonOkonomi, String jsonType) {
        return jsonOkonomi.getOpplysninger().getUtbetaling().stream()
                .filter(inntekt -> inntekt.getType().equals(jsonType))
                .map(this::getRadFromUtbetaling).collect(Collectors.toList());
    }

    private List<VedleggRadFrontend> getRadListFromOpplysningerUtgift(JsonOkonomi jsonOkonomi, String jsonType) {
        return jsonOkonomi.getOpplysninger().getUtgift().stream()
                .filter(utgift -> utgift.getType().equals(jsonType))
                .map(utgift -> getRadFromOpplysningerUtgift(utgift, jsonType)).collect(Collectors.toList());
    }

    private List<VedleggRadFrontend> getRadListFromInntekt(JsonOkonomi jsonOkonomi, String jsonType) {
        return jsonOkonomi.getOversikt().getInntekt().stream()
                .filter(inntekt-> inntekt.getType().equals(jsonType))
                .map(inntekt -> getRadFromInntekt(inntekt, jsonType)).collect(Collectors.toList());
    }

    private List<VedleggRadFrontend> getRadListFromOversiktUtgift(JsonOkonomi jsonOkonomi, String jsonType) {
        return jsonOkonomi.getOversikt().getUtgift().stream()
                .filter(utgift -> utgift.getType().equals(jsonType))
                .map(utgift -> getRadFromOversiktUtgift(utgift, jsonType)).collect(Collectors.toList());
    }

    private List<VedleggRadFrontend> getRadListFromFormue(JsonOkonomi jsonOkonomi, String jsonType) {
        return jsonOkonomi.getOversikt().getFormue().stream()
                .filter(utgift -> utgift.getType().equals(jsonType))
                .map(formue -> getRadFromFormue(formue, jsonType)).collect(Collectors.toList());
    }

    private VedleggRadFrontend getRadFromUtbetaling(JsonOkonomiOpplysningUtbetaling utbetaling) {
        if (utbetaling.getBelop() != null){
            return new VedleggRadFrontend().withBelop(utbetaling.getBelop());
        } else if (utbetaling.getBrutto() != null){
            return new VedleggRadFrontend().withBelop(new Integer(String.valueOf(utbetaling.getBrutto())));
        } else if (utbetaling.getNetto() != null) {
            return new VedleggRadFrontend().withBelop(new Integer(String.valueOf(utbetaling.getNetto())));
        }
        return null;
    }

    private VedleggRadFrontend getRadFromOpplysningerUtgift(JsonOkonomiOpplysningUtgift utgift, String jsonType) {
        switch (jsonType){
            case "annen":
            case "annenBarneutgift":
            case "annenBoutgift":
            case "barnFritidsaktiviteter":
                return new VedleggRadFrontend().withBelop(utgift.getBelop())
                        .withBeskrivelse(utgift.getTittel().substring(utgift.getTittel().indexOf(":") + 2));
            default:
                return new VedleggRadFrontend().withBelop(utgift.getBelop());
        }
    }

    private VedleggRadFrontend getRadFromInntekt(JsonOkonomioversiktInntekt inntekt, String jsonType) {
        if (inntekt.getBrutto() != null){
            return new VedleggRadFrontend().withBelop(inntekt.getBrutto());
        } else if (inntekt.getNetto() != null) {
            return new VedleggRadFrontend().withBelop(inntekt.getNetto());
        }
        return null;
    }

    private VedleggRadFrontend getRadFromOversiktUtgift(JsonOkonomioversiktUtgift utgift, String jsonType) {
        if (jsonType.equals("boliglanAvdrag")){
            return new VedleggRadFrontend().withAvdrag(utgift.getBelop());
        } else if (jsonType.equals("boliglanRenter")){
            return new VedleggRadFrontend().withRenter(utgift.getBelop());
        }
        return new VedleggRadFrontend().withBelop(utgift.getBelop());
    }

    private VedleggRadFrontend getRadFromFormue(JsonOkonomioversiktFormue formue, String jsonType) {
        return new VedleggRadFrontend().withBelop(formue.getBelop());
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
        } else {
            if (utbetaling.contains(tilleggsinfo) || formue.contains(tilleggsinfo)){
                return "inntekt";
            }
            if (opplysningerUtgift.contains(tilleggsinfo) || oversiktUtgift.contains(tilleggsinfo)){
                return "utgifter";
            }
        }

        return null;
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
