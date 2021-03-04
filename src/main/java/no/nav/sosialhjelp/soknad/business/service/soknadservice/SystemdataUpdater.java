package no.nav.sosialhjelp.soknad.business.service.soknadservice;

import no.nav.sosialhjelp.soknad.domain.SoknadUnderArbeid;
import org.springframework.stereotype.Component;

import javax.inject.Inject;
import java.util.List;

@Component
public class SystemdataUpdater {

    @Inject
    private List<Systemdata> systemdatas;

    public void update(SoknadUnderArbeid soknadUnderArbeid, String token) {
        systemdatas.forEach(s -> s.updateSystemdataIn(soknadUnderArbeid, token));
    }
}
