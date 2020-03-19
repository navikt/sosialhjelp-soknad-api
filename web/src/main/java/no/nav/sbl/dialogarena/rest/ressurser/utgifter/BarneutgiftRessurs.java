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
import no.nav.security.token.support.core.api.ProtectedWithClaims;
import org.springframework.stereotype.Controller;

import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import java.util.ArrayList;
import java.util.List;

import static javax.ws.rs.core.MediaType.APPLICATION_JSON;
import static no.nav.sbl.dialogarena.soknadinnsending.business.mappers.OkonomiMapper.addutgiftIfCheckedElseDeleteInOpplysninger;
import static no.nav.sbl.dialogarena.soknadinnsending.business.mappers.OkonomiMapper.addutgiftIfCheckedElseDeleteInOversikt;
import static no.nav.sbl.dialogarena.soknadinnsending.business.mappers.OkonomiMapper.setBekreftelse;
import static no.nav.sbl.dialogarena.soknadinnsending.business.mappers.TitleKeyMapper.soknadTypeToTitleKey;
import static no.nav.sbl.soknadsosialhjelp.json.SoknadJsonTyper.BEKREFTELSE_BARNEUTGIFTER;
import static no.nav.sbl.soknadsosialhjelp.json.SoknadJsonTyper.UTGIFTER_ANNET_BARN;
import static no.nav.sbl.soknadsosialhjelp.json.SoknadJsonTyper.UTGIFTER_BARNEHAGE;
import static no.nav.sbl.soknadsosialhjelp.json.SoknadJsonTyper.UTGIFTER_BARN_FRITIDSAKTIVITETER;
import static no.nav.sbl.soknadsosialhjelp.json.SoknadJsonTyper.UTGIFTER_BARN_TANNREGULERING;
import static no.nav.sbl.soknadsosialhjelp.json.SoknadJsonTyper.UTGIFTER_SFO;

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
        String eier = OidcFeatureToggleUtils.getUserId();
        JsonInternalSoknad soknad = soknadUnderArbeidRepository.hentSoknad(behandlingsId, eier).getJsonInternalSoknad();

        JsonHarForsorgerplikt harForsorgerplikt = soknad.getSoknad().getData().getFamilie().getForsorgerplikt().getHarForsorgerplikt();
        if (harForsorgerplikt == null || harForsorgerplikt.getVerdi() == null || !harForsorgerplikt.getVerdi()){
            return new BarneutgifterFrontend().withHarForsorgerplikt(false);
        }

        JsonOkonomi okonomi = soknad.getSoknad().getData().getOkonomi();
        BarneutgifterFrontend barneutgifterFrontend = new BarneutgifterFrontend().withHarForsorgerplikt(true);

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
        String eier = OidcFeatureToggleUtils.getUserId();
        SoknadUnderArbeid soknad = soknadUnderArbeidRepository.hentSoknad(behandlingsId, eier);
        JsonOkonomi okonomi = soknad.getJsonInternalSoknad().getSoknad().getData().getOkonomi();

        if (okonomi.getOpplysninger().getBekreftelse() == null){
            okonomi.getOpplysninger().setBekreftelse(new ArrayList<>());
        }

        setBekreftelse(okonomi.getOpplysninger(), BEKREFTELSE_BARNEUTGIFTER, barneutgifterFrontend.bekreftelse, textService.getJsonOkonomiTittel("utgifter.barn"));
        setBarneutgifter(okonomi, barneutgifterFrontend);

        soknadUnderArbeidRepository.oppdaterSoknadsdata(soknad, eier);
    }

    private void setBarneutgifter(JsonOkonomi okonomi, BarneutgifterFrontend barneutgifterFrontend) {
        List<JsonOkonomiOpplysningUtgift> opplysningerBarneutgifter = okonomi.getOpplysninger().getUtgift();
        List<JsonOkonomioversiktUtgift> oversiktBarneutgifter = okonomi.getOversikt().getUtgift();

        String tittel = textService.getJsonOkonomiTittel(soknadTypeToTitleKey.get(UTGIFTER_BARNEHAGE));
        addutgiftIfCheckedElseDeleteInOversikt(oversiktBarneutgifter, UTGIFTER_BARNEHAGE, tittel, barneutgifterFrontend.barnehage);

        tittel = textService.getJsonOkonomiTittel(soknadTypeToTitleKey.get(UTGIFTER_SFO));
        addutgiftIfCheckedElseDeleteInOversikt(oversiktBarneutgifter, UTGIFTER_SFO, tittel, barneutgifterFrontend.sfo);

        tittel = textService.getJsonOkonomiTittel(soknadTypeToTitleKey.get(UTGIFTER_BARN_FRITIDSAKTIVITETER));
        addutgiftIfCheckedElseDeleteInOpplysninger(opplysningerBarneutgifter, UTGIFTER_BARN_FRITIDSAKTIVITETER, tittel, barneutgifterFrontend.fritidsaktiviteter);

        tittel = textService.getJsonOkonomiTittel(soknadTypeToTitleKey.get(UTGIFTER_BARN_TANNREGULERING));
        addutgiftIfCheckedElseDeleteInOpplysninger(opplysningerBarneutgifter, UTGIFTER_BARN_TANNREGULERING, tittel, barneutgifterFrontend.tannregulering);

        tittel = textService.getJsonOkonomiTittel(soknadTypeToTitleKey.get(UTGIFTER_ANNET_BARN));
        addutgiftIfCheckedElseDeleteInOpplysninger(opplysningerBarneutgifter, UTGIFTER_ANNET_BARN, tittel, barneutgifterFrontend.annet);
    }

    private void setBekreftelseOnBarneutgifterFrontend(JsonOkonomiopplysninger opplysninger, BarneutgifterFrontend barneutgifterFrontend) {
        opplysninger.getBekreftelse().stream()
                .filter(bekreftelse -> bekreftelse.getType().equals(BEKREFTELSE_BARNEUTGIFTER)).findFirst()
                .ifPresent(jsonOkonomibekreftelse -> barneutgifterFrontend.setBekreftelse(jsonOkonomibekreftelse.getVerdi()));
    }

    private void setUtgiftstyperOnBarneutgifterFrontend(JsonOkonomi okonomi, BarneutgifterFrontend barneutgifterFrontend) {
        okonomi.getOpplysninger().getUtgift().forEach(utgift -> {
            switch(utgift.getType()){
                case UTGIFTER_BARN_FRITIDSAKTIVITETER:
                    barneutgifterFrontend.setFritidsaktiviteter(true);
                    break;
                case UTGIFTER_BARN_TANNREGULERING:
                    barneutgifterFrontend.setTannregulering(true);
                    break;
                case UTGIFTER_ANNET_BARN:
                    barneutgifterFrontend.setAnnet(true);
                    break;
            }
        });
        okonomi.getOversikt().getUtgift().forEach(
                utgift -> {
                    switch(utgift.getType()){
                        case UTGIFTER_BARNEHAGE:
                            barneutgifterFrontend.setBarnehage(true);
                            break;
                        case UTGIFTER_SFO:
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
