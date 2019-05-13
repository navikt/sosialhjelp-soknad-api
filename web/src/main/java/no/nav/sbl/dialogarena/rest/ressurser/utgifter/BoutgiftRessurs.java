package no.nav.sbl.dialogarena.rest.ressurser.utgifter;

import no.nav.metrics.aspects.Timed;
import no.nav.sbl.dialogarena.sendsoknad.domain.oidc.OidcFeatureToggleUtils;
import no.nav.sbl.dialogarena.rest.ressurser.LegacyHelper;
import no.nav.sbl.dialogarena.sendsoknad.domain.Faktum;
import no.nav.sbl.dialogarena.sendsoknad.domain.WebSoknad;
import no.nav.sbl.dialogarena.sikkerhet.Tilgangskontroll;
import no.nav.sbl.dialogarena.soknadinnsending.business.service.FaktaService;
import no.nav.sbl.dialogarena.soknadinnsending.business.service.TextService;
import no.nav.sbl.dialogarena.soknadinnsending.business.service.soknadservice.SoknadService;
import no.nav.sbl.soknadsosialhjelp.soknad.JsonInternalSoknad;
import no.nav.sbl.soknadsosialhjelp.soknad.okonomi.JsonOkonomi;
import no.nav.sbl.soknadsosialhjelp.soknad.okonomi.JsonOkonomiopplysninger;
import no.nav.sbl.soknadsosialhjelp.soknad.okonomi.opplysning.JsonOkonomiOpplysningUtgift;
import no.nav.sbl.soknadsosialhjelp.soknad.okonomi.opplysning.JsonOkonomibekreftelse;
import no.nav.sbl.soknadsosialhjelp.soknad.okonomi.oversikt.JsonOkonomioversiktUtgift;
import no.nav.sbl.sosialhjelp.domain.SoknadUnderArbeid;
import no.nav.sbl.sosialhjelp.soknadunderbehandling.SoknadUnderArbeidRepository;
import no.nav.security.oidc.api.ProtectedWithClaims;
import org.springframework.stereotype.Controller;

import javax.inject.Inject;
import javax.ws.rs.*;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static javax.ws.rs.core.MediaType.APPLICATION_JSON;
import static no.nav.sbl.dialogarena.soknadinnsending.business.mappers.FaktumNoklerOgBelopNavnMapper.soknadTypeToFaktumKey;
import static no.nav.sbl.dialogarena.soknadinnsending.business.mappers.OkonomiMapper.*;

@Controller
@ProtectedWithClaims(issuer = "selvbetjening", claimMap = { "acr=Level4" })
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
    private SoknadService soknadService;

    @Inject
    private FaktaService faktaService;

    @Inject
    private TextService textService;

    @GET
    public BoutgifterFrontend hentBoutgifter(@PathParam("behandlingsId") String behandlingsId){
        final String eier = OidcFeatureToggleUtils.getUserId();
        final JsonInternalSoknad soknad = legacyHelper.hentSoknad(behandlingsId, eier, false).getJsonInternalSoknad();
        final JsonOkonomi okonomi = soknad.getSoknad().getData().getOkonomi();
        final BoutgifterFrontend boutgifterFrontend = new BoutgifterFrontend();

        if (okonomi.getOpplysninger().getBekreftelse() == null){
            return boutgifterFrontend;
        }

        setBekreftelseOnBoutgifterFrontend(okonomi.getOpplysninger(), boutgifterFrontend);
        setUtgiftstyperOnBoutgifterFrontend(okonomi, boutgifterFrontend);

        return boutgifterFrontend;
    }

    @PUT
    public void updateBoutgifter(@PathParam("behandlingsId") String behandlingsId, BoutgifterFrontend boutgifterFrontend){
        tilgangskontroll.verifiserAtBrukerKanEndreSoknad(behandlingsId);
        update(behandlingsId, boutgifterFrontend);
        legacyUpdate(behandlingsId, boutgifterFrontend);
    }

    private void update(String behandlingsId, BoutgifterFrontend boutgifterFrontend) {
        final String eier = OidcFeatureToggleUtils.getUserId();
        final SoknadUnderArbeid soknad = soknadUnderArbeidRepository.hentSoknad(behandlingsId, eier).get();
        final JsonOkonomi okonomi = soknad.getJsonInternalSoknad().getSoknad().getData().getOkonomi();

        if (okonomi.getOpplysninger().getBekreftelse() == null){
            okonomi.getOpplysninger().setBekreftelse(new ArrayList<>());
        }

        setBekreftelse(okonomi.getOpplysninger(), "boutgifter", boutgifterFrontend.bekreftelse, textService.getJsonOkonomiTittel("utgifter.boutgift"));
        setBoutgifter(okonomi, boutgifterFrontend);

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
    }

    private void setBoutgifter(JsonOkonomi okonomi, BoutgifterFrontend boutgifterFrontend) {
        List<JsonOkonomiOpplysningUtgift> opplysningerBoutgifter = okonomi.getOpplysninger().getUtgift();
        List<JsonOkonomioversiktUtgift> oversiktBoutgifter = okonomi.getOversikt().getUtgift();

        String type = "husleie";
        String tittel = textService.getJsonOkonomiTittel(soknadTypeToFaktumKey.get(type));
        addutgiftIfCheckedElseDeleteInOversikt(oversiktBoutgifter, type, tittel, boutgifterFrontend.husleie);

        type = "strom";
        tittel = textService.getJsonOkonomiTittel(soknadTypeToFaktumKey.get(type));
        addutgiftIfCheckedElseDeleteInOpplysninger(opplysningerBoutgifter, type, tittel, boutgifterFrontend.strom);

        type = "kommunalAvgift";
        tittel = textService.getJsonOkonomiTittel(soknadTypeToFaktumKey.get(type));
        addutgiftIfCheckedElseDeleteInOpplysninger(opplysningerBoutgifter, type, tittel, boutgifterFrontend.kommunalAvgift);

        type = "oppvarming";
        tittel = textService.getJsonOkonomiTittel(soknadTypeToFaktumKey.get(type));
        addutgiftIfCheckedElseDeleteInOpplysninger(opplysningerBoutgifter, type, tittel, boutgifterFrontend.oppvarming);

        type = "boliglanAvdrag";
        tittel = textService.getJsonOkonomiTittel(soknadTypeToFaktumKey.get(type) + "." + type);
        addutgiftIfCheckedElseDeleteInOversikt(oversiktBoutgifter, type, tittel, boutgifterFrontend.boliglan);

        type = "boliglanRenter";
        tittel = textService.getJsonOkonomiTittel(soknadTypeToFaktumKey.get(type) + "." + type);
        addutgiftIfCheckedElseDeleteInOversikt(oversiktBoutgifter, type, tittel, boutgifterFrontend.boliglan);

        type = "annenBoutgift";
        tittel = textService.getJsonOkonomiTittel("opplysninger.inntekt.inntekter.annet");
        addutgiftIfCheckedElseDeleteInOpplysninger(opplysningerBoutgifter, type, tittel, boutgifterFrontend.annet);
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

    @XmlAccessorType(XmlAccessType.FIELD)
    public static final class BoutgifterFrontend {
        public Boolean bekreftelse;
        public boolean husleie;
        public boolean strom;
        public boolean kommunalAvgift;
        public boolean oppvarming;
        public boolean boliglan;
        public boolean annet;

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
    }
}
