package no.nav.sbl.dialogarena.rest.ressurser.inntekt;

import no.nav.metrics.aspects.Timed;
import no.nav.sbl.dialogarena.sendsoknad.domain.oidc.OidcFeatureToggleUtils;
import no.nav.sbl.dialogarena.sikkerhet.Tilgangskontroll;
import no.nav.sbl.dialogarena.soknadinnsending.business.service.TextService;
import no.nav.sbl.soknadsosialhjelp.soknad.JsonInternalSoknad;
import no.nav.sbl.soknadsosialhjelp.soknad.common.JsonKildeBruker;
import no.nav.sbl.soknadsosialhjelp.soknad.okonomi.JsonOkonomi;
import no.nav.sbl.soknadsosialhjelp.soknad.okonomi.JsonOkonomiopplysninger;
import no.nav.sbl.soknadsosialhjelp.soknad.okonomi.JsonOkonomioversikt;
import no.nav.sbl.soknadsosialhjelp.soknad.okonomi.opplysning.JsonOkonomibekreftelse;
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
import java.util.Optional;

import static javax.ws.rs.core.MediaType.APPLICATION_JSON;
import static no.nav.sbl.dialogarena.soknadinnsending.business.mappers.FaktumNoklerOgBelopNavnMapper.soknadTypeToFaktumKey;
import static no.nav.sbl.dialogarena.soknadinnsending.business.mappers.OkonomiMapper.addFormueIfCheckedElseDeleteInOversikt;
import static no.nav.sbl.dialogarena.soknadinnsending.business.mappers.OkonomiMapper.setBekreftelse;

@Controller
@ProtectedWithClaims(issuer = "selvbetjening", claimMap = { "acr=Level4" })
@Path("/soknader/{behandlingsId}/inntekt/verdier")
@Timed
@Produces(APPLICATION_JSON)
public class VerdiRessurs {

    @Inject
    private Tilgangskontroll tilgangskontroll;

    @Inject
    private SoknadUnderArbeidRepository soknadUnderArbeidRepository;

    @Inject
    private TextService textService;

    @GET
    public VerdierFrontend hentVerdier(@PathParam("behandlingsId") String behandlingsId){
        final String eier = OidcFeatureToggleUtils.getUserId();
        final JsonInternalSoknad soknad = soknadUnderArbeidRepository.hentSoknad(behandlingsId, eier).get().getJsonInternalSoknad();
        final JsonOkonomi okonomi = soknad.getSoknad().getData().getOkonomi();
        final VerdierFrontend verdierFrontend = new VerdierFrontend();

        if (okonomi.getOpplysninger().getBekreftelse() == null){
            return verdierFrontend;
        }

        setBekreftelseOnVerdierFrontend(okonomi.getOpplysninger(), verdierFrontend);
        setVerdityperOnVerdierFrontend(okonomi.getOversikt(), verdierFrontend);

        if (okonomi.getOpplysninger().getBeskrivelseAvAnnet() != null){
            verdierFrontend.setBeskrivelseAvAnnet(okonomi.getOpplysninger().getBeskrivelseAvAnnet().getVerdi());
        }

        return verdierFrontend;
    }

    @PUT
    public void updateVerdier(@PathParam("behandlingsId") String behandlingsId, VerdierFrontend verdierFrontend){
        tilgangskontroll.verifiserAtBrukerKanEndreSoknad(behandlingsId);
        final String eier = OidcFeatureToggleUtils.getUserId();
        final SoknadUnderArbeid soknad = soknadUnderArbeidRepository.hentSoknad(behandlingsId, eier).get();
        final JsonOkonomi okonomi = soknad.getJsonInternalSoknad().getSoknad().getData().getOkonomi();

        if (okonomi.getOpplysninger().getBekreftelse() == null){
            okonomi.getOpplysninger().setBekreftelse(new ArrayList<>());
        }

        setBekreftelse(okonomi.getOpplysninger(), "verdi", verdierFrontend.bekreftelse, textService.getJsonOkonomiTittel("inntekt.eierandeler"));
        setVerdier(okonomi.getOversikt(), verdierFrontend);
        setBeskrivelseAvAnnet(okonomi.getOpplysninger(), verdierFrontend);

        soknadUnderArbeidRepository.oppdaterSoknadsdata(soknad, eier);
    }

    private void setVerdier(JsonOkonomioversikt oversikt, VerdierFrontend verdierFrontend) {
        final List<JsonOkonomioversiktFormue> verdier = oversikt.getFormue();

        String type = "bolig";
        String tittel = textService.getJsonOkonomiTittel(soknadTypeToFaktumKey.get(type));
        addFormueIfCheckedElseDeleteInOversikt(verdier, type, tittel, verdierFrontend.bolig);

        type = "campingvogn";
        tittel = textService.getJsonOkonomiTittel(soknadTypeToFaktumKey.get(type));
        addFormueIfCheckedElseDeleteInOversikt(verdier, type, tittel, verdierFrontend.campingvogn);

        type = "kjoretoy";
        tittel = textService.getJsonOkonomiTittel(soknadTypeToFaktumKey.get(type));
        addFormueIfCheckedElseDeleteInOversikt(verdier, type, tittel, verdierFrontend.kjoretoy);

        type = "fritidseiendom";
        tittel = textService.getJsonOkonomiTittel(soknadTypeToFaktumKey.get(type));
        addFormueIfCheckedElseDeleteInOversikt(verdier, type, tittel, verdierFrontend.fritidseiendom);

        type = "annet";
        tittel = textService.getJsonOkonomiTittel(soknadTypeToFaktumKey.get(type));
        addFormueIfCheckedElseDeleteInOversikt(verdier, type, tittel, verdierFrontend.annet);
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
            verdierFrontend.setBekreftelse(verdiBekreftelse.get().getVerdi());
        }
    }

    private void setVerdityperOnVerdierFrontend(JsonOkonomioversikt oversikt, VerdierFrontend verdierFrontend) {
        oversikt.getFormue().forEach(
                formue -> {
                    switch(formue.getType()){
                        case "bolig":
                            verdierFrontend.setBolig(true);
                            break;
                        case "campingvogn":
                            verdierFrontend.setCampingvogn(true);
                            break;
                        case "kjoretoy":
                            verdierFrontend.setKjoretoy(true);
                            break;
                        case "fritidseiendom":
                            verdierFrontend.setFritidseiendom(true);
                            break;
                        case "annet":
                            verdierFrontend.setAnnet(true);
                            break;
                    }
                });
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

        public void setBekreftelse(Boolean bekreftelse) {
            this.bekreftelse = bekreftelse;
        }

        public void setBolig(boolean bolig) {
            this.bolig = bolig;
        }

        public void setCampingvogn(boolean campingvogn) {
            this.campingvogn = campingvogn;
        }

        public void setKjoretoy(boolean kjoretoy) {
            this.kjoretoy = kjoretoy;
        }

        public void setFritidseiendom(boolean fritidseiendom) {
            this.fritidseiendom = fritidseiendom;
        }

        public void setAnnet(boolean annet) {
            this.annet = annet;
        }

        public void setBeskrivelseAvAnnet(String beskrivelseAvAnnet) {
            this.beskrivelseAvAnnet = beskrivelseAvAnnet;
        }
    }
}
