package no.nav.sbl.dialogarena.rest.ressurser.inntekt;

import no.nav.metrics.aspects.Timed;
import no.nav.sbl.dialogarena.sendsoknad.domain.oidc.OidcFeatureToggleUtils;
import no.nav.sbl.dialogarena.sikkerhet.Tilgangskontroll;
import no.nav.sbl.dialogarena.soknadinnsending.business.service.TextService;
import no.nav.sbl.soknadsosialhjelp.soknad.JsonInternalSoknad;
import no.nav.sbl.soknadsosialhjelp.soknad.okonomi.JsonOkonomiopplysninger;
import no.nav.sbl.soknadsosialhjelp.soknad.okonomi.opplysning.JsonOkonomibekreftelse;
import no.nav.sbl.soknadsosialhjelp.soknad.okonomi.oversikt.JsonOkonomioversiktInntekt;
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
import java.util.Optional;

import static javax.ws.rs.core.MediaType.APPLICATION_JSON;
import static no.nav.sbl.dialogarena.soknadinnsending.business.mappers.FaktumNoklerOgBelopNavnMapper.soknadTypeToFaktumKey;
import static no.nav.sbl.dialogarena.soknadinnsending.business.mappers.OkonomiMapper.addInntektIfCheckedElseDeleteInOversikt;
import static no.nav.sbl.dialogarena.soknadinnsending.business.mappers.OkonomiMapper.setBekreftelse;

@Controller
@ProtectedWithClaims(issuer = "selvbetjening", claimMap = { "acr=Level4" })
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
    public BostotteFrontend hentBostotteBekreftelse(@PathParam("behandlingsId") String behandlingsId){
        final String eier = OidcFeatureToggleUtils.getUserId();
        final JsonInternalSoknad soknad = soknadUnderArbeidRepository.hentSoknad(behandlingsId, eier).get().getJsonInternalSoknad();
        final JsonOkonomiopplysninger opplysninger = soknad.getSoknad().getData().getOkonomi().getOpplysninger();
        final BostotteFrontend bostotteFrontend = new BostotteFrontend();

        if (opplysninger.getBekreftelse() == null){
            return bostotteFrontend;
        }

        setBekreftelseOnBostotteFrontend(opplysninger, bostotteFrontend);

        return bostotteFrontend;
    }

    @PUT
    public void updateBostotte(@PathParam("behandlingsId") String behandlingsId, BostotteFrontend bostotteFrontend){
        tilgangskontroll.verifiserAtBrukerKanEndreSoknad(behandlingsId);
        update(behandlingsId, bostotteFrontend);
    }

    private void update(String behandlingsId, BostotteFrontend bostotteFrontend) {
        final String eier = OidcFeatureToggleUtils.getUserId();
        final SoknadUnderArbeid soknad = soknadUnderArbeidRepository.hentSoknad(behandlingsId, eier).get();
        final JsonOkonomiopplysninger opplysninger = soknad.getJsonInternalSoknad().getSoknad().getData().getOkonomi().getOpplysninger();
        final List<JsonOkonomioversiktInntekt> inntekter = soknad.getJsonInternalSoknad().getSoknad().getData().getOkonomi().getOversikt().getInntekt();
        String soknadstype = "bostotte";

        if (opplysninger.getBekreftelse() == null){
            opplysninger.setBekreftelse(new ArrayList<>());
        }

        setBekreftelse(opplysninger, soknadstype, bostotteFrontend.bekreftelse, textService.getJsonOkonomiTittel("inntekt.bostotte"));

        if (bostotteFrontend.bekreftelse != null){
            String tittel = textService.getJsonOkonomiTittel(soknadTypeToFaktumKey.get(soknadstype));
            addInntektIfCheckedElseDeleteInOversikt(inntekter, soknadstype, tittel, bostotteFrontend.bekreftelse);
        }

        soknadUnderArbeidRepository.oppdaterSoknadsdata(soknad, eier);
    }

    private void setBekreftelseOnBostotteFrontend(JsonOkonomiopplysninger opplysninger, BostotteFrontend bostotteFrontend) {
        final Optional<JsonOkonomibekreftelse> bostotteBekreftelse = opplysninger.getBekreftelse().stream()
                .filter(bekreftelse -> bekreftelse.getType().equals("bostotte")).findFirst();
        if (bostotteBekreftelse.isPresent()){
            bostotteFrontend.setBekreftelse(bostotteBekreftelse.get().getVerdi());
        }
    }

    @XmlAccessorType(XmlAccessType.FIELD)
    public static final class BostotteFrontend {
        public Boolean bekreftelse;

        public void setBekreftelse(Boolean bekreftelse) {
            this.bekreftelse = bekreftelse;
        }
    }
}
