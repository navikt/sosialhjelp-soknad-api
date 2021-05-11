package no.nav.sosialhjelp.soknad.business.soknadunderbehandling;

import no.nav.sosialhjelp.soknad.business.exceptions.SamtidigOppdateringException;
import no.nav.sosialhjelp.soknad.domain.SoknadUnderArbeid;

import java.util.List;
import java.util.Optional;

public interface SoknadUnderArbeidRepository {

    Long opprettSoknad(SoknadUnderArbeid soknadUnderArbeid, String eier);
    Optional<SoknadUnderArbeid> hentSoknad(Long soknadId, String eier);
    SoknadUnderArbeid hentSoknad(String behandlingsId, String eier);
    Optional<SoknadUnderArbeid> hentSoknadOptional(String behandlingsId, String eier);
    Optional<SoknadUnderArbeid> hentEttersendingMedTilknyttetBehandlingsId(String behandlingsId, String eier);
    List<SoknadUnderArbeid> hentAlleSoknaderUnderArbeidSiste15Dager();
    void oppdaterSoknadsdata(SoknadUnderArbeid soknadUnderArbeid, String eier) throws SamtidigOppdateringException;
    void oppdaterInnsendingStatus(SoknadUnderArbeid soknadUnderArbeid, String eier);
    void slettSoknad(SoknadUnderArbeid soknadUnderArbeid, String eier);
}
