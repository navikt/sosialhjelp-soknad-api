package no.nav.sbl.sosialhjelp.soknad;

import no.nav.sbl.sosialhjelp.domain.SoknadUnderArbeid;

public interface SoknadUnderArbeidRepository {

    Long opprettSoknad(SoknadUnderArbeid soknadUnderArbeid, String eier);
    SoknadUnderArbeid hentSoknad(Long soknadId, String eier);
    SoknadUnderArbeid hentSoknad(String behandlingsId, String eier);
    void oppdatereSoknadsdata(SoknadUnderArbeid soknadUnderArbeid, String eier);
    void slettSoknad(SoknadUnderArbeid soknadUnderArbeid, String eier);
}
