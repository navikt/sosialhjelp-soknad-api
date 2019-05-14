package no.nav.sbl.dialogarena.rest.ressurser.inntekt;

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
import no.nav.sbl.soknadsosialhjelp.soknad.common.JsonKildeBruker;
import no.nav.sbl.soknadsosialhjelp.soknad.okonomi.JsonOkonomi;
import no.nav.sbl.soknadsosialhjelp.soknad.okonomi.JsonOkonomiopplysninger;
import no.nav.sbl.soknadsosialhjelp.soknad.okonomi.JsonOkonomioversikt;
import no.nav.sbl.soknadsosialhjelp.soknad.okonomi.opplysning.JsonOkonomibeskrivelserAvAnnet;
import no.nav.sbl.soknadsosialhjelp.soknad.okonomi.oversikt.JsonOkonomioversiktFormue;
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

import static javax.ws.rs.core.MediaType.APPLICATION_JSON;
import static no.nav.sbl.dialogarena.soknadinnsending.business.mappers.FaktumNoklerOgBelopNavnMapper.soknadTypeToFaktumKey;
import static no.nav.sbl.dialogarena.soknadinnsending.business.mappers.OkonomiMapper.*;

@Controller
@ProtectedWithClaims(issuer = "selvbetjening", claimMap = { "acr=Level4" })
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
    private TextService textService;

    @Inject
    private SoknadService soknadService;

    @Inject
    private FaktaService faktaService;

    @GET
    public FormueFrontend hentFormue(@PathParam("behandlingsId") String behandlingsId){
        final String eier = OidcFeatureToggleUtils.getUserId();
        final JsonInternalSoknad soknad = legacyHelper.hentSoknad(behandlingsId, eier, false).getJsonInternalSoknad();
        final JsonOkonomi okonomi = soknad.getSoknad().getData().getOkonomi();
        final FormueFrontend formueFrontend = new FormueFrontend();

        if (okonomi.getOpplysninger().getBekreftelse() == null){
            return formueFrontend;
        }

        setFormuetyperOnFormueFrontend(okonomi.getOversikt(), formueFrontend);

        if (okonomi.getOpplysninger().getBeskrivelseAvAnnet() != null){
            formueFrontend.setBeskrivelseAvAnnet(okonomi.getOpplysninger().getBeskrivelseAvAnnet().getSparing());
        }

        return formueFrontend;
    }

    @PUT
    public void updateFormue(@PathParam("behandlingsId") String behandlingsId, FormueFrontend formueFrontend){
        tilgangskontroll.verifiserAtBrukerKanEndreSoknad(behandlingsId);
        update(behandlingsId, formueFrontend);
        legacyUpdate(behandlingsId, formueFrontend);
    }

    private void update(String behandlingsId, FormueFrontend formueFrontend) {
        final String eier = OidcFeatureToggleUtils.getUserId();
        final SoknadUnderArbeid soknad = soknadUnderArbeidRepository.hentSoknad(behandlingsId, eier).get();
        final JsonOkonomi okonomi = soknad.getJsonInternalSoknad().getSoknad().getData().getOkonomi();

        if (okonomi.getOpplysninger().getBekreftelse() == null){
            okonomi.getOpplysninger().setBekreftelse(new ArrayList<>());
        }

        boolean hasAnyFormueType = formueFrontend.brukskonto || formueFrontend.bsu || formueFrontend.sparekonto ||
                formueFrontend.livsforsikring || formueFrontend.verdipapirer || formueFrontend.annet;

        setBekreftelse(okonomi.getOpplysninger(), "sparing", hasAnyFormueType, textService.getJsonOkonomiTittel("inntekt.bankinnskudd"));
        setFormue(okonomi.getOversikt(), formueFrontend);
        setBeskrivelseAvAnnet(okonomi.getOpplysninger(), formueFrontend);

        soknadUnderArbeidRepository.oppdaterSoknadsdata(soknad, eier);
    }

    private void legacyUpdate(String behandlingsId, FormueFrontend formueFrontend) {
        final WebSoknad webSoknad = soknadService.hentSoknad(behandlingsId, false, false);

        if (formueFrontend.brukskonto || formueFrontend.bsu || formueFrontend.sparekonto ||
                formueFrontend.livsforsikring || formueFrontend.verdipapirer || formueFrontend.annet){
            final Faktum bekreftelse = faktaService.hentFaktumMedKey(webSoknad.getSoknadId(), "inntekt.bankinnskudd");
            bekreftelse.setValue(String.valueOf(true));
            faktaService.lagreBrukerFaktum(bekreftelse);
        } else {
            final Faktum bekreftelse = faktaService.hentFaktumMedKey(webSoknad.getSoknadId(), "inntekt.bankinnskudd");
            bekreftelse.setValue(String.valueOf(false));
            faktaService.lagreBrukerFaktum(bekreftelse);
        }

        final Faktum brukskonto = faktaService.hentFaktumMedKey(webSoknad.getSoknadId(), "inntekt.bankinnskudd.true.type.brukskonto");
        brukskonto.setValue(String.valueOf(formueFrontend.brukskonto));
        faktaService.lagreBrukerFaktum(brukskonto);

        final Faktum bsu = faktaService.hentFaktumMedKey(webSoknad.getSoknadId(), "inntekt.bankinnskudd.true.type.bsu");
        bsu.setValue(String.valueOf(formueFrontend.bsu));
        faktaService.lagreBrukerFaktum(bsu);

        final Faktum sparekonto = faktaService.hentFaktumMedKey(webSoknad.getSoknadId(), "inntekt.bankinnskudd.true.type.sparekonto");
        sparekonto.setValue(String.valueOf(formueFrontend.sparekonto));
        faktaService.lagreBrukerFaktum(sparekonto);

        final Faktum livsforsikring = faktaService.hentFaktumMedKey(webSoknad.getSoknadId(), "inntekt.bankinnskudd.true.type.livsforsikring");
        livsforsikring.setValue(String.valueOf(formueFrontend.livsforsikring));
        faktaService.lagreBrukerFaktum(livsforsikring);

        final Faktum aksjer = faktaService.hentFaktumMedKey(webSoknad.getSoknadId(), "inntekt.bankinnskudd.true.type.aksjer");
        aksjer.setValue(String.valueOf(formueFrontend.verdipapirer));
        faktaService.lagreBrukerFaktum(aksjer);

        final Faktum annet = faktaService.hentFaktumMedKey(webSoknad.getSoknadId(), "inntekt.bankinnskudd.true.type.annet");
        annet.setValue(String.valueOf(formueFrontend.annet));
        faktaService.lagreBrukerFaktum(annet);

        final Faktum beskrivelse = faktaService.hentFaktumMedKey(webSoknad.getSoknadId(), "inntekt.bankinnskudd.true.type.annet.true.beskrivelse");
        beskrivelse.setValue(formueFrontend.beskrivelseAvAnnet != null ? formueFrontend.beskrivelseAvAnnet : "");
        faktaService.lagreBrukerFaktum(beskrivelse);
    }

    private void setFormue(JsonOkonomioversikt oversikt, FormueFrontend formueFrontend) {
        final List<JsonOkonomioversiktFormue> formue = oversikt.getFormue();

        String type = "brukskonto";
        String tittel = textService.getJsonOkonomiTittel(soknadTypeToFaktumKey.get(type));
        addFormueIfCheckedElseDeleteInOversikt(formue, type, tittel, formueFrontend.brukskonto);

        type = "bsu";
        tittel = textService.getJsonOkonomiTittel(soknadTypeToFaktumKey.get(type));
        addFormueIfCheckedElseDeleteInOversikt(formue, type, tittel, formueFrontend.bsu);

        type = "livsforsikringssparedel";
        tittel = textService.getJsonOkonomiTittel(soknadTypeToFaktumKey.get(type));
        addFormueIfCheckedElseDeleteInOversikt(formue, type, tittel, formueFrontend.livsforsikring);

        type = "sparekonto";
        tittel = textService.getJsonOkonomiTittel(soknadTypeToFaktumKey.get(type));
        addFormueIfCheckedElseDeleteInOversikt(formue, type, tittel, formueFrontend.sparekonto);

        type = "verdipapirer";
        tittel = textService.getJsonOkonomiTittel(soknadTypeToFaktumKey.get(type));
        addFormueIfCheckedElseDeleteInOversikt(formue, type, tittel, formueFrontend.verdipapirer);

        type = "belop";
        tittel = textService.getJsonOkonomiTittel(soknadTypeToFaktumKey.get(type));
        addFormueIfCheckedElseDeleteInOversikt(formue, type, tittel, formueFrontend.annet);
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

    private void setFormuetyperOnFormueFrontend(JsonOkonomioversikt oversikt, FormueFrontend formueFrontend) {
        oversikt.getFormue().forEach(
                formue -> {
                    switch(formue.getType()){
                        case "brukskonto":
                            formueFrontend.setBrukskonto(true);
                            break;
                        case "bsu":
                            formueFrontend.setBsu(true);
                            break;
                        case "sparekonto":
                            formueFrontend.setSparekonto(true);
                            break;
                        case "livsforsikringssparedel":
                            formueFrontend.setLivsforsikring(true);
                            break;
                        case "verdipapirer":
                            formueFrontend.setVerdipapirer(true);
                            break;
                        case "belop":
                            formueFrontend.setAnnet(true);
                            break;
                    }
                });
    }

    @XmlAccessorType(XmlAccessType.FIELD)
    public static final class FormueFrontend {
        public boolean brukskonto;
        public boolean sparekonto;
        public boolean bsu;
        public boolean livsforsikring;
        public boolean verdipapirer;
        public boolean annet;
        public String beskrivelseAvAnnet;

        public void setBrukskonto(boolean brukskonto) {
            this.brukskonto = brukskonto;
        }

        public void setSparekonto(boolean sparekonto) {
            this.sparekonto = sparekonto;
        }

        public void setBsu(boolean bsu) {
            this.bsu = bsu;
        }

        public void setLivsforsikring(boolean livsforsikring) {
            this.livsforsikring = livsforsikring;
        }

        public void setVerdipapirer(boolean verdipapirer) {
            this.verdipapirer = verdipapirer;
        }

        public void setAnnet(boolean annet) {
            this.annet = annet;
        }

        public void setBeskrivelseAvAnnet(String beskrivelseAvAnnet) {
            this.beskrivelseAvAnnet = beskrivelseAvAnnet;
        }
    }
}
