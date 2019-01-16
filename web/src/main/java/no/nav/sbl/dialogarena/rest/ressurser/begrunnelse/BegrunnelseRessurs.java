package no.nav.sbl.dialogarena.rest.ressurser.begrunnelse;

import no.nav.metrics.aspects.Timed;
import no.nav.modig.core.context.SubjectHandler;
import no.nav.sbl.dialogarena.rest.ressurser.LegacyHelper;
import no.nav.sbl.dialogarena.sendsoknad.domain.Faktum;
import no.nav.sbl.dialogarena.sendsoknad.domain.WebSoknad;
import no.nav.sbl.dialogarena.sikkerhet.Tilgangskontroll;
import no.nav.sbl.dialogarena.soknadinnsending.business.service.FaktaService;
import no.nav.sbl.dialogarena.soknadinnsending.business.service.soknadservice.SoknadService;
import no.nav.sbl.soknadsosialhjelp.soknad.JsonInternalSoknad;
import no.nav.sbl.soknadsosialhjelp.soknad.begrunnelse.JsonBegrunnelse;
import no.nav.sbl.soknadsosialhjelp.soknad.common.JsonKildeBruker;
import no.nav.sbl.sosialhjelp.domain.SoknadUnderArbeid;
import no.nav.sbl.sosialhjelp.soknadunderbehandling.SoknadUnderArbeidRepository;
import org.springframework.stereotype.Controller;

import javax.inject.Inject;
import javax.ws.rs.*;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;

import static javax.ws.rs.core.MediaType.APPLICATION_JSON;

@Controller
@Path("/soknader/{behandlingsId}/begrunnelse")
@Timed
@Produces(APPLICATION_JSON)
public class BegrunnelseRessurs {

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


    @GET
    public BegrunnelseFrontend hentBegrunnelse(@PathParam("behandlingsId") String behandlingsId) {
        final String eier = SubjectHandler.getSubjectHandler().getUid();
        final JsonInternalSoknad soknad = legacyHelper.hentSoknad(behandlingsId, eier).getJsonInternalSoknad();
        final JsonBegrunnelse begrunnelse = soknad.getSoknad().getData().getBegrunnelse();

        return new BegrunnelseFrontend()
                .withBrukerdefinert(begrunnelse.getKilde() == JsonKildeBruker.BRUKER)
                .withHvaSokesOm(begrunnelse != null ? begrunnelse.getHvaSokesOm() : null)
                .withHvorforSoke(begrunnelse != null ? begrunnelse.getHvorforSoke() : null);
    }

    @PUT
    public void updateBegrunnelse(@PathParam("behandlingsId") String behandlingsId, BegrunnelseFrontend begrunnelseFrontend) {
        tilgangskontroll.verifiserAtBrukerKanEndreSoknad(behandlingsId);
        update(behandlingsId, begrunnelseFrontend);
        legacyUpdate(behandlingsId, begrunnelseFrontend);
    }

    private void update(String behandlingsId, BegrunnelseFrontend begrunnelseFrontend) {
        final String eier = SubjectHandler.getSubjectHandler().getUid();
        final SoknadUnderArbeid soknad = soknadUnderArbeidRepository.hentSoknad(behandlingsId, eier).get();
        final JsonBegrunnelse begrunnelse = soknad.getJsonInternalSoknad().getSoknad().getData().getBegrunnelse();
        begrunnelse.setKilde(begrunnelseFrontend.brukerdefinert ? JsonKildeBruker.BRUKER : JsonKildeBruker.UTDATERT);
        begrunnelse.setHvaSokesOm(begrunnelseFrontend.hvaSokesOm);
        begrunnelse.setHvaSokesOm(begrunnelseFrontend.hvorforSoke);
        soknadUnderArbeidRepository.oppdaterSoknadsdata(soknad, eier);
    }

    private void legacyUpdate(String behandlingsId, BegrunnelseFrontend begrunnelseFrontend) {
        final WebSoknad webSoknad = soknadService.hentSoknad(behandlingsId, false, false);

        final Faktum hva = faktaService.hentFaktumMedKey(webSoknad.getSoknadId(), "begrunnelse.hva");
        hva.setValue(begrunnelseFrontend.hvaSokesOm);
        faktaService.lagreBrukerFaktum(hva);

        final Faktum hvorfor = faktaService.hentFaktumMedKey(webSoknad.getSoknadId(), "begrunnelse.hvorfor");
        hvorfor.setValue(begrunnelseFrontend.hvorforSoke);
        faktaService.lagreBrukerFaktum(hvorfor);
    }


    @XmlAccessorType(XmlAccessType.FIELD)
    public static final class BegrunnelseFrontend {
        public boolean brukerdefinert;
        public String hvaSokesOm;
        public String hvorforSoke;

        public BegrunnelseFrontend withBrukerdefinert(boolean brukerdefinert) {
            this.brukerdefinert = brukerdefinert;
            return this;
        }

        public BegrunnelseFrontend withHvaSokesOm(String hvaSokesOm) {
            this.hvaSokesOm = hvaSokesOm;
            return this;
        }

        public BegrunnelseFrontend withHvorforSoke(String hvorforSoke) {
            this.hvorforSoke = hvorforSoke;
            return this;
        }
    }
}
