package no.nav.sbl.dialogarena.rest.ressurser.inntekt;

import no.nav.metrics.aspects.Timed;

import no.nav.sbl.dialogarena.sendsoknad.domain.oidc.SubjectHandler;
import no.nav.sbl.dialogarena.sikkerhet.Tilgangskontroll;
import no.nav.sbl.dialogarena.soknadinnsending.business.service.TextService;
import no.nav.sbl.dialogarena.soknadinnsending.business.service.systemdata.BostotteSystemdata;
import no.nav.sbl.soknadsosialhjelp.soknad.JsonInternalSoknad;
import no.nav.sbl.soknadsosialhjelp.soknad.bostotte.JsonBostotteSak;
import no.nav.sbl.soknadsosialhjelp.soknad.okonomi.JsonOkonomiopplysninger;
import no.nav.sbl.soknadsosialhjelp.soknad.okonomi.opplysning.JsonOkonomiOpplysningUtbetaling;
import no.nav.sbl.soknadsosialhjelp.soknad.okonomi.opplysning.JsonOkonomibekreftelse;
import no.nav.sbl.sosialhjelp.domain.SoknadUnderArbeid;
import no.nav.sbl.sosialhjelp.soknadunderbehandling.SoknadUnderArbeidRepository;
import no.nav.security.token.support.core.api.ProtectedWithClaims;
import org.springframework.stereotype.Controller;

import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import static javax.ws.rs.core.MediaType.APPLICATION_JSON;
import static no.nav.sbl.dialogarena.soknadinnsending.business.mappers.OkonomiMapper.addUtbetalingIfNotPresentInOpplysninger;
import static no.nav.sbl.dialogarena.soknadinnsending.business.mappers.OkonomiMapper.removeBekreftelserIfPresent;
import static no.nav.sbl.dialogarena.soknadinnsending.business.mappers.OkonomiMapper.removeUtbetalingIfPresentInOpplysninger;
import static no.nav.sbl.dialogarena.soknadinnsending.business.mappers.OkonomiMapper.setBekreftelse;
import static no.nav.sbl.dialogarena.soknadinnsending.business.mappers.TitleKeyMapper.soknadTypeToTitleKey;
import static no.nav.sbl.soknadsosialhjelp.json.SoknadJsonTyper.BOSTOTTE;
import static no.nav.sbl.soknadsosialhjelp.json.SoknadJsonTyper.BOSTOTTE_SAMTYKKE;
import static no.nav.sbl.soknadsosialhjelp.json.SoknadJsonTyper.UTBETALING_HUSBANKEN;
import static org.springframework.http.HttpHeaders.AUTHORIZATION;

@Controller
@ProtectedWithClaims(issuer = "selvbetjening", claimMap = {"acr=Level4"})
@Path("/soknader/{behandlingsId}/inntekt/bostotte")
@Timed
@Produces(APPLICATION_JSON)
public class BostotteRessurs {

    @Inject
    private Tilgangskontroll tilgangskontroll;

    @Inject
    private SoknadUnderArbeidRepository soknadUnderArbeidRepository;

    @Inject
    private BostotteSystemdata bostotteSystemdata;

    @Inject
    private TextService textService;

    @Inject
    private SubjectHandler subjectHandler;

    @GET
    public BostotteFrontend hentBostotte(@PathParam("behandlingsId") String behandlingsId) {
        String eier = subjectHandler.getUserId();
        JsonInternalSoknad soknad = soknadUnderArbeidRepository.hentSoknad(behandlingsId, eier).getJsonInternalSoknad();
        JsonOkonomiopplysninger opplysninger = soknad.getSoknad().getData().getOkonomi().getOpplysninger();
        BostotteFrontend bostotteFrontend = new BostotteFrontend();

        if (opplysninger.getBekreftelse() != null) {
            setBekreftelseOnBostotteFrontend(opplysninger, bostotteFrontend);
            setSamtykkeOnBostotteFrontend(opplysninger, bostotteFrontend);
        }

        bostotteFrontend.setUtbetalinger(mapToUtbetalinger(soknad));
        bostotteFrontend.setSaksStatuser(mapToUtSaksStatuser(soknad));
        bostotteFrontend.setStotteFraHusbankenFeilet(soknad.getSoknad().getDriftsinformasjon().getStotteFraHusbankenFeilet());
        return bostotteFrontend;
    }

    @PUT
    public void updateBostotte(@PathParam("behandlingsId") String behandlingsId, BostotteFrontend bostotteFrontend,
                               @HeaderParam(value = AUTHORIZATION) String token) {
        tilgangskontroll.verifiserAtBrukerKanEndreSoknad(behandlingsId);
        String eier = subjectHandler.getUserId();
        SoknadUnderArbeid soknad = soknadUnderArbeidRepository.hentSoknad(behandlingsId, eier);
        JsonOkonomiopplysninger opplysninger = soknad.getJsonInternalSoknad().getSoknad().getData().getOkonomi().getOpplysninger();

        if (opplysninger.getBekreftelse() == null) {
            opplysninger.setBekreftelse(new ArrayList<>());
        }

        setBekreftelse(opplysninger, BOSTOTTE, bostotteFrontend.bekreftelse, textService.getJsonOkonomiTittel("inntekt.bostotte"));

        if (bostotteFrontend.bekreftelse != null) {
            if(bostotteFrontend.bekreftelse) {
                String tittel = textService.getJsonOkonomiTittel(soknadTypeToTitleKey.get(BOSTOTTE));
                addUtbetalingIfNotPresentInOpplysninger(opplysninger.getUtbetaling(), UTBETALING_HUSBANKEN, tittel);
            } else {
                removeUtbetalingIfPresentInOpplysninger(opplysninger.getUtbetaling(), UTBETALING_HUSBANKEN);
            }
        }

        soknadUnderArbeidRepository.oppdaterSoknadsdata(soknad, eier);
    }

    @POST
    @Path(value = "/samtykke")
    public void updateSamtykke(@PathParam("behandlingsId") String behandlingsId, boolean samtykke,
                               @HeaderParam(value = AUTHORIZATION) String token) {
        tilgangskontroll.verifiserAtBrukerKanEndreSoknad(behandlingsId);
        String eier = subjectHandler.getUserId();
        SoknadUnderArbeid soknad = soknadUnderArbeidRepository.hentSoknad(behandlingsId, eier);
        JsonOkonomiopplysninger opplysninger = soknad.getJsonInternalSoknad().getSoknad().getData().getOkonomi().getOpplysninger();

        boolean lagretSamtykke = hentSamtykkeFraSoknad(opplysninger);
        boolean skalLagre = samtykke;

        if(lagretSamtykke != samtykke) {
            skalLagre = true;
            removeBekreftelserIfPresent(opplysninger, BOSTOTTE_SAMTYKKE);
            setBekreftelse(opplysninger, BOSTOTTE_SAMTYKKE, samtykke, textService.getJsonOkonomiTittel("inntekt.bostotte.samtykke"));
        }

        if(skalLagre) {
            bostotteSystemdata.updateSystemdataIn(soknad, token);
            soknadUnderArbeidRepository.oppdaterSoknadsdata(soknad, eier);
        }
    }

    private boolean hentSamtykkeFraSoknad(JsonOkonomiopplysninger opplysninger) {
        return opplysninger.getBekreftelse().stream()
                .filter(bekreftelse -> bekreftelse.getType().equals(BOSTOTTE_SAMTYKKE))
                .anyMatch(JsonOkonomibekreftelse::getVerdi);
    }

    private String hentSamtykkeDatoFraSoknad(JsonOkonomiopplysninger opplysninger) {
        return opplysninger.getBekreftelse().stream()
                .filter(bekreftelse -> bekreftelse.getType().equals(BOSTOTTE_SAMTYKKE))
                .filter(JsonOkonomibekreftelse::getVerdi)
                .findAny()
                .map(JsonOkonomibekreftelse::getBekreftelsesDato).orElse(null);
    }

    private void setBekreftelseOnBostotteFrontend(JsonOkonomiopplysninger opplysninger, BostotteFrontend bostotteFrontend) {
        opplysninger.getBekreftelse().stream()
                .filter(bekreftelse -> bekreftelse.getType().equals(BOSTOTTE))
                .findFirst()
                .ifPresent(jsonOkonomibekreftelse -> bostotteFrontend.setBekreftelse(jsonOkonomibekreftelse.getVerdi()));
    }

    private void setSamtykkeOnBostotteFrontend(JsonOkonomiopplysninger opplysninger, BostotteFrontend bostotteFrontend) {
        bostotteFrontend.setSamtykke(hentSamtykkeFraSoknad(opplysninger), hentSamtykkeDatoFraSoknad(opplysninger));
    }

    private List<JsonOkonomiOpplysningUtbetaling> mapToUtbetalinger(JsonInternalSoknad soknad) {
        return soknad.getSoknad().getData().getOkonomi().getOpplysninger().getUtbetaling().stream()
                .filter(utbetaling -> utbetaling.getType().equals(UTBETALING_HUSBANKEN))
                .collect(Collectors.toList());
    }

    private List<JsonBostotteSak> mapToUtSaksStatuser(JsonInternalSoknad soknad) {
        return soknad.getSoknad().getData().getOkonomi().getOpplysninger().getBostotte().getSaker().stream()
                .filter(sak -> sak.getType().equals(UTBETALING_HUSBANKEN))
                .collect(Collectors.toList());
    }

    @SuppressWarnings("WeakerAccess")
    @XmlAccessorType(XmlAccessType.FIELD)
    public static final class BostotteFrontend {
        public Boolean bekreftelse;
        public Boolean samtykke;
        public List<JsonOkonomiOpplysningUtbetaling> utbetalinger;
        public List<JsonBostotteSak> saker;
        public Boolean stotteFraHusbankenFeilet;
        public String samtykkeTidspunkt;

        public void setBekreftelse(Boolean bekreftelse) {
            this.bekreftelse = bekreftelse;
        }

        public void setSamtykke(Boolean samtykke, String samtykkeTidspunkt) {
            this.samtykke = samtykke;
            this.samtykkeTidspunkt = samtykkeTidspunkt;
        }

        public void setUtbetalinger(List<JsonOkonomiOpplysningUtbetaling> utbetalinger) {
            this.utbetalinger = utbetalinger;
        }

        public void setSaksStatuser(List<JsonBostotteSak> saker) {
            this.saker = saker;
        }

        public void setStotteFraHusbankenFeilet(Boolean stotteFraHusbankenFeilet) {
            this.stotteFraHusbankenFeilet = stotteFraHusbankenFeilet;
        }
    }
}
