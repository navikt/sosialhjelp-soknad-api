package no.nav.sosialhjelp.soknad.business.db.repositories.soknadunderarbeid;

import no.nav.sosialhjelp.soknad.common.exceptions.SamtidigOppdateringException;
import no.nav.sosialhjelp.soknad.domain.SoknadUnderArbeid;

import java.util.Optional;

public interface SoknadUnderArbeidRepository {

    Long opprettSoknad(SoknadUnderArbeid soknadUnderArbeid, String eier);
    Optional<SoknadUnderArbeid> hentSoknad(Long soknadId, String eier);
    SoknadUnderArbeid hentSoknad(String behandlingsId, String eier);
    Optional<SoknadUnderArbeid> hentSoknadOptional(String behandlingsId, String eier);
    Optional<SoknadUnderArbeid> hentEttersendingMedTilknyttetBehandlingsId(String behandlingsId, String eier);
    void oppdaterSoknadsdata(SoknadUnderArbeid soknadUnderArbeid, String eier) throws SamtidigOppdateringException;
    void oppdaterInnsendingStatus(SoknadUnderArbeid soknadUnderArbeid, String eier);
    void slettSoknad(SoknadUnderArbeid soknadUnderArbeid, String eier);
}
