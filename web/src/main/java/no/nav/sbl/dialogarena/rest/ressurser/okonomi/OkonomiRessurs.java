package no.nav.sbl.dialogarena.rest.ressurser.okonomi;

import no.nav.metrics.aspects.Timed;
import no.nav.modig.core.context.SubjectHandler;
import no.nav.sbl.dialogarena.rest.ressurser.LegacyHelper;
import no.nav.sbl.dialogarena.sendsoknad.domain.Faktum;
import no.nav.sbl.dialogarena.sendsoknad.domain.WebSoknad;
import no.nav.sbl.dialogarena.sendsoknad.domain.inntektsogskatteopplysninger.InntektOgskatteopplysningerConsumer;
import no.nav.sbl.dialogarena.sikkerhet.Tilgangskontroll;
import no.nav.sbl.dialogarena.soknadinnsending.business.service.FaktaService;
import no.nav.sbl.dialogarena.soknadinnsending.business.service.soknadservice.SoknadService;
import no.nav.sbl.dialogarena.soknadinnsending.business.service.systemdata.InntektOgSkatteopplysningerSystemdata;
import no.nav.sbl.soknadsosialhjelp.soknad.JsonInternalSoknad;
import no.nav.sbl.soknadsosialhjelp.soknad.okonomi.JsonOkonomi;
import no.nav.sbl.soknadsosialhjelp.soknad.common.JsonKildeBruker;
import no.nav.sbl.soknadsosialhjelp.soknad.okonomi.JsonOkonomioversikt;
import no.nav.sbl.soknadsosialhjelp.soknad.okonomi.oversikt.JsonOkonomioversiktInntekt;
import no.nav.sbl.sosialhjelp.domain.SoknadUnderArbeid;
import no.nav.sbl.sosialhjelp.soknadunderbehandling.SoknadUnderArbeidRepository;
import org.springframework.stereotype.Controller;

import javax.inject.Inject;
import javax.ws.rs.*;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;

import java.time.LocalDate;
import java.util.Collections;

import static javax.ws.rs.core.MediaType.APPLICATION_JSON;

@Controller
@Path("/soknader/{behandlingsId}/okonomi")
@Timed
@Produces(APPLICATION_JSON)
public class OkonomiRessurs {

    @Inject
    private SoknadService soknadService;

    @Inject
    private FaktaService faktaService;

    @Inject
    private Tilgangskontroll tilgangskontroll;

    @Inject
    private LegacyHelper legacyHelper;

    @Inject
    private SoknadUnderArbeidRepository soknadUnderArbeidRepository;

    @Inject
    private InntektOgSkatteopplysningerSystemdata inntektOgSkatteopplysningerSystemdata;

    @GET
    public OkonomiFrontend hentOkonomi(@PathParam("behandlingsId") String behandlingsId) {
        String eier = SubjectHandler.getSubjectHandler().getUid();

        JsonInternalSoknad soknad = legacyHelper.hentSoknad(behandlingsId, eier, false).getJsonInternalSoknad();

          //  inntektOgSkatteopplysningerSystemdata.updateSystemdataIn(soknad);
        return null;
//        new OkonomiFrontend()
//                .withBotype(okonomi.getBotype())
//                .withAntallPersoner(okonomi.getAntallPersoner());
    }

    @PUT
    public void updateOkonomi(@PathParam("behandlingsId") String behandlingsId, OkonomiFrontend okonomiFrontend) {
       // tilgangskontroll.verifiserAtBrukerKanEndreSoknad(behandlingsId);
       // update(behandlingsId, okonomiFrontend);
       // legacyUpdate(behandlingsId, okonomiFrontend);
    }

    private void update(String behandlingsId, OkonomiFrontend okonomiFrontend) {
        String eier = SubjectHandler.getSubjectHandler().getUid();
        SoknadUnderArbeid soknad = soknadUnderArbeidRepository.hentSoknad(behandlingsId, eier).get();
        JsonOkonomi okonomi = soknad.getJsonInternalSoknad().getSoknad().getData().getOkonomi();

        okonomi.getOpplysninger().getUtbetaling().add(null);

        soknadUnderArbeidRepository.oppdaterSoknadsdata(soknad, eier);
    }

    private void legacyUpdate(String behandlingsId, OkonomiFrontend okonomiFrontend) {
    }


    @XmlAccessorType(XmlAccessType.FIELD)
    public static final class OkonomiFrontend {
        public JsonOkonomioversikt jsonOkonomioversikt;

        public OkonomiFrontend withOkonomioversikt(JsonOkonomioversikt okonomioversikt) {
            this.jsonOkonomioversikt = okonomioversikt;
            return this;
        }
    }
}
