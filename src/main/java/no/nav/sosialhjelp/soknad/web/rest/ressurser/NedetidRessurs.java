package no.nav.sosialhjelp.soknad.web.rest.ressurser;

import no.nav.metrics.aspects.Timed;
import no.nav.security.token.support.core.api.Unprotected;
import org.springframework.stereotype.Controller;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;

import static javax.ws.rs.core.MediaType.APPLICATION_JSON;
import static no.nav.sosialhjelp.soknad.web.utils.NedetidUtils.NEDETID_SLUTT;
import static no.nav.sosialhjelp.soknad.web.utils.NedetidUtils.NEDETID_START;
import static no.nav.sosialhjelp.soknad.web.utils.NedetidUtils.getNedetidAsHumanReadable;
import static no.nav.sosialhjelp.soknad.web.utils.NedetidUtils.getNedetidAsHumanReadableEn;
import static no.nav.sosialhjelp.soknad.web.utils.NedetidUtils.getNedetidAsStringOrNull;
import static no.nav.sosialhjelp.soknad.web.utils.NedetidUtils.isInnenforNedetid;
import static no.nav.sosialhjelp.soknad.web.utils.NedetidUtils.isInnenforPlanlagtNedetid;

@Controller
@Unprotected
@Path("/nedetid")
@Timed
@Produces(APPLICATION_JSON)
public class NedetidRessurs {

    @GET
    public NedetidFrontend hentNedetidInformasjon() {
        return new NedetidFrontend()
                .withNedetidStart(getNedetidAsStringOrNull(NEDETID_START))
                .withNedetidSlutt(getNedetidAsStringOrNull(NEDETID_SLUTT))
                .withNedetidStartText(getNedetidAsHumanReadable(NEDETID_START))
                .withNedetidSluttText(getNedetidAsHumanReadable(NEDETID_SLUTT))
                .withNedetidStartTextEn(getNedetidAsHumanReadableEn(NEDETID_START))
                .withNedetidSluttTextEn(getNedetidAsHumanReadableEn(NEDETID_SLUTT))
                .withIsNedetid(isInnenforNedetid())
                .withIsPlanlagtNedetid(isInnenforPlanlagtNedetid());
    }

    @SuppressWarnings("WeakerAccess")
    @XmlAccessorType(XmlAccessType.FIELD)
    public static class NedetidFrontend {
        public boolean isNedetid;
        public boolean isPlanlagtNedetid;
        public String nedetidStart;
        public String nedetidSlutt;
        public String nedetidStartText;
        public String nedetidSluttText;
        public String nedetidStartTextEn;
        public String nedetidSluttTextEn;

        public NedetidFrontend withIsNedetid(boolean isNedetid) {
            this.isNedetid = isNedetid;
            return this;
        }

        public NedetidFrontend withIsPlanlagtNedetid(boolean isPlanlagtNedetid) {
            this.isPlanlagtNedetid = isPlanlagtNedetid;
            return this;
        }

        public NedetidFrontend withNedetidStart(String nedetidStart) {
            this.nedetidStart = nedetidStart;
            return this;
        }

        public NedetidFrontend withNedetidSlutt(String nedetidSlutt) {
            this.nedetidSlutt = nedetidSlutt;
            return this;
        }

        public NedetidFrontend withNedetidStartText(String nedetidStartText) {
            this.nedetidStartText = nedetidStartText;
            return this;
        }

        public NedetidFrontend withNedetidSluttText(String nedetidSluttText) {
            this.nedetidSluttText = nedetidSluttText;
            return this;
        }

        public NedetidFrontend withNedetidStartTextEn(String nedetidStartTextEn) {
            this.nedetidStartTextEn = nedetidStartTextEn;
            return this;
        }

        public NedetidFrontend withNedetidSluttTextEn(String nedetidSluttTextEn) {
            this.nedetidSluttTextEn = nedetidSluttTextEn;
            return this;
        }
    }
}
