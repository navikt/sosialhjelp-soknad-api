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

@Controller
@Unprotected
@Path("/nedetid")
@Timed
@Produces(APPLICATION_JSON)
public class NedetidRessurs {
    private static final Logger log = LoggerFactory.getLogger(NedetidRessurs.class);


    @GET
    public NedetidFrontend hentNedetidInformasjon() {
        LocalDateTime nedetidStart = NedetidUtils.getNedetid(NedetidUtils.NEDETID_START);
        LocalDateTime nedetidSlutt = NedetidUtils.getNedetid(NedetidUtils.NEDETID_SLUTT);

        if (nedetidStart == null || nedetidSlutt == null) {
            return new NedetidFrontend();
        }
        NedetidFrontend nedetidResponse = new NedetidFrontend()
                .withNedetidStarter(nedetidStart)
                .withNedetidSlutter(nedetidSlutt);

        LocalDateTime now = LocalDateTime.now();

        if (NedetidUtils.isUtenforNedetidEllerPlanlagtNedetid(now, nedetidStart, nedetidSlutt)){
            return nedetidResponse;
        }

        if (NedetidUtils.isInnenforPlanlagtNedetid(now, nedetidStart)) {
            return nedetidResponse.withIsPlanlagtNedetid(true);
        }

        if (NedetidUtils.isInnenforNedetid(now, nedetidStart, nedetidSlutt)) {
            return nedetidResponse.withIsNedetid(true);
        }

        log.error("Nedetidscase som ikke er tenkt på, start {}, slutt {}", nedetidStart, nedetidSlutt);
        return new NedetidFrontend();
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
            this.nedetidStarter = nedetidStarter.format(NedetidUtils.dateFormat);
            return this;
        }

        public NedetidFrontend withNedetidSlutter(LocalDateTime nedetidSlutter) {
            this.nedetidSlutter = nedetidSlutter.format(NedetidUtils.dateFormat);
            return this;
        }
    }
}
