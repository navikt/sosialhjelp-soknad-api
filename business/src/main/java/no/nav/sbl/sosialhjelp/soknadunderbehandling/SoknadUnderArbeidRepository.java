package no.nav.sbl.sosialhjelp.soknadunderbehandling;

import no.nav.sbl.sosialhjelp.domain.SoknadUnderArbeid;

import java.util.Optional;

public interface SoknadUnderArbeidRepository {

    Long opprettSoknad(SoknadUnderArbeid soknadUnderArbeid, String eier);
    Optional<SoknadUnderArbeid> hentSoknad(Long soknadId, String eier);
    Optional<SoknadUnderArbeid> hentSoknad(String behandlingsId, String eier);
    void oppdaterSoknadsdata(SoknadUnderArbeid soknadUnderArbeid, String eier) throws SamtidigSoknadUnderArbeidOppdateringException;
    void oppdaterInnsendingStatus(SoknadUnderArbeid soknadUnderArbeid, String eier);
    void slettSoknad(SoknadUnderArbeid soknadUnderArbeid, String eier);
}
