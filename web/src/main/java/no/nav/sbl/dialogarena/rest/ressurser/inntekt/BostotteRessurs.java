package no.nav.sbl.dialogarena.rest.ressurser.inntekt;

import no.nav.metrics.aspects.Timed;
import no.nav.modig.core.context.SubjectHandler;
import no.nav.sbl.dialogarena.rest.ressurser.LegacyHelper;
import no.nav.sbl.dialogarena.sendsoknad.domain.Faktum;
import no.nav.sbl.dialogarena.sendsoknad.domain.WebSoknad;
import no.nav.sbl.dialogarena.service.TextService;
import no.nav.sbl.dialogarena.sikkerhet.Tilgangskontroll;
import no.nav.sbl.dialogarena.soknadinnsending.business.service.FaktaService;
import no.nav.sbl.dialogarena.soknadinnsending.business.service.soknadservice.SoknadService;
import no.nav.sbl.soknadsosialhjelp.soknad.JsonInternalSoknad;
import no.nav.sbl.soknadsosialhjelp.soknad.common.JsonKilde;
import no.nav.sbl.soknadsosialhjelp.soknad.okonomi.JsonOkonomiopplysninger;
import no.nav.sbl.soknadsosialhjelp.soknad.okonomi.opplysning.JsonOkonomibekreftelse;
import no.nav.sbl.sosialhjelp.domain.SoknadUnderArbeid;
import no.nav.sbl.sosialhjelp.soknadunderbehandling.SoknadUnderArbeidRepository;
import org.springframework.stereotype.Controller;

import javax.inject.Inject;
import javax.ws.rs.*;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import java.util.List;
import java.util.Optional;

import static javax.ws.rs.core.MediaType.APPLICATION_JSON;

@Controller
@Path("/soknader/{behandlingsId}/inntekt/bostotte")
@Timed
@Produces(APPLICATION_JSON)
public class BostotteRessurs {

    @Inject
    private LegacyHelper legacyHelper;

    @Inject
    private Tilgangskontroll tilgangskontroll;

    @Inject
    private SoknadUnderArbeidRepository soknadUnderArbeidRepository;

    @Inject
    private TextService textService;

    @Inject
    private SoknadService soknadService;

    @Inject
    private FaktaService faktaService;

    @GET
    public BostotteFrontend hentBostotteBekreftelse(@PathParam("behandlingsId") String behandlingsId){
        final String eier = SubjectHandler.getSubjectHandler().getUid();
        final JsonInternalSoknad soknad = legacyHelper.hentSoknad(behandlingsId, eier).getJsonInternalSoknad();
        final Optional<JsonOkonomibekreftelse> bostotteBekreftelse = soknad.getSoknad()
                .getData().getOkonomi().getOpplysninger().getBekreftelse().stream()
                .filter(bekreftelse -> bekreftelse.getType().equals("bostotte")).findFirst();
        if (bostotteBekreftelse.isPresent()){
            return new BostotteFrontend().withBostotteBekreftelse(bostotteBekreftelse.get().getVerdi());
        }

        return new BostotteFrontend();
    }

    @PUT
    public void updateBostotte(@PathParam("behandlingsId") String behandlingsId, BostotteFrontend bostotteFrontend){
        tilgangskontroll.verifiserAtBrukerKanEndreSoknad(behandlingsId);
        update(behandlingsId, bostotteFrontend);
        legacyUpdate(behandlingsId, bostotteFrontend);
    }

    private void update(String behandlingsId, BostotteFrontend bostotteFrontend) {
        final String eier = SubjectHandler.getSubjectHandler().getUid();
        final SoknadUnderArbeid soknad = soknadUnderArbeidRepository.hentSoknad(behandlingsId, eier).get();
        final JsonOkonomiopplysninger opplysninger = soknad.getJsonInternalSoknad().getSoknad().getData().getOkonomi().getOpplysninger();
        final Optional<JsonOkonomibekreftelse> bostotteBekreftelse = opplysninger.getBekreftelse().stream()
                .filter(bekreftelse -> bekreftelse.getType().equals("bostotte")).findFirst();
        if (bostotteBekreftelse.isPresent()){
            bostotteBekreftelse.get().withKilde(JsonKilde.BRUKER).withVerdi(bostotteFrontend.bostotteBekreftelse);
        } else {
            List<JsonOkonomibekreftelse> bekreftelser = opplysninger.getBekreftelse();
            bekreftelser.add(new JsonOkonomibekreftelse()
                    .withKilde(JsonKilde.BRUKER)
                    .withType("bostotte")
                    .withTittel(textService.getJsonOkonomiTittel("inntekt.bostotte"))
                    .withVerdi(bostotteFrontend.bostotteBekreftelse));
        }
        soknadUnderArbeidRepository.oppdaterSoknadsdata(soknad, eier);
    }

    private void legacyUpdate(String behandlingsId, BostotteFrontend bostotteFrontend) {
        final WebSoknad webSoknad = soknadService.hentSoknad(behandlingsId, false, false);

        final Faktum bostotte = faktaService.hentFaktumMedKey(webSoknad.getSoknadId(), "inntekt.bostotte");
        bostotte.setValue(bostotteFrontend.bostotteBekreftelse.toString());
        faktaService.lagreBrukerFaktum(bostotte);
    }

    @XmlAccessorType(XmlAccessType.FIELD)
    public static final class BostotteFrontend {
        public Boolean bostotteBekreftelse;

        public BostotteFrontend withBostotteBekreftelse(Boolean bostotteBekreftelse) {
            this.bostotteBekreftelse = bostotteBekreftelse;
            return this;
        }
    }
}
