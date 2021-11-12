package no.nav.sosialhjelp.soknad.web.rest.ressurser.personalia;

import no.nav.sbl.soknadsosialhjelp.soknad.JsonInternalSoknad;
import no.nav.sbl.soknadsosialhjelp.soknad.common.JsonNavn;
import no.nav.sbl.soknadsosialhjelp.soknad.personalia.JsonPersonalia;
import no.nav.security.token.support.core.api.ProtectedWithClaims;
import no.nav.sosialhjelp.metrics.aspects.Timed;
import no.nav.sosialhjelp.soknad.business.db.repositories.soknadunderarbeid.SoknadUnderArbeidRepository;
import no.nav.sosialhjelp.soknad.client.kodeverk.KodeverkService;
import no.nav.sosialhjelp.soknad.domain.model.oidc.SubjectHandler;
import no.nav.sosialhjelp.soknad.web.rest.ressurser.NavnFrontend;
import no.nav.sosialhjelp.soknad.web.sikkerhet.Tilgangskontroll;
import org.springframework.stereotype.Controller;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;

import static javax.ws.rs.core.MediaType.APPLICATION_JSON;
import static no.nav.sosialhjelp.soknad.web.utils.Constants.CLAIM_ACR_LEVEL_4;
import static no.nav.sosialhjelp.soknad.web.utils.Constants.SELVBETJENING;

@Controller
@ProtectedWithClaims(issuer = SELVBETJENING, claimMap = {CLAIM_ACR_LEVEL_4})
@Path("/soknader/{behandlingsId}/personalia/basisPersonalia")
@Timed
@Produces(APPLICATION_JSON)
public class BasisPersonaliaRessurs {

    private final KodeverkService kodeverkService;
    private final SoknadUnderArbeidRepository soknadUnderArbeidRepository;
    private final Tilgangskontroll tilgangskontroll;

    public BasisPersonaliaRessurs(
            KodeverkService kodeverkService,
            SoknadUnderArbeidRepository soknadUnderArbeidRepository,
            Tilgangskontroll tilgangskontroll
    ) {
        this.kodeverkService = kodeverkService;
        this.soknadUnderArbeidRepository = soknadUnderArbeidRepository;
        this.tilgangskontroll = tilgangskontroll;
    }

    @GET
    public BasisPersonaliaFrontend hentBasisPersonalia(@PathParam("behandlingsId") String behandlingsId) {
        tilgangskontroll.verifiserAtBrukerHarTilgang();
        final String eier = SubjectHandler.getUserId();
        JsonInternalSoknad soknad = soknadUnderArbeidRepository.hentSoknad(behandlingsId, eier).getJsonInternalSoknad();

        return mapToBasisPersonaliaFrontend(soknad.getSoknad().getData().getPersonalia());
    }

    private BasisPersonaliaFrontend mapToBasisPersonaliaFrontend(JsonPersonalia jsonPersonalia) {
        final JsonNavn navn = jsonPersonalia.getNavn();
        return new BasisPersonaliaFrontend()
                .withNavn(new NavnFrontend(navn.getFornavn(), navn.getMellomnavn(), navn.getEtternavn()))
                .withFodselsnummer(jsonPersonalia.getPersonIdentifikator().getVerdi())
                .withStatsborgerskap(jsonPersonalia.getStatsborgerskap() == null ? null :
                        kodeverkService.getLand(jsonPersonalia.getStatsborgerskap().getVerdi()))
                .withNordiskBorger(jsonPersonalia.getNordiskBorger() != null ? jsonPersonalia.getNordiskBorger().getVerdi() : null);
    }

    @XmlAccessorType(XmlAccessType.FIELD)
    public static final class BasisPersonaliaFrontend {
        public NavnFrontend navn;
        public String fodselsnummer;
        public String statsborgerskap;
        public Boolean nordiskBorger;

        public BasisPersonaliaFrontend withNavn(NavnFrontend navn) {
            this.navn = navn;
            return this;
        }

        public BasisPersonaliaFrontend withFodselsnummer(String fodselsnummer) {
            this.fodselsnummer = fodselsnummer;
            return this;
        }

        public BasisPersonaliaFrontend withStatsborgerskap(String statsborgerskap) {
            this.statsborgerskap = statsborgerskap;
            return this;
        }

        public BasisPersonaliaFrontend withNordiskBorger(Boolean nordiskBorger) {
            this.nordiskBorger = nordiskBorger;
            return this;
        }
    }
}
