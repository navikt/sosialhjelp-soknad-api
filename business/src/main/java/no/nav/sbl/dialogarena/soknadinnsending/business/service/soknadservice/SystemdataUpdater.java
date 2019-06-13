package no.nav.sbl.dialogarena.soknadinnsending.business.service.soknadservice;

import java.util.List;

import javax.inject.Inject;

import org.springframework.stereotype.Component;

import no.nav.sbl.sosialhjelp.domain.SoknadUnderArbeid;

@Component
public class SystemdataUpdater {

    @Inject
    private List<Systemdata> systemdatas;

    public void update(SoknadUnderArbeid soknadUnderArbeid) {
        systemdatas.forEach((s) -> s.updateSystemdataIn(soknadUnderArbeid));
    }
}
