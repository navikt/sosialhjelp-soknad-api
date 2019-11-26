package no.nav.sbl.dialogarena.rest.ressurser.utgifter;

import no.nav.metrics.aspects.Timed;
import no.nav.sbl.dialogarena.sendsoknad.domain.oidc.OidcFeatureToggleUtils;
import no.nav.sbl.dialogarena.sikkerhet.Tilgangskontroll;
import no.nav.sbl.dialogarena.soknadinnsending.business.service.TextService;
import no.nav.sbl.soknadsosialhjelp.soknad.JsonSoknad;
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
import static no.nav.sbl.dialogarena.soknadinnsending.business.mappers.OkonomiMapper.*;
import static no.nav.sbl.dialogarena.soknadinnsending.business.mappers.TitleKeyMapper.soknadTypeToTitleKey;
import static no.nav.sbl.soknadsosialhjelp.json.SoknadJsonTyper.*;

@Controller
@ProtectedWithClaims(issuer = "selvbetjening", claimMap = { "acr=Level4" })
@Path("/soknader/{behandlingsId}/utgifter/boutgifter")
@Timed
@Produces(APPLICATION_JSON)
public class BoutgiftRessurs {

    @Inject
    private Tilgangskontroll tilgangskontroll;

    @Inject
    private SoknadUnderArbeidRepository soknadUnderArbeidRepository;

    @Inject
    private TextService textService;

    @GET
    public BoutgifterFrontend hentBoutgifter(@PathParam("behandlingsId") String behandlingsId){
        String eier = OidcFeatureToggleUtils.getUserId();
        JsonSoknad soknad = soknadUnderArbeidRepository.hentSoknad(behandlingsId, eier).getJsonInternalSoknad().getSoknad();
        JsonOkonomi okonomi = soknad.getData().getOkonomi();
        BoutgifterFrontend boutgifterFrontend = new BoutgifterFrontend();

        setSkalViseInfoVedBekreftelseOnBoutgifterFrontend(soknad, okonomi, boutgifterFrontend);

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
        String eier = OidcFeatureToggleUtils.getUserId();
        SoknadUnderArbeid soknad = soknadUnderArbeidRepository.hentSoknad(behandlingsId, eier);
        JsonOkonomi okonomi = soknad.getJsonInternalSoknad().getSoknad().getData().getOkonomi();

        if (okonomi.getOpplysninger().getBekreftelse() == null){
            okonomi.getOpplysninger().setBekreftelse(new ArrayList<>());
        }

        setBekreftelse(okonomi.getOpplysninger(), BEKREFTELSE_BOUTGIFTER, boutgifterFrontend.bekreftelse, textService.getJsonOkonomiTittel("utgifter.boutgift"));
        setBoutgifter(okonomi, boutgifterFrontend);

        soknadUnderArbeidRepository.oppdaterSoknadsdata(soknad, eier);
    }

    private void setBoutgifter(JsonOkonomi okonomi, BoutgifterFrontend boutgifterFrontend) {
        List<JsonOkonomiOpplysningUtgift> opplysningerBoutgifter = okonomi.getOpplysninger().getUtgift();
        List<JsonOkonomioversiktUtgift> oversiktBoutgifter = okonomi.getOversikt().getUtgift();

        String tittel = textService.getJsonOkonomiTittel(soknadTypeToTitleKey.get(UTGIFTER_HUSLEIE));
        addutgiftIfCheckedElseDeleteInOversikt(oversiktBoutgifter, UTGIFTER_HUSLEIE, tittel, boutgifterFrontend.husleie);

        tittel = textService.getJsonOkonomiTittel(soknadTypeToTitleKey.get(UTGIFTER_STROM));
        addutgiftIfCheckedElseDeleteInOpplysninger(opplysningerBoutgifter, UTGIFTER_STROM, tittel, boutgifterFrontend.strom);

        tittel = textService.getJsonOkonomiTittel(soknadTypeToTitleKey.get(UTGIFTER_KOMMUNAL_AVGIFT));
        addutgiftIfCheckedElseDeleteInOpplysninger(opplysningerBoutgifter, UTGIFTER_KOMMUNAL_AVGIFT, tittel, boutgifterFrontend.kommunalAvgift);

        tittel = textService.getJsonOkonomiTittel(soknadTypeToTitleKey.get(UTGIFTER_OPPVARMING));
        addutgiftIfCheckedElseDeleteInOpplysninger(opplysningerBoutgifter, UTGIFTER_OPPVARMING, tittel, boutgifterFrontend.oppvarming);

        tittel = textService.getJsonOkonomiTittel(soknadTypeToTitleKey.get(UTGIFTER_BOLIGLAN_AVDRAG));
        addutgiftIfCheckedElseDeleteInOversikt(oversiktBoutgifter, UTGIFTER_BOLIGLAN_AVDRAG, tittel, boutgifterFrontend.boliglan);

        tittel = textService.getJsonOkonomiTittel(soknadTypeToTitleKey.get(UTGIFTER_BOLIGLAN_RENTER));
        addutgiftIfCheckedElseDeleteInOversikt(oversiktBoutgifter, UTGIFTER_BOLIGLAN_RENTER, tittel, boutgifterFrontend.boliglan);

        tittel = textService.getJsonOkonomiTittel(soknadTypeToTitleKey.get(UTGIFTER_ANNET_BO));
        addutgiftIfCheckedElseDeleteInOpplysninger(opplysningerBoutgifter, UTGIFTER_ANNET_BO, tittel, boutgifterFrontend.annet);
    }

    private void setBekreftelseOnBoutgifterFrontend(JsonOkonomiopplysninger opplysninger, BoutgifterFrontend boutgifterFrontend) {
        opplysninger.getBekreftelse().stream()
                .filter(bekreftelse -> bekreftelse.getType().equals(BEKREFTELSE_BOUTGIFTER)).findFirst()
                .ifPresent(jsonOkonomibekreftelse -> boutgifterFrontend.setBekreftelse(jsonOkonomibekreftelse.getVerdi()));
    }

    private void setUtgiftstyperOnBoutgifterFrontend(JsonOkonomi okonomi, BoutgifterFrontend boutgifterFrontend) {
        okonomi.getOpplysninger().getUtgift().forEach(utgift -> {
            switch(utgift.getType()){
                case UTGIFTER_STROM:
                    boutgifterFrontend.setStrom(true);
                    break;
                case UTGIFTER_KOMMUNAL_AVGIFT:
                    boutgifterFrontend.setKommunalAvgift(true);
                    break;
                case UTGIFTER_OPPVARMING:
                    boutgifterFrontend.setOppvarming(true);
                    break;
                case UTGIFTER_ANNET_BO:
                    boutgifterFrontend.setAnnet(true);
                    break;
            }
        });
        okonomi.getOversikt().getUtgift().forEach(
                utgift -> {
                    switch(utgift.getType()){
                        case UTGIFTER_HUSLEIE:
                            boutgifterFrontend.setHusleie(true);
                            break;
                        case UTGIFTER_BOLIGLAN_AVDRAG:
                            boutgifterFrontend.setBoliglan(true);
                            break;
                    }
                });
    }

    private void setSkalViseInfoVedBekreftelseOnBoutgifterFrontend(JsonSoknad soknad, JsonOkonomi okonomi, BoutgifterFrontend boutgifterFrontend) {
        if (soknad.getDriftsinformasjon().getStotteFraHusbankenFeilet()) {
            if (okonomi.getOpplysninger().getBekreftelse() != null) {
                okonomi.getOpplysninger().getBekreftelse().stream()
                        .filter(bekreftelse -> bekreftelse.getType().equals(BOSTOTTE))
                        .findFirst()
                        .ifPresent(jsonOkonomibekreftelse -> boutgifterFrontend.setSkalViseInfoVedBekreftelse(
                                jsonOkonomibekreftelse.getVerdi() != null ? jsonOkonomibekreftelse.getVerdi() : false));
            }
        } else {
            if (!isAnyHusbankenSaker(soknad) && !isAnyHusbankenUtbetalinger(soknad)) {
                boutgifterFrontend.setSkalViseInfoVedBekreftelse(true);
            }
        }
    }

    private boolean isAnyHusbankenUtbetalinger(JsonSoknad soknad) {
        return soknad.getData().getOkonomi().getOpplysninger().getUtbetaling().stream()
                .anyMatch(utbetaling -> utbetaling.getType().equals(UTBETALING_HUSBANKEN));
    }

    private boolean isAnyHusbankenSaker(JsonSoknad soknad) {
        return soknad.getData().getOkonomi().getOpplysninger().getBostotte().getSaker().stream()
                .anyMatch(sak -> sak.getType().equals(UTBETALING_HUSBANKEN));
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
        public boolean skalViseInfoVedBekreftelse;

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

        public void setSkalViseInfoVedBekreftelse(boolean skalViseInfoVedBekreftelse) {
            this.skalViseInfoVedBekreftelse = skalViseInfoVedBekreftelse;
        }
    }
}
