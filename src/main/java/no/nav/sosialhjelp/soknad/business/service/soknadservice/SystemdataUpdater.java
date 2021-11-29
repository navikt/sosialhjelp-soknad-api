package no.nav.sosialhjelp.soknad.business.service.soknadservice;

import no.nav.sosialhjelp.soknad.domain.SoknadUnderArbeid;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class SystemdataUpdater {

    private final List<Systemdata> systemdatas;

    public SystemdataUpdater(List<Systemdata> systemdatas) {
        this.systemdatas = systemdatas;
    }

    public void update(SoknadUnderArbeid soknadUnderArbeid, String token) {
        systemdatas.forEach(s -> s.updateSystemdataIn(soknadUnderArbeid, token));
    }
}
