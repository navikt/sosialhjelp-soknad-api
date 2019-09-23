package no.nav.sbl.dialogarena.rest.ressurser.bostotte;

import no.nav.metrics.aspects.Timed;
import no.nav.sbl.dialogarena.sendsoknad.domain.oidc.OidcFeatureToggleUtils;
import no.nav.sbl.soknadsosialhjelp.digisos.soker.hendelse.JsonSaksStatus;
import no.nav.sbl.soknadsosialhjelp.soknad.JsonInternalSoknad;
import no.nav.sbl.soknadsosialhjelp.soknad.okonomi.opplysning.JsonOkonomiOpplysningUtbetaling;
import no.nav.sbl.sosialhjelp.soknadunderbehandling.SoknadUnderArbeidRepository;
import no.nav.security.oidc.api.ProtectedWithClaims;
import org.springframework.stereotype.Controller;

import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import java.util.List;
import java.util.stream.Collectors;

import static javax.ws.rs.core.MediaType.APPLICATION_JSON;
import static no.nav.sbl.dialogarena.bostotte.Bostotte.HUSBANKEN_TYPE;

@Controller
@ProtectedWithClaims(issuer = "selvbetjening", claimMap = { "acr=Level4" })
@Path("/soknader/{behandlingsId}/bostotte")
@Timed
@Produces(APPLICATION_JSON)
public class BostotteRessurs {

    @Inject
    private SoknadUnderArbeidRepository soknadUnderArbeidRepository;

    @GET
    public BostotteFrontend hentBostotte(@PathParam("behandlingsId") String behandlingsId) {
        final String eier = OidcFeatureToggleUtils.getUserId();
        final JsonInternalSoknad soknad = soknadUnderArbeidRepository.hentSoknad(behandlingsId, eier).getJsonInternalSoknad();

        return new BostotteFrontend()
                .withUtbetalinger(mapToUtbetalinger(soknad))
                .withSaksStatuser(mapToUtSaksStatuser(soknad));
    }

    private List<JsonOkonomiOpplysningUtbetaling> mapToUtbetalinger(JsonInternalSoknad soknad) {
        return soknad.getSoknad().getData().getOkonomi().getOpplysninger().getUtbetaling().stream()
                .filter(utbetaling -> utbetaling.getType().equals(HUSBANKEN_TYPE))
                .collect(Collectors.toList());
    }

    private List<JsonSaksStatus> mapToUtSaksStatuser(JsonInternalSoknad soknad) {
        return null;
    }

    @XmlAccessorType(XmlAccessType.FIELD)
    public static final class BostotteFrontend {
        public List<JsonOkonomiOpplysningUtbetaling> utbetalinger;
        public List<JsonSaksStatus> saksStatuser;

        public BostotteFrontend withUtbetalinger(List<JsonOkonomiOpplysningUtbetaling> utbetalinger) {
            this.utbetalinger = utbetalinger;
            return this;
        }

        public BostotteFrontend withSaksStatuser(List<JsonSaksStatus> saksStatuser) {
            this.saksStatuser = saksStatuser;
            return this;
        }
    }
}
