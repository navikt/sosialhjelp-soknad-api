package no.nav.sbl.dialogarena.soknadinnsending.business.service.soknadservice;

import no.nav.sbl.sosialhjelp.domain.SoknadUnderArbeid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import javax.inject.Inject;
import java.util.List;

@Component
public class SystemdataUpdater {
    private static final Logger log = LoggerFactory.getLogger(SystemdataUpdater.class);

    @Inject
    private List<Systemdata> systemdatas;

    public void update(SoknadUnderArbeid soknadUnderArbeid, String token) {
        log.info("Systemdata update");
        systemdatas.forEach((s) -> s.updateSystemdataIn(soknadUnderArbeid, token));
    }
}
