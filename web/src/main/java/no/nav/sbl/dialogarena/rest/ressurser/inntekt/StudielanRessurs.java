package no.nav.sbl.dialogarena.rest.ressurser.inntekt;

import no.nav.metrics.aspects.Timed;
import no.nav.sbl.dialogarena.sendsoknad.domain.oidc.OidcFeatureToggleUtils;
import no.nav.sbl.dialogarena.sikkerhet.Tilgangskontroll;
import no.nav.sbl.dialogarena.soknadinnsending.business.service.TextService;
import no.nav.sbl.soknadsosialhjelp.soknad.JsonInternalSoknad;
import no.nav.sbl.soknadsosialhjelp.soknad.okonomi.JsonOkonomiopplysninger;
import no.nav.sbl.soknadsosialhjelp.soknad.okonomi.oversikt.JsonOkonomioversiktInntekt;
import no.nav.sbl.soknadsosialhjelp.soknad.utdanning.JsonUtdanning;
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
import static no.nav.sbl.dialogarena.soknadinnsending.business.mappers.OkonomiMapper.addInntektIfCheckedElseDeleteInOversikt;
import static no.nav.sbl.dialogarena.soknadinnsending.business.mappers.OkonomiMapper.setBekreftelse;
import static no.nav.sbl.dialogarena.soknadinnsending.business.mappers.TitleKeyMapper.soknadTypeToTitleKey;
import static no.nav.sbl.soknadsosialhjelp.json.SoknadJsonTyper.STUDIELAN;

@Controller
@ProtectedWithClaims(issuer = "selvbetjening", claimMap = {"acr=Level4"})
@Path("/soknader/{behandlingsId}/inntekt/studielan")
@Timed
@Produces(APPLICATION_JSON)
public class StudielanRessurs {

    @Inject
    private Tilgangskontroll tilgangskontroll;

    @Inject
    private SoknadUnderArbeidRepository soknadUnderArbeidRepository;

    @Inject
    private TextService textService;

    @GET
    public StudielanFrontend hentStudielanBekreftelse(@PathParam("behandlingsId") String behandlingsId) {
        String eier = OidcFeatureToggleUtils.getUserId();
        JsonInternalSoknad soknad = soknadUnderArbeidRepository.hentSoknad(behandlingsId, eier).getJsonInternalSoknad();
        JsonOkonomiopplysninger opplysninger = soknad.getSoknad().getData().getOkonomi().getOpplysninger();
        JsonUtdanning utdanning = soknad.getSoknad().getData().getUtdanning();
        StudielanFrontend studielanFrontend = new StudielanFrontend();

        if (utdanning.getErStudent() == null || !utdanning.getErStudent()) {
            return studielanFrontend;
        }
        studielanFrontend.setSkalVises(true);

        if (opplysninger.getBekreftelse() == null) {
            studielanFrontend.setSkalVises(false);
            return studielanFrontend;
        }

        setBekreftelseOnStudielanFrontend(opplysninger, studielanFrontend);

        return studielanFrontend;
    }

    @PUT
    public void updateStudielan(@PathParam("behandlingsId") String behandlingsId, StudielanFrontend studielanFrontend) {
        tilgangskontroll.verifiserAtBrukerKanEndreSoknad(behandlingsId);
        String eier = OidcFeatureToggleUtils.getUserId();
        SoknadUnderArbeid soknad = soknadUnderArbeidRepository.hentSoknad(behandlingsId, eier);
        JsonOkonomiopplysninger opplysninger = soknad.getJsonInternalSoknad().getSoknad().getData().getOkonomi().getOpplysninger();
        List<JsonOkonomioversiktInntekt> inntekter = soknad.getJsonInternalSoknad().getSoknad().getData().getOkonomi().getOversikt().getInntekt();

        if (opplysninger.getBekreftelse() == null) {
            opplysninger.setBekreftelse(new ArrayList<>());
        }

        setBekreftelse(opplysninger, STUDIELAN, studielanFrontend.bekreftelse, textService.getJsonOkonomiTittel("inntekt.student"));

        if (studielanFrontend.bekreftelse != null) {
            String tittel = textService.getJsonOkonomiTittel(soknadTypeToTitleKey.get(STUDIELAN));
            addInntektIfCheckedElseDeleteInOversikt(inntekter, STUDIELAN, tittel, studielanFrontend.bekreftelse);
        }

        soknadUnderArbeidRepository.oppdaterSoknadsdata(soknad, eier);
    }

    private void setBekreftelseOnStudielanFrontend(JsonOkonomiopplysninger opplysninger, StudielanFrontend studielanFrontend) {
        opplysninger.getBekreftelse().stream()
                .filter(bekreftelse -> bekreftelse.getType().equals(STUDIELAN))
                .findFirst()
                .ifPresent(jsonOkonomibekreftelse -> studielanFrontend.setBekreftelse(jsonOkonomibekreftelse.getVerdi()));
    }

    @XmlAccessorType(XmlAccessType.FIELD)
    public static final class StudielanFrontend {
        public boolean skalVises;
        public Boolean bekreftelse;

        public void setSkalVises(boolean skalVises) {
            this.skalVises = skalVises;
        }

        public void setBekreftelse(Boolean bekreftelse) {
            this.bekreftelse = bekreftelse;
        }
    }
}
