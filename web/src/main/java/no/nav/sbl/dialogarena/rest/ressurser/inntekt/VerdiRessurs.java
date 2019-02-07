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
import static no.nav.sbl.dialogarena.rest.mappers.OkonomiMapper.addFormueIfNotPresentInOversikt;
import static no.nav.sbl.dialogarena.rest.mappers.OkonomiMapper.setBekreftelse;
import static no.nav.sbl.dialogarena.rest.mappers.FaktumNoklerOgBelopNavnMapper.jsonTypeToFaktumKey;

@Controller
@Path("/soknader/{behandlingsId}/inntekt/verdier")
@Timed
@Produces(APPLICATION_JSON)
public class VerdiRessurs {

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
    public VerdierFrontend hentVerdier(@PathParam("behandlingsId") String behandlingsId){
        final String eier = SubjectHandler.getSubjectHandler().getUid();
        final JsonInternalSoknad soknad = legacyHelper.hentSoknad(behandlingsId, eier).getJsonInternalSoknad();
        final JsonOkonomiopplysninger opplysninger = soknad.getSoknad().getData().getOkonomi().getOpplysninger();
        final JsonOkonomioversikt oversikt = soknad.getSoknad().getData().getOkonomi().getOversikt();
        final VerdierFrontend verdierFrontend = new VerdierFrontend();

        if (opplysninger.getBekreftelse() == null){
            return verdierFrontend;
        }

        setBekreftelseOnVerdierFrontend(opplysninger, verdierFrontend);
        setVerdityperOnVerdierFrontend(oversikt, verdierFrontend);

        if (opplysninger.getBeskrivelseAvAnnet() != null){
            verdierFrontend.withBeskrivelseAvAnnet(opplysninger.getBeskrivelseAvAnnet().getVerdi());
        }

        return verdierFrontend;
    }

    @PUT
    public void updateVerdier(@PathParam("behandlingsId") String behandlingsId, VerdierFrontend verdierFrontend){
        tilgangskontroll.verifiserAtBrukerKanEndreSoknad(behandlingsId);
        update(behandlingsId, verdierFrontend);
        legacyUpdate(behandlingsId, verdierFrontend);
    }

    private void update(String behandlingsId, VerdierFrontend verdierFrontend) {
        final String eier = SubjectHandler.getSubjectHandler().getUid();
        final SoknadUnderArbeid soknad = soknadUnderArbeidRepository.hentSoknad(behandlingsId, eier).get();
        final JsonOkonomiopplysninger opplysninger = soknad.getJsonInternalSoknad().getSoknad().getData().getOkonomi().getOpplysninger();
        final JsonOkonomioversikt oversikt = soknad.getJsonInternalSoknad().getSoknad().getData().getOkonomi().getOversikt();

        if (opplysninger.getBekreftelse() == null){
            opplysninger.setBekreftelse(new ArrayList<>());
        }

        setBekreftelse(opplysninger, "verdi", verdierFrontend.bekreftelse, getJsonOkonomiTittel("inntekt.eierandeler"));
        setVerdier(oversikt, verdierFrontend);
        setBeskrivelseAvAnnet(opplysninger, verdierFrontend);

        soknadUnderArbeidRepository.oppdaterSoknadsdata(soknad, eier);
    }

    private void legacyUpdate(String behandlingsId, VerdierFrontend verdierFrontend) {
        final WebSoknad webSoknad = soknadService.hentSoknad(behandlingsId, false, false);

        final Faktum bekreftelse = faktaService.hentFaktumMedKey(webSoknad.getSoknadId(), "inntekt.eierandeler");
        bekreftelse.setValue(verdierFrontend.bekreftelse.toString());
        faktaService.lagreBrukerFaktum(bekreftelse);

        final Faktum bolig = faktaService.hentFaktumMedKey(webSoknad.getSoknadId(), "inntekt.eierandeler.true.type.bolig");
        bolig.setValue(String.valueOf(verdierFrontend.bolig));
        faktaService.lagreBrukerFaktum(bolig);

        final Faktum campingvogn = faktaService.hentFaktumMedKey(webSoknad.getSoknadId(), "inntekt.eierandeler.true.type.campingvogn");
        campingvogn.setValue(String.valueOf(verdierFrontend.campingvogn));
        faktaService.lagreBrukerFaktum(campingvogn);

        final Faktum kjoretoy = faktaService.hentFaktumMedKey(webSoknad.getSoknadId(), "inntekt.eierandeler.true.type.kjoretoy");
        kjoretoy.setValue(String.valueOf(verdierFrontend.kjoretoy));
        faktaService.lagreBrukerFaktum(kjoretoy);

        final Faktum fritidseiendom = faktaService.hentFaktumMedKey(webSoknad.getSoknadId(), "inntekt.eierandeler.true.type.fritidseiendom");
        fritidseiendom.setValue(String.valueOf(verdierFrontend.fritidseiendom));
        faktaService.lagreBrukerFaktum(fritidseiendom);

        final Faktum annet = faktaService.hentFaktumMedKey(webSoknad.getSoknadId(), "inntekt.eierandeler.true.type.annet");
        annet.setValue(String.valueOf(verdierFrontend.annet));
        faktaService.lagreBrukerFaktum(annet);

        final Faktum beskrivelse = faktaService.hentFaktumMedKey(webSoknad.getSoknadId(), "inntekt.eierandeler.true.type.annet.true.beskrivelse");
        beskrivelse.setValue(verdierFrontend.beskrivelseAvAnnet != null ? verdierFrontend.beskrivelseAvAnnet : "");
        faktaService.lagreBrukerFaktum(beskrivelse);
    }

    private void setVerdier(JsonOkonomioversikt oversikt, VerdierFrontend verdierFrontend) {
        final List<JsonOkonomioversiktFormue> verdier = oversikt.getFormue();

        if(verdierFrontend.bolig){
            final String type = "bolig";
            final String tittel = getJsonOkonomiTittel(jsonTypeToFaktumKey.get(type));
            addFormueIfNotPresentInOversikt(verdier, type, tittel);
        }
        if(verdierFrontend.campingvogn){
            final String type = "campingvogn";
            final String tittel = getJsonOkonomiTittel(jsonTypeToFaktumKey.get(type));
            addFormueIfNotPresentInOversikt(verdier, type, tittel);
        }
        if(verdierFrontend.kjoretoy){
            final String type = "kjoretoy";
            final String tittel = getJsonOkonomiTittel(jsonTypeToFaktumKey.get(type));
            addFormueIfNotPresentInOversikt(verdier, type, tittel);
        }
        if(verdierFrontend.fritidseiendom){
            final String type = "fritidseiendom";
            final String tittel = getJsonOkonomiTittel(jsonTypeToFaktumKey.get(type));
            addFormueIfNotPresentInOversikt(verdier, type, tittel);
        }
        if(verdierFrontend.annet){
            final String type = "annet";
            final String tittel = getJsonOkonomiTittel(jsonTypeToFaktumKey.get(type));
            addFormueIfNotPresentInOversikt(verdier, type, tittel);
        }
    }

    private void setBeskrivelseAvAnnet(JsonOkonomiopplysninger opplysninger, VerdierFrontend verdierFrontend) {
        if (opplysninger.getBeskrivelseAvAnnet() == null){
            opplysninger.withBeskrivelseAvAnnet(new JsonOkonomibeskrivelserAvAnnet()
                    .withKilde(JsonKildeBruker.BRUKER)
                    .withVerdi("")
                    .withSparing("")
                    .withUtbetaling("")
                    .withBoutgifter("")
                    .withBarneutgifter(""));
        }
        opplysninger.getBeskrivelseAvAnnet().setVerdi(verdierFrontend.beskrivelseAvAnnet != null ? verdierFrontend.beskrivelseAvAnnet : "");
    }

    private void setBekreftelseOnVerdierFrontend(JsonOkonomiopplysninger opplysninger, VerdierFrontend verdierFrontend) {
        final Optional<JsonOkonomibekreftelse> verdiBekreftelse = opplysninger.getBekreftelse().stream()
                .filter(bekreftelse -> bekreftelse.getType().equals("verdi")).findFirst();
        if (verdiBekreftelse.isPresent()){
            verdierFrontend.withBekreftelse(verdiBekreftelse.get().getVerdi());
        }
    }

    private void setVerdityperOnVerdierFrontend(JsonOkonomioversikt oversikt, VerdierFrontend verdierFrontend) {
        oversikt.getFormue().forEach(
                formue -> {
                    switch(formue.getType()){
                        case "bolig":
                            verdierFrontend.withBolig(true);
                            break;
                        case "campingvogn":
                            verdierFrontend.withCampingvogn(true);
                            break;
                        case "kjoretoy":
                            verdierFrontend.withKjoretoy(true);
                            break;
                        case "fritidseiendom":
                            verdierFrontend.withFritidseiendom(true);
                            break;
                        case "annet":
                            verdierFrontend.withAnnet(true);
                            break;
                    }
                });
    }

    private String getJsonOkonomiTittel(String key) {
        Properties properties = navMessageSource.getBundleFor("sendsoknad", new Locale("nb", "NO"));

        return properties.getProperty("json.okonomi." + key);
    }

    @XmlAccessorType(XmlAccessType.FIELD)
    public static final class VerdierFrontend {
        public Boolean bekreftelse;
        public boolean bolig;
        public boolean campingvogn;
        public boolean kjoretoy;
        public boolean fritidseiendom;
        public boolean annet;
        public String beskrivelseAvAnnet;

        public VerdierFrontend withBekreftelse(Boolean bekreftelse) {
            this.bekreftelse = bekreftelse;
            return this;
        }

        public VerdierFrontend withBolig(boolean bolig) {
            this.bolig = bolig;
            return this;
        }

        public VerdierFrontend withCampingvogn(boolean campingvogn) {
            this.campingvogn = campingvogn;
            return this;
        }

        public VerdierFrontend withKjoretoy(boolean kjoretoy) {
            this.kjoretoy = kjoretoy;
            return this;
        }

        public VerdierFrontend withFritidseiendom(boolean fritidseiendom) {
            this.fritidseiendom = fritidseiendom;
            return this;
        }

        public VerdierFrontend withAnnet(boolean annet) {
            this.annet = annet;
            return this;
        }

        public VerdierFrontend withBeskrivelseAvAnnet(String beskrivelseAvAnnet) {
            this.beskrivelseAvAnnet = beskrivelseAvAnnet;
            return this;
        }
    }
}
