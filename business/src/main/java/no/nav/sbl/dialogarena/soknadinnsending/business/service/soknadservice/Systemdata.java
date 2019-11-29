package no.nav.sbl.dialogarena.soknadinnsending.business.service.soknadservice;

import no.nav.sbl.sosialhjelp.domain.SoknadUnderArbeid;

public interface Systemdata {

    void updateSystemdataIn(SoknadUnderArbeid soknadUnderArbeid, String token);
    
}
