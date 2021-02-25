package no.nav.sosialhjelp.soknad.web.rest.ressurser.utgifter;

import no.nav.metrics.aspects.Timed;
import no.nav.sbl.soknadsosialhjelp.soknad.JsonSoknad;
import no.nav.sbl.soknadsosialhjelp.soknad.okonomi.JsonOkonomi;
import no.nav.sbl.soknadsosialhjelp.soknad.okonomi.JsonOkonomiopplysninger;
import no.nav.sbl.soknadsosialhjelp.soknad.okonomi.opplysning.JsonOkonomiOpplysningUtgift;
import no.nav.sbl.soknadsosialhjelp.soknad.okonomi.opplysning.JsonOkonomibekreftelse;
import no.nav.sbl.soknadsosialhjelp.soknad.okonomi.oversikt.JsonOkonomioversiktUtgift;
import no.nav.security.token.support.core.api.ProtectedWithClaims;
import no.nav.sosialhjelp.soknad.business.service.TextService;
import no.nav.sosialhjelp.soknad.business.soknadunderbehandling.SoknadUnderArbeidRepository;
import no.nav.sosialhjelp.soknad.domain.SoknadUnderArbeid;
import no.nav.sosialhjelp.soknad.domain.model.oidc.SubjectHandler;
import no.nav.sosialhjelp.soknad.web.sikkerhet.Tilgangskontroll;
import org.springframework.stereotype.Controller;

import javax.ws.rs.GET;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static javax.ws.rs.core.MediaType.APPLICATION_JSON;
import static no.nav.sbl.soknadsosialhjelp.json.SoknadJsonTyper.BEKREFTELSE_BOUTGIFTER;
import static no.nav.sbl.soknadsosialhjelp.json.SoknadJsonTyper.BOSTOTTE;
import static no.nav.sbl.soknadsosialhjelp.json.SoknadJsonTyper.BOSTOTTE_SAMTYKKE;
import static no.nav.sbl.soknadsosialhjelp.json.SoknadJsonTyper.UTBETALING_HUSBANKEN;
import static no.nav.sbl.soknadsosialhjelp.json.SoknadJsonTyper.UTGIFTER_ANNET_BO;
import static no.nav.sbl.soknadsosialhjelp.json.SoknadJsonTyper.UTGIFTER_BOLIGLAN_AVDRAG;
import static no.nav.sbl.soknadsosialhjelp.json.SoknadJsonTyper.UTGIFTER_BOLIGLAN_RENTER;
import static no.nav.sbl.soknadsosialhjelp.json.SoknadJsonTyper.UTGIFTER_HUSLEIE;
import static no.nav.sbl.soknadsosialhjelp.json.SoknadJsonTyper.UTGIFTER_KOMMUNAL_AVGIFT;
import static no.nav.sbl.soknadsosialhjelp.json.SoknadJsonTyper.UTGIFTER_OPPVARMING;
import static no.nav.sbl.soknadsosialhjelp.json.SoknadJsonTyper.UTGIFTER_STROM;
import static no.nav.sosialhjelp.soknad.business.mappers.OkonomiMapper.addutgiftIfCheckedElseDeleteInOpplysninger;
import static no.nav.sosialhjelp.soknad.business.mappers.OkonomiMapper.addutgiftIfCheckedElseDeleteInOversikt;
import static no.nav.sosialhjelp.soknad.business.mappers.OkonomiMapper.setBekreftelse;
import static no.nav.sosialhjelp.soknad.business.mappers.TitleKeyMapper.soknadTypeToTitleKey;

@Controller
@ProtectedWithClaims(issuer = "selvbetjening", claimMap = {"acr=Level4"})
@Path("/soknader/{behandlingsId}/utgifter/boutgifter")
@Timed
@Produces(APPLICATION_JSON)
public class BoutgiftRessurs {

    private final Tilgangskontroll tilgangskontroll;
    private final SoknadUnderArbeidRepository soknadUnderArbeidRepository;
    private final TextService textService;

    public BoutgiftRessurs(Tilgangskontroll tilgangskontroll, SoknadUnderArbeidRepository soknadUnderArbeidRepository, TextService textService) {
        this.tilgangskontroll = tilgangskontroll;
        this.soknadUnderArbeidRepository = soknadUnderArbeidRepository;
        this.textService = textService;
    }

    @GET
    public BoutgifterFrontend hentBoutgifter(@PathParam("behandlingsId") String behandlingsId) {
        tilgangskontroll.verifiserAtBrukerHarTilgang();
        String eier = SubjectHandler.getUserId();
        JsonSoknad soknad = soknadUnderArbeidRepository.hentSoknad(behandlingsId, eier).getJsonInternalSoknad().getSoknad();
        JsonOkonomi okonomi = soknad.getData().getOkonomi();
        BoutgifterFrontend boutgifterFrontend = new BoutgifterFrontend();

        setSkalViseInfoVedBekreftelseOnBoutgifterFrontend(soknad, okonomi, boutgifterFrontend);

        if (okonomi.getOpplysninger().getBekreftelse() == null) {
            return boutgifterFrontend;
        }

        setBekreftelseOnBoutgifterFrontend(okonomi.getOpplysninger(), boutgifterFrontend);
        setUtgiftstyperOnBoutgifterFrontend(okonomi, boutgifterFrontend);

        return boutgifterFrontend;
    }

    @PUT
    public void updateBoutgifter(@PathParam("behandlingsId") String behandlingsId, BoutgifterFrontend boutgifterFrontend) {
        tilgangskontroll.verifiserAtBrukerKanEndreSoknad(behandlingsId);
        String eier = SubjectHandler.getUserId();
        SoknadUnderArbeid soknad = soknadUnderArbeidRepository.hentSoknad(behandlingsId, eier);
        JsonOkonomi okonomi = soknad.getJsonInternalSoknad().getSoknad().getData().getOkonomi();

        if (okonomi.getOpplysninger().getBekreftelse() == null) {
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
            switch (utgift.getType()) {
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
                    switch (utgift.getType()) {
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
        if (bostotteFeiletEllerManglerSamtykke(soknad)) {
            if (okonomi.getOpplysninger().getBekreftelse() != null) {
                Optional<JsonOkonomibekreftelse> bekreftelseOptional = okonomi.getOpplysninger().getBekreftelse().stream()
                        .filter(bekreftelse -> bekreftelse.getType().equals(BOSTOTTE))
                        .findFirst();
                if (bekreftelseOptional.isPresent()) {
                    JsonOkonomibekreftelse jsonOkonomibekreftelse = bekreftelseOptional.get();
                    boutgifterFrontend.setSkalViseInfoVedBekreftelse(
                            jsonOkonomibekreftelse.getVerdi() != null && !jsonOkonomibekreftelse.getVerdi());
                } else {
                    boutgifterFrontend.setSkalViseInfoVedBekreftelse(true);
                }
            }
        } else {
            if (!isAnyHusbankenSaker(soknad) && !isAnyHusbankenUtbetalinger(soknad)) {
                boutgifterFrontend.setSkalViseInfoVedBekreftelse(true);
            }
        }
    }

    private boolean bostotteFeiletEllerManglerSamtykke(JsonSoknad soknad) {
        return soknad.getDriftsinformasjon().getStotteFraHusbankenFeilet() ||
                soknad.getData().getOkonomi().getOpplysninger().getBekreftelse().stream()
                        .noneMatch(bekreftelse ->
                                bekreftelse.getType().equalsIgnoreCase(BOSTOTTE_SAMTYKKE) &&
                                        bekreftelse.getVerdi());
    }

    private boolean isAnyHusbankenUtbetalinger(JsonSoknad soknad) {
        return soknad.getData().getOkonomi().getOpplysninger().getUtbetaling().stream()
                .anyMatch(utbetaling -> utbetaling.getType().equals(UTBETALING_HUSBANKEN));
    }

    private boolean isAnyHusbankenSaker(JsonSoknad soknad) {
        return soknad.getData().getOkonomi().getOpplysninger().getBostotte().getSaker().stream()
                .anyMatch(sak -> sak.getType().equals(UTBETALING_HUSBANKEN));
    }

    @SuppressWarnings("WeakerAccess")
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
