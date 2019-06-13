package no.nav.sbl.dialogarena.rest.ressurser.utgifter;

import no.nav.metrics.aspects.Timed;
import no.nav.sbl.dialogarena.sendsoknad.domain.oidc.OidcFeatureToggleUtils;
import no.nav.sbl.dialogarena.sikkerhet.Tilgangskontroll;
import no.nav.sbl.dialogarena.soknadinnsending.business.service.TextService;
import no.nav.sbl.soknadsosialhjelp.soknad.JsonInternalSoknad;
import no.nav.sbl.soknadsosialhjelp.soknad.familie.JsonHarForsorgerplikt;
import no.nav.sbl.soknadsosialhjelp.soknad.okonomi.JsonOkonomi;
import no.nav.sbl.soknadsosialhjelp.soknad.okonomi.JsonOkonomiopplysninger;
import no.nav.sbl.soknadsosialhjelp.soknad.okonomi.opplysning.JsonOkonomiOpplysningUtgift;
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

import static javax.ws.rs.core.MediaType.APPLICATION_JSON;
import static no.nav.sbl.dialogarena.soknadinnsending.business.mappers.FaktumNoklerOgBelopNavnMapper.soknadTypeToFaktumKey;
import static no.nav.sbl.dialogarena.soknadinnsending.business.mappers.OkonomiMapper.*;

@Controller
@ProtectedWithClaims(issuer = "selvbetjening", claimMap = { "acr=Level4" })
@Path("/soknader/{behandlingsId}/utgifter/barneutgifter")
@Timed
@Produces(APPLICATION_JSON)
public class BarneutgiftRessurs {

    @Inject
    private Tilgangskontroll tilgangskontroll;

    @Inject
    private SoknadUnderArbeidRepository soknadUnderArbeidRepository;

    @Inject
    private TextService textService;

    @GET
    public BarneutgifterFrontend hentBarneutgifter(@PathParam("behandlingsId") String behandlingsId){
        final String eier = OidcFeatureToggleUtils.getUserId();
        final JsonInternalSoknad soknad = soknadUnderArbeidRepository.hentSoknad(behandlingsId, eier).get().getJsonInternalSoknad();

        final JsonHarForsorgerplikt harForsorgerplikt = soknad.getSoknad().getData().getFamilie().getForsorgerplikt().getHarForsorgerplikt();
        if (harForsorgerplikt == null || harForsorgerplikt.getVerdi() == null || !harForsorgerplikt.getVerdi()){
            return new BarneutgifterFrontend().withHarForsorgerplikt(false);
        }

        final JsonOkonomi okonomi = soknad.getSoknad().getData().getOkonomi();
        final BarneutgifterFrontend barneutgifterFrontend = new BarneutgifterFrontend().withHarForsorgerplikt(true);

        if (okonomi.getOpplysninger().getBekreftelse() == null){
            return barneutgifterFrontend;
        }

        setBekreftelseOnBarneutgifterFrontend(okonomi.getOpplysninger(), barneutgifterFrontend);
        setUtgiftstyperOnBarneutgifterFrontend(okonomi, barneutgifterFrontend);

        return barneutgifterFrontend;
    }

    @PUT
    public void updateBarneutgifter(@PathParam("behandlingsId") String behandlingsId, BarneutgifterFrontend barneutgifterFrontend){
        tilgangskontroll.verifiserAtBrukerKanEndreSoknad(behandlingsId);
        final String eier = OidcFeatureToggleUtils.getUserId();
        final SoknadUnderArbeid soknad = soknadUnderArbeidRepository.hentSoknad(behandlingsId, eier).get();
        final JsonOkonomi okonomi = soknad.getJsonInternalSoknad().getSoknad().getData().getOkonomi();

        if (okonomi.getOpplysninger().getBekreftelse() == null){
            okonomi.getOpplysninger().setBekreftelse(new ArrayList<>());
        }

        setBekreftelse(okonomi.getOpplysninger(), "barneutgifter", barneutgifterFrontend.bekreftelse, textService.getJsonOkonomiTittel("utgifter.barn"));
        setBarneutgifter(okonomi, barneutgifterFrontend);

        soknadUnderArbeidRepository.oppdaterSoknadsdata(soknad, eier);
    }

    private void setBarneutgifter(JsonOkonomi okonomi, BarneutgifterFrontend barneutgifterFrontend) {
        List<JsonOkonomiOpplysningUtgift> opplysningerBarneutgifter = okonomi.getOpplysninger().getUtgift();
        List<JsonOkonomioversiktUtgift> oversiktBarneutgifter = okonomi.getOversikt().getUtgift();

        String type = "barnehage";
        String tittel = textService.getJsonOkonomiTittel(soknadTypeToFaktumKey.get(type));
        addutgiftIfCheckedElseDeleteInOversikt(oversiktBarneutgifter, type, tittel, barneutgifterFrontend.barnehage);

        type = "sfo";
        tittel = textService.getJsonOkonomiTittel(soknadTypeToFaktumKey.get(type));
        addutgiftIfCheckedElseDeleteInOversikt(oversiktBarneutgifter, type, tittel, barneutgifterFrontend.sfo);

        type = "barnFritidsaktiviteter";
        tittel = textService.getJsonOkonomiTittel(soknadTypeToFaktumKey.get(type));
        addutgiftIfCheckedElseDeleteInOpplysninger(opplysningerBarneutgifter, type, tittel, barneutgifterFrontend.fritidsaktiviteter);

        type = "barnTannregulering";
        tittel = textService.getJsonOkonomiTittel(soknadTypeToFaktumKey.get(type));
        addutgiftIfCheckedElseDeleteInOpplysninger(opplysningerBarneutgifter, type, tittel, barneutgifterFrontend.tannregulering);

        type = "annenBarneutgift";
        tittel = textService.getJsonOkonomiTittel(soknadTypeToFaktumKey.get(type));
        addutgiftIfCheckedElseDeleteInOpplysninger(opplysningerBarneutgifter, type, tittel, barneutgifterFrontend.annet);
    }

    private void setBekreftelseOnBarneutgifterFrontend(JsonOkonomiopplysninger opplysninger, BarneutgifterFrontend barneutgifterFrontend) {
        opplysninger.getBekreftelse().stream()
                .filter(bekreftelse -> bekreftelse.getType().equals("barneutgifter")).findFirst()
                .ifPresent(jsonOkonomibekreftelse -> barneutgifterFrontend.setBekreftelse(jsonOkonomibekreftelse.getVerdi()));
    }

    private void setUtgiftstyperOnBarneutgifterFrontend(JsonOkonomi okonomi, BarneutgifterFrontend barneutgifterFrontend) {
        okonomi.getOpplysninger().getUtgift().forEach(utgift -> {
            switch(utgift.getType()){
                case "barnFritidsaktiviteter":
                    barneutgifterFrontend.setFritidsaktiviteter(true);
                    break;
                case "barnTannregulering":
                    barneutgifterFrontend.setTannregulering(true);
                    break;
                case "annenBarneutgift":
                    barneutgifterFrontend.setAnnet(true);
                    break;
            }
        });
        okonomi.getOversikt().getUtgift().forEach(
                utgift -> {
                    switch(utgift.getType()){
                        case "barnehage":
                            barneutgifterFrontend.setBarnehage(true);
                            break;
                        case "sfo":
                            barneutgifterFrontend.setSfo(true);
                            break;
                    }
                });
    }

    @XmlAccessorType(XmlAccessType.FIELD)
    public static final class BarneutgifterFrontend {
        public boolean harForsorgerplikt;
        public Boolean bekreftelse;
        public boolean fritidsaktiviteter;
        public boolean barnehage;
        public boolean sfo;
        public boolean tannregulering;
        public boolean annet;

        public BarneutgifterFrontend withHarForsorgerplikt(boolean harForsorgerplikt) {
            this.harForsorgerplikt = harForsorgerplikt;
            return this;
        }

        public void setBekreftelse(Boolean bekreftelse) {
            this.bekreftelse = bekreftelse;
        }

        public void setFritidsaktiviteter(boolean fritidsaktiviteter) {
            this.fritidsaktiviteter = fritidsaktiviteter;
        }

        public void setBarnehage(boolean barnehage) {
            this.barnehage = barnehage;
        }

        public void setSfo(boolean sfo) {
            this.sfo = sfo;
        }

        public void setTannregulering(boolean tannregulering) {
            this.tannregulering = tannregulering;
        }

        public void setAnnet(boolean annet) {
            this.annet = annet;
        }
    }
}
