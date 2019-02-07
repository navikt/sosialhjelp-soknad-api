package no.nav.sbl.dialogarena.rest.ressurser.utgifter;

import no.nav.metrics.aspects.Timed;
import no.nav.modig.core.context.SubjectHandler;
import no.nav.sbl.dialogarena.rest.ressurser.LegacyHelper;
import no.nav.sbl.dialogarena.sendsoknad.domain.Faktum;
import no.nav.sbl.dialogarena.sendsoknad.domain.WebSoknad;
import no.nav.sbl.dialogarena.sikkerhet.Tilgangskontroll;
import no.nav.sbl.dialogarena.soknadinnsending.business.service.FaktaService;
import no.nav.sbl.dialogarena.soknadinnsending.business.service.VedleggOriginalFilerService;
import no.nav.sbl.dialogarena.soknadinnsending.business.service.soknadservice.SoknadService;
import no.nav.sbl.dialogarena.soknadsosialhjelp.message.NavMessageSource;
import no.nav.sbl.soknadsosialhjelp.soknad.JsonInternalSoknad;
import no.nav.sbl.soknadsosialhjelp.soknad.common.JsonKildeBruker;
import no.nav.sbl.soknadsosialhjelp.soknad.okonomi.JsonOkonomi;
import no.nav.sbl.soknadsosialhjelp.soknad.okonomi.JsonOkonomiopplysninger;
import no.nav.sbl.soknadsosialhjelp.soknad.okonomi.opplysning.JsonOkonomiOpplysningUtgift;
import no.nav.sbl.soknadsosialhjelp.soknad.okonomi.opplysning.JsonOkonomibekreftelse;
import no.nav.sbl.soknadsosialhjelp.soknad.okonomi.opplysning.JsonOkonomibeskrivelserAvAnnet;
import no.nav.sbl.soknadsosialhjelp.soknad.okonomi.oversikt.JsonOkonomioversiktUtgift;
import no.nav.sbl.sosialhjelp.domain.SoknadUnderArbeid;
import no.nav.sbl.sosialhjelp.soknadunderbehandling.SoknadUnderArbeidRepository;
import org.springframework.stereotype.Controller;

import javax.inject.Inject;
import javax.ws.rs.*;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import java.util.*;

import static javax.ws.rs.core.MediaType.APPLICATION_JSON;
import static no.nav.sbl.dialogarena.rest.mappers.FaktumNoklerOgBelopNavnMapper.jsonTypeToFaktumKey;
import static no.nav.sbl.dialogarena.rest.mappers.OkonomiMapper.*;

@Controller
@Path("/soknader/{behandlingsId}/utgifter/boutgifter")
@Timed
@Produces(APPLICATION_JSON)
public class BoutgiftRessurs {

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

    @Inject
    private VedleggOriginalFilerService vedleggOriginalFilerService;

    @GET
    public BoutgifterFrontend hentBoutgifter(@PathParam("behandlingsId") String behandlingsId){
        vedleggOriginalFilerService.oppdaterVedleggOgBelopFaktum(behandlingsId);

        final String eier = SubjectHandler.getSubjectHandler().getUid();
        final JsonInternalSoknad soknad = legacyHelper.hentSoknad(behandlingsId, eier).getJsonInternalSoknad();
        final JsonOkonomi okonomi = soknad.getSoknad().getData().getOkonomi();
        final BoutgifterFrontend boutgifterFrontend = new BoutgifterFrontend();

        if (okonomi.getOpplysninger().getBekreftelse() == null){
            return boutgifterFrontend;
        }

        setBekreftelseOnBoutgifterFrontend(okonomi.getOpplysninger(), boutgifterFrontend);
        setUtgiftstyperOnBoutgifterFrontend(okonomi, boutgifterFrontend);

        if (okonomi.getOpplysninger().getBeskrivelseAvAnnet() != null){
            boutgifterFrontend.setBeskrivelseAvAnnet(okonomi.getOpplysninger().getBeskrivelseAvAnnet().getUtbetaling());
        }

        return boutgifterFrontend;
    }

    @PUT
    public void updateBoutgifter(@PathParam("behandlingsId") String behandlingsId, BoutgifterFrontend boutgifterFrontend){
        tilgangskontroll.verifiserAtBrukerKanEndreSoknad(behandlingsId);
        update(behandlingsId, boutgifterFrontend);
        legacyUpdate(behandlingsId, boutgifterFrontend);
    }

    private void update(String behandlingsId, BoutgifterFrontend boutgifterFrontend) {
        final String eier = SubjectHandler.getSubjectHandler().getUid();
        final SoknadUnderArbeid soknad = soknadUnderArbeidRepository.hentSoknad(behandlingsId, eier).get();
        final JsonOkonomi okonomi = soknad.getJsonInternalSoknad().getSoknad().getData().getOkonomi();

        if (okonomi.getOpplysninger().getBekreftelse() == null){
            okonomi.getOpplysninger().setBekreftelse(new ArrayList<>());
        }

        setBekreftelse(okonomi.getOpplysninger(), "boutgifter", boutgifterFrontend.bekreftelse, getJsonOkonomiTittel("utgifter.boutgift"));
        setBoutgifter(okonomi, boutgifterFrontend);
        setBeskrivelseAvAnnet(okonomi.getOpplysninger(), boutgifterFrontend);

        soknadUnderArbeidRepository.oppdaterSoknadsdata(soknad, eier);
    }

    private void legacyUpdate(String behandlingsId, BoutgifterFrontend boutgifterFrontend) {
        final WebSoknad webSoknad = soknadService.hentSoknad(behandlingsId, false, false);

        final Faktum bekreftelse = faktaService.hentFaktumMedKey(webSoknad.getSoknadId(), "utgifter.boutgift");
        bekreftelse.setValue(boutgifterFrontend.bekreftelse.toString());
        faktaService.lagreBrukerFaktum(bekreftelse);

        final Faktum husleie = faktaService.hentFaktumMedKey(webSoknad.getSoknadId(), "utgifter.boutgift.true.type.husleie");
        husleie.setValue(String.valueOf(boutgifterFrontend.husleie));
        faktaService.lagreBrukerFaktum(husleie);

        final Faktum strom = faktaService.hentFaktumMedKey(webSoknad.getSoknadId(), "utgifter.boutgift.true.type.strom");
        strom.setValue(String.valueOf(boutgifterFrontend.strom));
        faktaService.lagreBrukerFaktum(strom);

        final Faktum kommunaleavgifter = faktaService.hentFaktumMedKey(webSoknad.getSoknadId(), "utgifter.boutgift.true.type.kommunaleavgifter");
        kommunaleavgifter.setValue(String.valueOf(boutgifterFrontend.kommunalAvgift));
        faktaService.lagreBrukerFaktum(kommunaleavgifter);

        final Faktum oppvarming = faktaService.hentFaktumMedKey(webSoknad.getSoknadId(), "utgifter.boutgift.true.type.oppvarming");
        oppvarming.setValue(String.valueOf(boutgifterFrontend.oppvarming));
        faktaService.lagreBrukerFaktum(oppvarming);

        final Faktum avdraglaan = faktaService.hentFaktumMedKey(webSoknad.getSoknadId(), "utgifter.boutgift.true.type.avdraglaan");
        avdraglaan.setValue(String.valueOf(boutgifterFrontend.boliglan));
        faktaService.lagreBrukerFaktum(avdraglaan);

        final Faktum annet = faktaService.hentFaktumMedKey(webSoknad.getSoknadId(), "utgifter.boutgift.true.type.andreutgifter");
        annet.setValue(String.valueOf(boutgifterFrontend.annet));
        faktaService.lagreBrukerFaktum(annet);

        final Faktum beskrivelse = faktaService.hentFaktumMedKey(webSoknad.getSoknadId(), "utgifter.boutgift.true.type.andreutgifter.true.beskrivelse");
        beskrivelse.setValue(boutgifterFrontend.beskrivelseAvAnnet != null ? boutgifterFrontend.beskrivelseAvAnnet : "");
        faktaService.lagreBrukerFaktum(beskrivelse);
    }

    private void setBoutgifter(JsonOkonomi okonomi, BoutgifterFrontend boutgifterFrontend) {
        List<JsonOkonomiOpplysningUtgift> opplysningerBoutgifter = okonomi.getOpplysninger().getUtgift();
        List<JsonOkonomioversiktUtgift> oversiktBoutgifter = okonomi.getOversikt().getUtgift();

        if(boutgifterFrontend.husleie){
            final String type = "husleie";
            final String tittel = getJsonOkonomiTittel(jsonTypeToFaktumKey.get(type));
            addUtgiftIfNotPresentInOversikt(oversiktBoutgifter, type, tittel);
        }
        if(boutgifterFrontend.strom){
            final String type = "strom";
            final String tittel = getJsonOkonomiTittel(jsonTypeToFaktumKey.get(type));
            addUtgiftIfNotPresentInOpplysninger(opplysningerBoutgifter, type, tittel);
        }
        if(boutgifterFrontend.kommunalAvgift){
            final String type = "kommunalAvgift";
            final String tittel = getJsonOkonomiTittel(jsonTypeToFaktumKey.get(type));
            addUtgiftIfNotPresentInOpplysninger(opplysningerBoutgifter, type, tittel);
        }
        if(boutgifterFrontend.oppvarming){
            final String type = "oppvarming";
            final String tittel = getJsonOkonomiTittel(jsonTypeToFaktumKey.get(type));
            addUtgiftIfNotPresentInOpplysninger(opplysningerBoutgifter, type, tittel);
        }
        if(boutgifterFrontend.boliglan){
            String type = "boliglanAvdrag";
            String tittel = getJsonOkonomiTittel(jsonTypeToFaktumKey.get(type) + ".boliglanAvdrag");
            addUtgiftIfNotPresentInOversikt(oversiktBoutgifter, type, tittel);
            type = "boliglanRenter";
            tittel = getJsonOkonomiTittel(jsonTypeToFaktumKey.get(type) + ".boliglanRenter");
            addUtgiftIfNotPresentInOversikt(oversiktBoutgifter, type, tittel);
        }
        if(boutgifterFrontend.annet){
            final String type = "annenBoutgift";
            final String tittel = getJsonOkonomiTittel("opplysninger.inntekt.inntekter.annet");
            addUtgiftIfNotPresentInOpplysninger(opplysningerBoutgifter, type, tittel);
        }
    }

    private void setBeskrivelseAvAnnet(JsonOkonomiopplysninger opplysninger, BoutgifterFrontend boutgifterFrontend) {
        if (opplysninger.getBeskrivelseAvAnnet() == null){
            opplysninger.withBeskrivelseAvAnnet(new JsonOkonomibeskrivelserAvAnnet()
                    .withKilde(JsonKildeBruker.BRUKER)
                    .withVerdi("")
                    .withSparing("")
                    .withUtbetaling("")
                    .withBoutgifter("")
                    .withBarneutgifter(""));
        }
        opplysninger.getBeskrivelseAvAnnet().setUtbetaling(boutgifterFrontend.beskrivelseAvAnnet != null ? boutgifterFrontend.beskrivelseAvAnnet : "");
    }

    private void setBekreftelseOnBoutgifterFrontend(JsonOkonomiopplysninger opplysninger, BoutgifterFrontend boutgifterFrontend) {
        final Optional<JsonOkonomibekreftelse> boutgiftBekreftelse = opplysninger.getBekreftelse().stream()
                .filter(bekreftelse -> bekreftelse.getType().equals("boutgifter")).findFirst();
        if (boutgiftBekreftelse.isPresent()){
            boutgifterFrontend.setBekreftelse(boutgiftBekreftelse.get().getVerdi());
        }
    }

    private void setUtgiftstyperOnBoutgifterFrontend(JsonOkonomi okonomi, BoutgifterFrontend boutgifterFrontend) {
        okonomi.getOpplysninger().getUtgift().forEach(utgift -> {
            switch(utgift.getType()){
                case "strom":
                    boutgifterFrontend.setStrom(true);
                    break;
                case "kommunalAvgift":
                    boutgifterFrontend.setKommunalAvgift(true);
                    break;
                case "oppvarming":
                    boutgifterFrontend.setOppvarming(true);
                    break;
                case "annenBoutgift":
                    boutgifterFrontend.setAnnet(true);
                    break;
            }
        });
        okonomi.getOversikt().getUtgift().forEach(
                utgift -> {
                    switch(utgift.getType()){
                        case "husleie":
                            boutgifterFrontend.setHusleie(true);
                            break;
                        case "boliglanAvdrag":
                            boutgifterFrontend.setBoliglan(true);
                            break;
                    }
                });
    }

    private String getJsonOkonomiTittel(String key) {
        Properties properties = navMessageSource.getBundleFor("sendsoknad", new Locale("nb", "NO"));

        return properties.getProperty("json.okonomi." + key);
    }

    @XmlAccessorType(XmlAccessType.FIELD)
    public static final class BoutgifterFrontend {
        public Boolean bekreftelse;
        public boolean husleie;
        public boolean strom;
        public boolean kommunalAvgift;
        public boolean oppvarming;
        public boolean boliglan;
        public boolean annet;
        public String beskrivelseAvAnnet;

        public void setBekreftelse(Boolean bekreftelse) {
            this.bekreftelse = bekreftelse;
        }

        public void setHusleie(boolean husleie) {
            this.husleie = husleie;
        }

        public void setStrom(boolean strom) {
            this.strom = strom;
        }

        public void setKommunalAvgift(boolean kommunalAvgift) {
            this.kommunalAvgift = kommunalAvgift;
        }

        public void setOppvarming(boolean oppvarming) {
            this.oppvarming = oppvarming;
        }

        public void setBoliglan(boolean boliglan) {
            this.boliglan = boliglan;
        }

        public void setAnnet(boolean annet) {
            this.annet = annet;
        }

        public void setBeskrivelseAvAnnet(String beskrivelseAvAnnet) {
            this.beskrivelseAvAnnet = beskrivelseAvAnnet;
        }
    }
}
