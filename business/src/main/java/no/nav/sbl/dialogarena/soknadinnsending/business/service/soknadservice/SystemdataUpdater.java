package no.nav.sbl.dialogarena.soknadinnsending.business.service.soknadservice;

import no.nav.sbl.sosialhjelp.domain.SoknadUnderArbeid;
import org.springframework.stereotype.Component;

import javax.inject.Inject;
import java.util.List;

@Component
public class SystemdataUpdater {

    @Inject
    private List<Systemdata> systemdatas;

    public void update(SoknadUnderArbeid soknadUnderArbeid) {
        systemdatas.stream().forEach((s) -> s.updateSystemdataIn(soknadUnderArbeid));
    }
}
