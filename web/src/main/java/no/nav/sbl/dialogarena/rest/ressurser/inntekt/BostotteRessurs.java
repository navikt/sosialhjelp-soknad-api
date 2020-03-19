package no.nav.sbl.dialogarena.rest.ressurser.inntekt;

import no.nav.metrics.aspects.Timed;
import no.nav.sbl.dialogarena.sendsoknad.domain.oidc.OidcFeatureToggleUtils;
import no.nav.sbl.dialogarena.sikkerhet.Tilgangskontroll;
import no.nav.sbl.dialogarena.soknadinnsending.business.service.TextService;
import no.nav.sbl.soknadsosialhjelp.soknad.JsonInternalSoknad;
import no.nav.sbl.soknadsosialhjelp.soknad.bostotte.JsonBostotteSak;
import no.nav.sbl.soknadsosialhjelp.soknad.okonomi.JsonOkonomiopplysninger;
import no.nav.sbl.soknadsosialhjelp.soknad.okonomi.opplysning.JsonOkonomiOpplysningUtbetaling;
import no.nav.sbl.soknadsosialhjelp.soknad.okonomi.oversikt.JsonOkonomioversiktInntekt;
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
import java.util.stream.Collectors;

import static javax.ws.rs.core.MediaType.APPLICATION_JSON;
import static no.nav.sbl.dialogarena.soknadinnsending.business.mappers.OkonomiMapper.addInntektIfCheckedElseDeleteInOversikt;
import static no.nav.sbl.dialogarena.soknadinnsending.business.mappers.OkonomiMapper.setBekreftelse;
import static no.nav.sbl.dialogarena.soknadinnsending.business.mappers.TitleKeyMapper.soknadTypeToTitleKey;
import static no.nav.sbl.soknadsosialhjelp.json.SoknadJsonTyper.BOSTOTTE;
import static no.nav.sbl.soknadsosialhjelp.json.SoknadJsonTyper.UTBETALING_HUSBANKEN;

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
    private TextService textService;

    @GET
    public BostotteFrontend hentBostotte(@PathParam("behandlingsId") String behandlingsId) {
        String eier = OidcFeatureToggleUtils.getUserId();
        JsonInternalSoknad soknad = soknadUnderArbeidRepository.hentSoknad(behandlingsId, eier).getJsonInternalSoknad();
        JsonOkonomiopplysninger opplysninger = soknad.getSoknad().getData().getOkonomi().getOpplysninger();
        BostotteFrontend bostotteFrontend = new BostotteFrontend();

        if (opplysninger.getBekreftelse() != null) {
            setBekreftelseOnBostotteFrontend(opplysninger, bostotteFrontend);
        }

        if(soknad.getSoknad().getData().getOkonomi().getOpplysninger().getBostotte() == null) {
            // TODO: 2019-11-25 pcn: Denne er her midlertidig for å fange opp søknader som er started før bostøtte ble rullet ut.
            bostotteFrontend.setStotteFraHusbankenFeilet(true);
        } else {
            bostotteFrontend.setUtbetalinger(mapToUtbetalinger(soknad));
            bostotteFrontend.setSaksStatuser(mapToUtSaksStatuser(soknad));
            bostotteFrontend.setStotteFraHusbankenFeilet(soknad.getSoknad().getDriftsinformasjon().getStotteFraHusbankenFeilet());
        }
        return bostotteFrontend;
    }

    @PUT
    public void updateBostotte(@PathParam("behandlingsId") String behandlingsId, BostotteFrontend bostotteFrontend) {
        tilgangskontroll.verifiserAtBrukerKanEndreSoknad(behandlingsId);
        String eier = OidcFeatureToggleUtils.getUserId();
        SoknadUnderArbeid soknad = soknadUnderArbeidRepository.hentSoknad(behandlingsId, eier);
        JsonOkonomiopplysninger opplysninger = soknad.getJsonInternalSoknad().getSoknad().getData().getOkonomi().getOpplysninger();
        List<JsonOkonomioversiktInntekt> inntekter = soknad.getJsonInternalSoknad().getSoknad().getData().getOkonomi().getOversikt().getInntekt();

        if (opplysninger.getBekreftelse() == null) {
            opplysninger.setBekreftelse(new ArrayList<>());
        }

        setBekreftelse(opplysninger, BOSTOTTE, bostotteFrontend.bekreftelse, textService.getJsonOkonomiTittel("inntekt.bostotte"));

        if (bostotteFrontend.bekreftelse != null) {
            String tittel = textService.getJsonOkonomiTittel(soknadTypeToTitleKey.get(BOSTOTTE));
            addInntektIfCheckedElseDeleteInOversikt(inntekter, BOSTOTTE, tittel, bostotteFrontend.bekreftelse);
        }

        soknadUnderArbeidRepository.oppdaterSoknadsdata(soknad, eier);
    }

    private void setBekreftelseOnBostotteFrontend(JsonOkonomiopplysninger opplysninger, BostotteFrontend bostotteFrontend) {
        opplysninger.getBekreftelse().stream()
                .filter(bekreftelse -> bekreftelse.getType().equals(BOSTOTTE))
                .findFirst()
                .ifPresent(jsonOkonomibekreftelse -> bostotteFrontend.setBekreftelse(jsonOkonomibekreftelse.getVerdi()));
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
        public List<JsonOkonomiOpplysningUtbetaling> utbetalinger;
        public List<JsonBostotteSak> saker;
        public Boolean stotteFraHusbankenFeilet;

        public void setBekreftelse(Boolean bekreftelse) {
            this.bekreftelse = bekreftelse;
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
