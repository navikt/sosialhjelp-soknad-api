package no.nav.sosialhjelp.soknad.web.rest.ressurser.inntekt;

import no.nav.sbl.soknadsosialhjelp.soknad.JsonInternalSoknad;
import no.nav.sbl.soknadsosialhjelp.soknad.common.JsonKildeBruker;
import no.nav.sbl.soknadsosialhjelp.soknad.okonomi.JsonOkonomiopplysninger;
import no.nav.sbl.soknadsosialhjelp.soknad.okonomi.opplysning.JsonOkonomiOpplysningUtbetaling;
import no.nav.sbl.soknadsosialhjelp.soknad.okonomi.opplysning.JsonOkonomibeskrivelserAvAnnet;
import no.nav.security.token.support.core.api.ProtectedWithClaims;
import no.nav.sosialhjelp.metrics.aspects.Timed;
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

import static javax.ws.rs.core.MediaType.APPLICATION_JSON;
import static no.nav.sbl.soknadsosialhjelp.json.SoknadJsonTyper.BEKREFTELSE_UTBETALING;
import static no.nav.sbl.soknadsosialhjelp.json.SoknadJsonTyper.UTBETALING_ANNET;
import static no.nav.sbl.soknadsosialhjelp.json.SoknadJsonTyper.UTBETALING_FORSIKRING;
import static no.nav.sbl.soknadsosialhjelp.json.SoknadJsonTyper.UTBETALING_SALG;
import static no.nav.sbl.soknadsosialhjelp.json.SoknadJsonTyper.UTBETALING_UTBYTTE;
import static no.nav.sosialhjelp.soknad.business.mappers.OkonomiMapper.addUtbetalingIfCheckedElseDeleteInOpplysninger;
import static no.nav.sosialhjelp.soknad.business.mappers.OkonomiMapper.setBekreftelse;
import static no.nav.sosialhjelp.soknad.business.mappers.TitleKeyMapper.soknadTypeToTitleKey;

@Controller
@ProtectedWithClaims(issuer = "selvbetjening", claimMap = { "acr=Level4" })
@Path("/soknader/{behandlingsId}/inntekt/utbetalinger")
@Timed
@Produces(APPLICATION_JSON)
public class UtbetalingRessurs {

    private final Tilgangskontroll tilgangskontroll;
    private final SoknadUnderArbeidRepository soknadUnderArbeidRepository;
    private final TextService textService;

    public UtbetalingRessurs(Tilgangskontroll tilgangskontroll, SoknadUnderArbeidRepository soknadUnderArbeidRepository, TextService textService) {
        this.tilgangskontroll = tilgangskontroll;
        this.soknadUnderArbeidRepository = soknadUnderArbeidRepository;
        this.textService = textService;
    }

    @GET
    public UtbetalingerFrontend hentUtbetalinger(@PathParam("behandlingsId") String behandlingsId){
        tilgangskontroll.verifiserAtBrukerHarTilgang();
        String eier = SubjectHandler.getUserId();
        JsonInternalSoknad soknad = soknadUnderArbeidRepository.hentSoknad(behandlingsId, eier).getJsonInternalSoknad();
        JsonOkonomiopplysninger opplysninger = soknad.getSoknad().getData().getOkonomi().getOpplysninger();
        UtbetalingerFrontend utbetalingerFrontend = new UtbetalingerFrontend();

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
        String eier = SubjectHandler.getUserId();
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
