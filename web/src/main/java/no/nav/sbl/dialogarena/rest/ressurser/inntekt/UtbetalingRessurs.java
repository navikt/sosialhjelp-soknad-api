package no.nav.sbl.dialogarena.rest.ressurser.inntekt;

import no.nav.metrics.aspects.Timed;
import no.nav.sbl.dialogarena.sendsoknad.domain.oidc.OidcFeatureToggleUtils;
import no.nav.sbl.dialogarena.sikkerhet.Tilgangskontroll;
import no.nav.sbl.dialogarena.soknadinnsending.business.service.TextService;
import no.nav.sbl.soknadsosialhjelp.soknad.JsonInternalSoknad;
import no.nav.sbl.soknadsosialhjelp.soknad.common.JsonKildeBruker;
import no.nav.sbl.soknadsosialhjelp.soknad.okonomi.JsonOkonomiopplysninger;
import no.nav.sbl.soknadsosialhjelp.soknad.okonomi.opplysning.JsonOkonomiOpplysningUtbetaling;
import no.nav.sbl.soknadsosialhjelp.soknad.okonomi.opplysning.JsonOkonomibeskrivelserAvAnnet;
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
import static no.nav.sbl.dialogarena.soknadinnsending.business.mappers.TitleKeyMapper.soknadTypeToTitleKey;
import static no.nav.sbl.dialogarena.soknadinnsending.business.mappers.OkonomiMapper.addUtbetalingIfCheckedElseDeleteInOpplysninger;
import static no.nav.sbl.dialogarena.soknadinnsending.business.mappers.OkonomiMapper.setBekreftelse;
import static no.nav.sbl.soknadsosialhjelp.json.SoknadJsonTyper.*;

@Controller
@ProtectedWithClaims(issuer = "selvbetjening", claimMap = { "acr=Level4" })
@Path("/soknader/{behandlingsId}/inntekt/utbetalinger")
@Timed
@Produces(APPLICATION_JSON)
public class UtbetalingRessurs {

    @Inject
    private Tilgangskontroll tilgangskontroll;

    @Inject
    private SoknadUnderArbeidRepository soknadUnderArbeidRepository;

    @Inject
    private TextService textService;

    @GET
    public UtbetalingerFrontend hentUtbetalinger(@PathParam("behandlingsId") String behandlingsId){
        String eier = OidcFeatureToggleUtils.getUserId();
        JsonInternalSoknad soknad = soknadUnderArbeidRepository.hentSoknad(behandlingsId, eier).getJsonInternalSoknad();
        JsonOkonomiopplysninger opplysninger = soknad.getSoknad().getData().getOkonomi().getOpplysninger();
        UtbetalingerFrontend utbetalingerFrontend = new UtbetalingerFrontend();

        if (opplysninger.getBekreftelse() == null){
            return utbetalingerFrontend;
        }

        setBekreftelseOnUtbetalingerFrontend(opplysninger, utbetalingerFrontend);
        setUtbetalingstyperOnUtbetalingerFrontend(opplysninger, utbetalingerFrontend);

        if (opplysninger.getBeskrivelseAvAnnet() != null){
            utbetalingerFrontend.setBeskrivelseAvAnnet(opplysninger.getBeskrivelseAvAnnet().getUtbetaling());
        }

        return utbetalingerFrontend;
    }

    @PUT
    public void updateUtbetalinger(@PathParam("behandlingsId") String behandlingsId, UtbetalingerFrontend utbetalingerFrontend){
        tilgangskontroll.verifiserAtBrukerKanEndreSoknad(behandlingsId);
        String eier = OidcFeatureToggleUtils.getUserId();
        SoknadUnderArbeid soknad = soknadUnderArbeidRepository.hentSoknad(behandlingsId, eier);
        JsonOkonomiopplysninger opplysninger = soknad.getJsonInternalSoknad().getSoknad().getData().getOkonomi().getOpplysninger();

        if (opplysninger.getBekreftelse() == null){
            opplysninger.setBekreftelse(new ArrayList<>());
        }

        setBekreftelse(opplysninger, BEKREFTELSE_UTBETALING, utbetalingerFrontend.bekreftelse, textService.getJsonOkonomiTittel("inntekt.inntekter"));
        setUtbetalinger(opplysninger, utbetalingerFrontend);
        setBeskrivelseAvAnnet(opplysninger, utbetalingerFrontend);

        soknadUnderArbeidRepository.oppdaterSoknadsdata(soknad, eier);
    }

    private void setUtbetalinger(JsonOkonomiopplysninger opplysninger, UtbetalingerFrontend utbetalingerFrontend) {
        List<JsonOkonomiOpplysningUtbetaling> utbetalinger = opplysninger.getUtbetaling();

        String tittel = textService.getJsonOkonomiTittel(soknadTypeToTitleKey.get(UTBETALING_UTBYTTE));
        addUtbetalingIfCheckedElseDeleteInOpplysninger(utbetalinger, UTBETALING_UTBYTTE, tittel, utbetalingerFrontend.utbytte);

        tittel = textService.getJsonOkonomiTittel(soknadTypeToTitleKey.get(UTBETALING_SALG));
        addUtbetalingIfCheckedElseDeleteInOpplysninger(utbetalinger, UTBETALING_SALG, tittel, utbetalingerFrontend.salg);

        tittel = textService.getJsonOkonomiTittel(soknadTypeToTitleKey.get(UTBETALING_FORSIKRING));
        addUtbetalingIfCheckedElseDeleteInOpplysninger(utbetalinger, UTBETALING_FORSIKRING, tittel, utbetalingerFrontend.forsikring);

        tittel = textService.getJsonOkonomiTittel("opplysninger.inntekt.inntekter.annet");
        addUtbetalingIfCheckedElseDeleteInOpplysninger(utbetalinger, UTBETALING_ANNET, tittel, utbetalingerFrontend.annet);
    }

    private void setBeskrivelseAvAnnet(JsonOkonomiopplysninger opplysninger, UtbetalingerFrontend utbetalingerFrontend) {
        if (opplysninger.getBeskrivelseAvAnnet() == null){
            opplysninger.withBeskrivelseAvAnnet(new JsonOkonomibeskrivelserAvAnnet()
                    .withKilde(JsonKildeBruker.BRUKER)
                    .withVerdi("")
                    .withSparing("")
                    .withUtbetaling("")
                    .withBoutgifter("")
                    .withBarneutgifter(""));
        }
        opplysninger.getBeskrivelseAvAnnet().setUtbetaling(utbetalingerFrontend.beskrivelseAvAnnet != null ? utbetalingerFrontend.beskrivelseAvAnnet : "");
    }

    private void setBekreftelseOnUtbetalingerFrontend(JsonOkonomiopplysninger opplysninger, UtbetalingerFrontend utbetalingerFrontend) {
        opplysninger.getBekreftelse().stream()
                .filter(bekreftelse -> bekreftelse.getType().equals(BEKREFTELSE_UTBETALING)).findFirst()
                .ifPresent(jsonOkonomibekreftelse -> utbetalingerFrontend.setBekreftelse(jsonOkonomibekreftelse.getVerdi()));
    }

    private void setUtbetalingstyperOnUtbetalingerFrontend(JsonOkonomiopplysninger opplysninger, UtbetalingerFrontend utbetalingerFrontend) {
        opplysninger.getUtbetaling().forEach(
                utbetaling -> {
                    switch(utbetaling.getType()){
                        case UTBETALING_UTBYTTE:
                            utbetalingerFrontend.setUtbytte(true);
                            break;
                        case UTBETALING_SALG:
                            utbetalingerFrontend.setSalg(true);
                            break;
                        case UTBETALING_FORSIKRING:
                            utbetalingerFrontend.setForsikring(true);
                            break;
                        case UTBETALING_ANNET:
                            utbetalingerFrontend.setAnnet(true);
                            break;
                    }
                });
    }

    @XmlAccessorType(XmlAccessType.FIELD)
    public static final class UtbetalingerFrontend {
        public Boolean bekreftelse;
        public boolean utbytte;
        public boolean salg;
        public boolean forsikring;
        public boolean annet;
        public String beskrivelseAvAnnet;

        public void setBekreftelse(Boolean bekreftelse) {
            this.bekreftelse = bekreftelse;
        }

        public void setUtbytte(boolean utbytte) {
            this.utbytte = utbytte;
        }

        public void setSalg(boolean salg) {
            this.salg = salg;
        }

        public void setForsikring(boolean forsikring) {
            this.forsikring = forsikring;
        }

        public void setAnnet(boolean annet) {
            this.annet = annet;
        }

        public void setBeskrivelseAvAnnet(String beskrivelseAvAnnet) {
            this.beskrivelseAvAnnet = beskrivelseAvAnnet;
        }
    }
}
