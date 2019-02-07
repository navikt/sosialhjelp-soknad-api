package no.nav.sbl.dialogarena.rest.ressurser.inntekt;

import no.nav.metrics.aspects.Timed;
import no.nav.modig.core.context.SubjectHandler;
import no.nav.sbl.dialogarena.rest.ressurser.LegacyHelper;
import no.nav.sbl.dialogarena.sendsoknad.domain.Faktum;
import no.nav.sbl.dialogarena.sendsoknad.domain.WebSoknad;
import no.nav.sbl.dialogarena.sikkerhet.Tilgangskontroll;
import no.nav.sbl.dialogarena.soknadinnsending.business.service.FaktaService;
import no.nav.sbl.dialogarena.soknadinnsending.business.service.soknadservice.SoknadService;
import no.nav.sbl.dialogarena.soknadsosialhjelp.message.NavMessageSource;
import no.nav.sbl.soknadsosialhjelp.soknad.JsonInternalSoknad;
import no.nav.sbl.soknadsosialhjelp.soknad.common.JsonKilde;
import no.nav.sbl.soknadsosialhjelp.soknad.common.JsonKildeBruker;
import no.nav.sbl.soknadsosialhjelp.soknad.okonomi.JsonOkonomiopplysninger;
import no.nav.sbl.soknadsosialhjelp.soknad.okonomi.JsonOkonomioversikt;
import no.nav.sbl.soknadsosialhjelp.soknad.okonomi.opplysning.JsonOkonomibekreftelse;
import no.nav.sbl.soknadsosialhjelp.soknad.okonomi.opplysning.JsonOkonomibeskrivelserAvAnnet;
import no.nav.sbl.soknadsosialhjelp.soknad.okonomi.oversikt.JsonOkonomioversiktFormue;
import no.nav.sbl.sosialhjelp.domain.SoknadUnderArbeid;
import no.nav.sbl.sosialhjelp.soknadunderbehandling.SoknadUnderArbeidRepository;
import org.springframework.stereotype.Controller;

import javax.inject.Inject;
import javax.ws.rs.*;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import java.util.*;

import static javax.ws.rs.core.MediaType.APPLICATION_JSON;
import static no.nav.sbl.dialogarena.rest.mappers.BekreftelseMapper.setBekreftelse;
import static no.nav.sbl.dialogarena.rest.mappers.FaktumNoklerOgBelopNavnMapper.jsonTypeToFaktumKey;

@Controller
@Path("/soknader/{behandlingsId}/inntekt/formue")
@Timed
@Produces(APPLICATION_JSON)
public class FormueRessurs {

    @Inject
    private LegacyHelper legacyHelper;

    @Inject
    private Tilgangskontroll tilgangskontroll;

    @Inject
    private SoknadUnderArbeidRepository soknadUnderArbeidRepository;

    @Inject
    private NavMessageSource navMessageSource;

    @Inject
    private SoknadService soknadService;

    @Inject
    private FaktaService faktaService;

    @GET
    public FormueFrontend hentFormue(@PathParam("behandlingsId") String behandlingsId){
        final String eier = SubjectHandler.getSubjectHandler().getUid();
        final JsonInternalSoknad soknad = legacyHelper.hentSoknad(behandlingsId, eier).getJsonInternalSoknad();
        final JsonOkonomiopplysninger opplysninger = soknad.getSoknad().getData().getOkonomi().getOpplysninger();
        final JsonOkonomioversikt oversikt = soknad.getSoknad().getData().getOkonomi().getOversikt();
        final FormueFrontend formueFrontend = new FormueFrontend();

        if (opplysninger.getBekreftelse() == null){
            return formueFrontend;
        }

        setBekreftelseOnFormueFrontend(opplysninger, formueFrontend);
        setFormuetyperOnFormueFrontend(oversikt, formueFrontend);

        if (opplysninger.getBeskrivelseAvAnnet() != null){
            formueFrontend.withBeskrivelseAvAnnet(opplysninger.getBeskrivelseAvAnnet().getSparing());
        }

        return formueFrontend;
    }

    @PUT
    public void updateUtbetaltinger(@PathParam("behandlingsId") String behandlingsId, FormueFrontend formueFrontend){
        tilgangskontroll.verifiserAtBrukerKanEndreSoknad(behandlingsId);
        update(behandlingsId, formueFrontend);
        legacyUpdate(behandlingsId, formueFrontend);
    }

    private void update(String behandlingsId, FormueFrontend formueFrontend) {
        final String eier = SubjectHandler.getSubjectHandler().getUid();
        final SoknadUnderArbeid soknad = soknadUnderArbeidRepository.hentSoknad(behandlingsId, eier).get();
        final JsonOkonomiopplysninger opplysninger = soknad.getJsonInternalSoknad().getSoknad().getData().getOkonomi().getOpplysninger();
        final JsonOkonomioversikt oversikt = soknad.getJsonInternalSoknad().getSoknad().getData().getOkonomi().getOversikt();

        if (opplysninger.getBekreftelse() == null){
            opplysninger.setBekreftelse(new ArrayList<>());
        }

        setBekreftelse(opplysninger, "sparing", formueFrontend.bekreftelse, getJsonOkonomiTittel("inntekt.bankinnskudd"));
        setFormue(oversikt, formueFrontend);
        setBeskrivelseAvAnnet(opplysninger, formueFrontend);

        soknadUnderArbeidRepository.oppdaterSoknadsdata(soknad, eier);
    }

    private void legacyUpdate(String behandlingsId, FormueFrontend formueFrontend) {
        final WebSoknad webSoknad = soknadService.hentSoknad(behandlingsId, false, false);

        final Faktum bekreftelse = faktaService.hentFaktumMedKey(webSoknad.getSoknadId(), "inntekt.bankinnskudd");
        bekreftelse.setValue(formueFrontend.bekreftelse.toString());
        faktaService.lagreBrukerFaktum(bekreftelse);

        final Faktum brukskonto = faktaService.hentFaktumMedKey(webSoknad.getSoknadId(), "opplysninger.inntekt.bankinnskudd.brukskonto");
        brukskonto.setValue(String.valueOf(formueFrontend.brukskonto));
        faktaService.lagreBrukerFaktum(brukskonto);

        final Faktum bsu = faktaService.hentFaktumMedKey(webSoknad.getSoknadId(), "opplysninger.inntekt.bankinnskudd.bsu");
        bsu.setValue(String.valueOf(formueFrontend.bsu));
        faktaService.lagreBrukerFaktum(bsu);

        final Faktum sparekonto = faktaService.hentFaktumMedKey(webSoknad.getSoknadId(), "opplysninger.inntekt.bankinnskudd.sparekonto");
        sparekonto.setValue(String.valueOf(formueFrontend.sparekonto));
        faktaService.lagreBrukerFaktum(sparekonto);

        final Faktum livsforsikring = faktaService.hentFaktumMedKey(webSoknad.getSoknadId(), "opplysninger.inntekt.bankinnskudd.livsforsikring");
        livsforsikring.setValue(String.valueOf(formueFrontend.livsforsikring));
        faktaService.lagreBrukerFaktum(livsforsikring);

        final Faktum annet = faktaService.hentFaktumMedKey(webSoknad.getSoknadId(), "opplysninger.inntekt.bankinnskudd.annet");
        annet.setValue(String.valueOf(formueFrontend.annet));
        faktaService.lagreBrukerFaktum(annet);

        final Faktum beskrivelse = faktaService.hentFaktumMedKey(webSoknad.getSoknadId(), "inntekt.bankinnskudd.true.type.annet.true.beskrivelse");
        beskrivelse.setValue(formueFrontend.beskrivelseAvAnnet != null ? formueFrontend.beskrivelseAvAnnet : "");
        faktaService.lagreBrukerFaktum(beskrivelse);
    }

    private void setFormue(JsonOkonomioversikt oversikt, FormueFrontend formueFrontend) {
        List<JsonOkonomioversiktFormue> formue = oversikt.getFormue();

        if(formueFrontend.brukskonto){
            final String type = "brukskonto";
            final String tittel = getJsonOkonomiTittel(jsonTypeToFaktumKey.get(type));
            addFormueIfNotPresentInOversikt(oversikt, formue, type, tittel);
        }
        if(formueFrontend.bsu){
            final String type = "bsu";
            final String tittel = getJsonOkonomiTittel(jsonTypeToFaktumKey.get(type));
            addFormueIfNotPresentInOversikt(oversikt, formue, type, tittel);
        }
        if(formueFrontend.livsforsikring){
            final String type = "livsforsikring";
            final String tittel = getJsonOkonomiTittel(jsonTypeToFaktumKey.get(type));
            addFormueIfNotPresentInOversikt(oversikt, formue, type, tittel);
        }
        if(formueFrontend.sparekonto){
            final String type = "sparekonto";
            final String tittel = getJsonOkonomiTittel(jsonTypeToFaktumKey.get(type));
            addFormueIfNotPresentInOversikt(oversikt, formue, type, tittel);
        }
        if(formueFrontend.verdipapirer){
            final String type = "verdipapirer";
            final String tittel = getJsonOkonomiTittel(jsonTypeToFaktumKey.get(type));
            addFormueIfNotPresentInOversikt(oversikt, formue, type, tittel);
        }
        if(formueFrontend.annet){
            final String type = "belop";
            final String tittel = getJsonOkonomiTittel(jsonTypeToFaktumKey.get(type));
            addFormueIfNotPresentInOversikt(oversikt, formue, type, tittel);
        }
    }

    private void addFormueIfNotPresentInOversikt(JsonOkonomioversikt oversikt, List<JsonOkonomioversiktFormue> formuer, String type, String tittel) {
        Optional<JsonOkonomioversiktFormue> jsonFormue = oversikt.getFormue().stream()
                .filter(formue -> formue.getType().equals(type)).findFirst();
        if (!jsonFormue.isPresent()){
            formuer.add(new JsonOkonomioversiktFormue()
                    .withKilde(JsonKilde.BRUKER)
                    .withType(type)
                    .withTittel(tittel)
                    .withOverstyrtAvBruker(false));
        }
    }

    private void setBeskrivelseAvAnnet(JsonOkonomiopplysninger opplysninger, FormueFrontend formueFrontend) {
        if (opplysninger.getBeskrivelseAvAnnet() == null){
            opplysninger.withBeskrivelseAvAnnet(new JsonOkonomibeskrivelserAvAnnet()
                    .withKilde(JsonKildeBruker.BRUKER)
                    .withVerdi("")
                    .withSparing("")
                    .withUtbetaling("")
                    .withBoutgifter("")
                    .withBarneutgifter(""));
        }
        opplysninger.getBeskrivelseAvAnnet().setSparing(formueFrontend.beskrivelseAvAnnet != null ? formueFrontend.beskrivelseAvAnnet : "");
    }

    private void setBekreftelseOnFormueFrontend(JsonOkonomiopplysninger opplysninger, FormueFrontend formueFrontend) {
        final Optional<JsonOkonomibekreftelse> formueBekreftelse = opplysninger.getBekreftelse().stream()
                .filter(bekreftelse -> bekreftelse.getType().equals("sparing")).findFirst();
        if (formueBekreftelse.isPresent()){
            formueFrontend.withBekreftelse(formueBekreftelse.get().getVerdi());
        }
    }

    private void setFormuetyperOnFormueFrontend(JsonOkonomioversikt oversikt, FormueFrontend formueFrontend) {
        oversikt.getFormue().forEach(
                formue -> {
                    switch(formue.getType()){
                        case "brukskonto":
                            formueFrontend.withBrukskonto(true);
                            break;
                        case "bsu":
                            formueFrontend.withBsu(true);
                            break;
                        case "sparekonto":
                            formueFrontend.withSparekonto(true);
                            break;
                        case "livsforsikring":
                            formueFrontend.withLivsforsikring(true);
                            break;
                        case "verdipapirer":
                            formueFrontend.withVerdipapirer(true);
                            break;
                        case "belop":
                            formueFrontend.withAnnet(true);
                            break;
                    }
                });
    }

    private String getJsonOkonomiTittel(String key) {
        Properties properties = navMessageSource.getBundleFor("sendsoknad", new Locale("nb", "NO"));

        return properties.getProperty("json.okonomi." + key);
    }

    @XmlAccessorType(XmlAccessType.FIELD)
    public static final class FormueFrontend {
        public Boolean bekreftelse;
        public boolean brukskonto;
        public boolean sparekonto;
        public boolean bsu;
        public boolean livsforsikring;
        public boolean verdipapirer;
        public boolean annet;
        public String beskrivelseAvAnnet;

        public FormueFrontend withBekreftelse(Boolean bekreftelse) {
            this.bekreftelse = bekreftelse;
            return this;
        }

        public FormueFrontend withBrukskonto(boolean brukskonto) {
            this.brukskonto = brukskonto;
            return this;
        }

        public FormueFrontend withSparekonto(boolean sparekonto) {
            this.sparekonto = sparekonto;
            return this;
        }

        public FormueFrontend withBsu(boolean bsu) {
            this.bsu = bsu;
            return this;
        }

        public FormueFrontend withLivsforsikring(boolean livsforsikring) {
            this.livsforsikring = livsforsikring;
            return this;
        }

        public FormueFrontend withVerdipapirer(boolean verdipapirer) {
            this.verdipapirer = verdipapirer;
            return this;
        }

        public FormueFrontend withAnnet(boolean annet) {
            this.annet = annet;
            return this;
        }



        public FormueFrontend withBeskrivelseAvAnnet(String beskrivelseAvAnnet) {
            this.beskrivelseAvAnnet = beskrivelseAvAnnet;
            return this;
        }
    }
}
