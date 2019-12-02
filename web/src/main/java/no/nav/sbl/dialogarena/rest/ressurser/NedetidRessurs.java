package no.nav.sbl.dialogarena.rest.ressurser;

import no.nav.metrics.aspects.Timed;
import no.nav.sbl.dialogarena.utils.NedetidUtils;
import no.nav.security.oidc.api.Unprotected;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;

import javax.ws.rs.*;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import java.time.LocalDateTime;

import static javax.ws.rs.core.MediaType.APPLICATION_JSON;
import static no.nav.sbl.dialogarena.utils.NedetidUtils.*;

@Controller
@Unprotected
@Path("/nedetid")
@Timed
@Produces(APPLICATION_JSON)
public class NedetidRessurs {
    private static final Logger log = LoggerFactory.getLogger(NedetidRessurs.class);


    @GET
    public NedetidFrontend hentNedetidInformasjon() {
        return new NedetidFrontend()
                .withNedetidStarter(getNedetid(NEDETID_START))
                .withNedetidSlutter(getNedetid(NEDETID_SLUTT))
                .withIsNedetid(isInnenforNedetid())
                .withIsPlanlagtNedetid(isInnenforPlanlagtNedetid());
    }

    @SuppressWarnings("WeakerAccess")
    @XmlAccessorType(XmlAccessType.FIELD)
    public static class NedetidFrontend {
        public boolean isNedetid;
        public boolean isPlanlagtNedetid;
        public String nedetidStarter;
        public String nedetidSlutter;

        public NedetidFrontend withIsNedetid(boolean isNedetid) {
            this.isNedetid = isNedetid;
            return this;
        }

        public NedetidFrontend withIsPlanlagtNedetid(boolean isPlanlagtNedetid) {
            this.isPlanlagtNedetid = isPlanlagtNedetid;
            return this;
        }

        public NedetidFrontend withNedetidStarter(LocalDateTime nedetidStarter) {
            this.nedetidStarter = (nedetidStarter == null ? null : nedetidStarter.format(NedetidUtils.dateFormat));
            return this;
        }

        public NedetidFrontend withNedetidSlutter(LocalDateTime nedetidSlutter) {
            this.nedetidSlutter = (nedetidSlutter == null ? null : nedetidSlutter.format(NedetidUtils.dateFormat));
            return this;
        }
    }
}
