package no.nav.sbl.dialogarena.rest.ressurser.arbeid;

import no.nav.metrics.aspects.Timed;
import no.nav.modig.core.context.SubjectHandler;
import no.nav.sbl.dialogarena.rest.ressurser.LegacyHelper;
import no.nav.sbl.dialogarena.sendsoknad.domain.Faktum;
import no.nav.sbl.dialogarena.sendsoknad.domain.WebSoknad;
import no.nav.sbl.dialogarena.sikkerhet.Tilgangskontroll;
import no.nav.sbl.dialogarena.soknadinnsending.business.service.FaktaService;
import no.nav.sbl.dialogarena.soknadinnsending.business.service.soknadservice.SoknadService;
import no.nav.sbl.soknadsosialhjelp.soknad.JsonInternalSoknad;
import no.nav.sbl.soknadsosialhjelp.soknad.arbeid.JsonKommentarTilArbeidsforhold;
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
@Path("/soknader/{behandlingsId}/arbeid/kommentarTilArbeidsforhold")
@Timed
@Produces(APPLICATION_JSON)
public class KommentarTilArbeidsforholdRessurs {

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
    public KommentarTilArbeidsforholdFrontend hentKommentarTilArbeidsforhold(@PathParam("behandlingsId") String behandlingsId) {
        final String eier = SubjectHandler.getSubjectHandler().getUid();
        final JsonInternalSoknad soknad = legacyHelper.hentSoknad(behandlingsId, eier).getJsonInternalSoknad();
        final JsonKommentarTilArbeidsforhold kommentarTilArbeidsforhold = soknad.getSoknad().getData().getArbeid().getKommentarTilArbeidsforhold();

        return new KommentarTilArbeidsforholdFrontend()
                .withBrukerdefinert(kommentarTilArbeidsforhold.getKilde() == JsonKildeBruker.BRUKER)
                .withKommentarTilArbeidsforhold(kommentarTilArbeidsforhold != null ? kommentarTilArbeidsforhold.getVerdi() : null);
    }

    @PUT
    public void updateKommentarTilArbeidsforhold(@PathParam("behandlingsId") String behandlingsId, KommentarTilArbeidsforholdFrontend kommentarTilArbeidsforholdFrontend) {
        tilgangskontroll.verifiserAtBrukerKanEndreSoknad(behandlingsId);
        update(behandlingsId, kommentarTilArbeidsforholdFrontend);
        legacyUpdate(behandlingsId, kommentarTilArbeidsforholdFrontend);
    }

    private void update(String behandlingsId, KommentarTilArbeidsforholdFrontend kommentarTilArbeidsforholdFrontend) {
        final String eier = SubjectHandler.getSubjectHandler().getUid();
        final SoknadUnderArbeid soknad = soknadUnderArbeidRepository.hentSoknad(behandlingsId, eier).get();
        final JsonKommentarTilArbeidsforhold kommentarTilArbeidsforhold = soknad.getJsonInternalSoknad().getSoknad().getData().getArbeid().getKommentarTilArbeidsforhold();
        kommentarTilArbeidsforhold.setKilde(kommentarTilArbeidsforholdFrontend.brukerdefinert ? JsonKildeBruker.BRUKER : JsonKildeBruker.UTDATERT);
        kommentarTilArbeidsforhold.setVerdi(kommentarTilArbeidsforholdFrontend.kommentarTilArbeidsforhold);
        soknadUnderArbeidRepository.oppdaterSoknadsdata(soknad, eier);
    }

    private void legacyUpdate(String behandlingsId, KommentarTilArbeidsforholdFrontend kommentarTilArbeidsforholdFrontend) {
        final WebSoknad webSoknad = soknadService.hentSoknad(behandlingsId, false, false);

        final Faktum kommentarTilArbeidsforhold = faktaService.hentFaktumMedKey(webSoknad.getSoknadId(), "opplysninger.arbeidsituasjon.kommentarer");
        kommentarTilArbeidsforhold.setValue(kommentarTilArbeidsforholdFrontend.kommentarTilArbeidsforhold);
        faktaService.lagreBrukerFaktum(kommentarTilArbeidsforhold);
    }


    @XmlAccessorType(XmlAccessType.FIELD)
    public static final class KommentarTilArbeidsforholdFrontend {
        public boolean brukerdefinert;
        public String kommentarTilArbeidsforhold;

        public KommentarTilArbeidsforholdFrontend withBrukerdefinert(boolean brukerdefinert) {
            this.brukerdefinert = brukerdefinert;
            return this;
        }

        public KommentarTilArbeidsforholdFrontend withKommentarTilArbeidsforhold(String kommentarTilArbeidsforhold) {
            this.kommentarTilArbeidsforhold = kommentarTilArbeidsforhold;
            return this;
        }
    }
}
