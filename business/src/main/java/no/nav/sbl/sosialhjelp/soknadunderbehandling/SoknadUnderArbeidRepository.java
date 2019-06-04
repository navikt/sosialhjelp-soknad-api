package no.nav.sbl.sosialhjelp.soknadunderbehandling;

import no.nav.sbl.sosialhjelp.SamtidigOppdateringException;
import no.nav.sbl.sosialhjelp.domain.SoknadUnderArbeid;

import java.util.List;
import java.util.Optional;

public interface SoknadUnderArbeidRepository {

    Long opprettSoknad(SoknadUnderArbeid soknadUnderArbeid, String eier);
    Optional<SoknadUnderArbeid> hentSoknad(Long soknadId, String eier);
    Optional<SoknadUnderArbeid> hentSoknad(String behandlingsId, String eier);
    Optional<SoknadUnderArbeid> hentEttersendingMedTilknyttetBehandlingsId(String behandlingsId, String eier);
    List<SoknadUnderArbeid> hentForeldedeEttersendelser();
    void oppdaterSoknadsdata(SoknadUnderArbeid soknadUnderArbeid, String eier) throws SamtidigOppdateringException;
    void oppdaterInnsendingStatus(SoknadUnderArbeid soknadUnderArbeid, String eier);
    void slettSoknad(SoknadUnderArbeid soknadUnderArbeid, String eier);
}
