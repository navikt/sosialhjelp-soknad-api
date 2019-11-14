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
import static no.nav.sbl.dialogarena.soknadinnsending.business.mappers.OkonomiMapper.addUtbetalingIfCheckedElseDeleteInOpplysninger;
import static no.nav.sbl.dialogarena.soknadinnsending.business.mappers.OkonomiMapper.setBekreftelse;
import static no.nav.sbl.dialogarena.soknadinnsending.business.mappers.TittelNoklerOgBelopNavnMapper.soknadTypeToTittelKey;

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
        final String eier = OidcFeatureToggleUtils.getUserId();
        final JsonInternalSoknad soknad = soknadUnderArbeidRepository.hentSoknad(behandlingsId, eier).getJsonInternalSoknad();
        final JsonOkonomiopplysninger opplysninger = soknad.getSoknad().getData().getOkonomi().getOpplysninger();
        final UtbetalingerFrontend utbetalingerFrontend = new UtbetalingerFrontend();

        utbetalingerFrontend.setUtbetalingerFraNavFeilet(soknad.getSoknad().getDriftsinformasjon().getUtbetalingerFraNavFeilet());

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
        final String eier = OidcFeatureToggleUtils.getUserId();
        final SoknadUnderArbeid soknad = soknadUnderArbeidRepository.hentSoknad(behandlingsId, eier);
        final JsonOkonomiopplysninger opplysninger = soknad.getJsonInternalSoknad().getSoknad().getData().getOkonomi().getOpplysninger();

        if (opplysninger.getBekreftelse() == null){
            opplysninger.setBekreftelse(new ArrayList<>());
        }

        setBekreftelse(opplysninger, "utbetaling", utbetalingerFrontend.bekreftelse, textService.getJsonOkonomiTittel("inntekt.inntekter"));
        setUtbetalinger(opplysninger, utbetalingerFrontend);
        setBeskrivelseAvAnnet(opplysninger, utbetalingerFrontend);

        soknadUnderArbeidRepository.oppdaterSoknadsdata(soknad, eier);
    }

    private void setUtbetalinger(JsonOkonomiopplysninger opplysninger, UtbetalingerFrontend utbetalingerFrontend) {
        List<JsonOkonomiOpplysningUtbetaling> utbetalinger = opplysninger.getUtbetaling();

        String type = "utbytte";
        String tittel = textService.getJsonOkonomiTittel(soknadTypeToTittelKey.get(type));
        addUtbetalingIfCheckedElseDeleteInOpplysninger(utbetalinger, type, tittel, utbetalingerFrontend.utbytte);

        type = "salg";
        tittel = textService.getJsonOkonomiTittel(soknadTypeToTittelKey.get(type));
        addUtbetalingIfCheckedElseDeleteInOpplysninger(utbetalinger, type, tittel, utbetalingerFrontend.salg);

        type = "forsikring";
        tittel = textService.getJsonOkonomiTittel(soknadTypeToTittelKey.get(type));
        addUtbetalingIfCheckedElseDeleteInOpplysninger(utbetalinger, type, tittel, utbetalingerFrontend.forsikring);

        type = "annen";
        tittel = textService.getJsonOkonomiTittel("opplysninger.inntekt.inntekter.annet");
        addUtbetalingIfCheckedElseDeleteInOpplysninger(utbetalinger, type, tittel, utbetalingerFrontend.annet);
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
                .filter(bekreftelse -> bekreftelse.getType().equals("utbetaling")).findFirst()
                .ifPresent(jsonOkonomibekreftelse -> utbetalingerFrontend.setBekreftelse(jsonOkonomibekreftelse.getVerdi()));
    }

    private void setUtbetalingstyperOnUtbetalingerFrontend(JsonOkonomiopplysninger opplysninger, UtbetalingerFrontend utbetalingerFrontend) {
        opplysninger.getUtbetaling().forEach(
                utbetaling -> {
                    switch(utbetaling.getType()){
                        case "utbytte":
                            utbetalingerFrontend.setUtbytte(true);
                            break;
                        case "salg":
                            utbetalingerFrontend.setSalg(true);
                            break;
                        case "forsikring":
                            utbetalingerFrontend.setForsikring(true);
                            break;
                        case "annen":
                            utbetalingerFrontend.setAnnet(true);
                            break;
                    }
                });
    }

    @SuppressWarnings("WeakerAccess")
    @XmlAccessorType(XmlAccessType.FIELD)
    public static final class UtbetalingerFrontend {
        public Boolean bekreftelse;
        public boolean utbytte;
        public boolean salg;
        public boolean forsikring;
        public boolean annet;
        public String beskrivelseAvAnnet;
        public Boolean utbetalingerFraNavFeilet;

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

        public void setUtbetalingerFraNavFeilet(Boolean utbetalingerFraNavFeilet) {
            this.utbetalingerFraNavFeilet = utbetalingerFraNavFeilet;
        }
    }
}
