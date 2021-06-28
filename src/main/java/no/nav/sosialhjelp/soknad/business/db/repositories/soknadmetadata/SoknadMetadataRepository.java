package no.nav.sosialhjelp.soknad.business.db.repositories.soknadmetadata;

import no.nav.sosialhjelp.soknad.business.domain.SoknadMetadata;

import java.time.LocalDateTime;
import java.util.List;

public interface SoknadMetadataRepository {

    Long hentNesteId();
    void opprett(SoknadMetadata metadata);
    void oppdater(SoknadMetadata metadata);
    SoknadMetadata hent(String behandlingsId);
    List<SoknadMetadata> hentBehandlingskjede(String behandlingsId);
    int hentAntallInnsendteSoknaderEtterTidspunkt(String fnr, LocalDateTime tidspunkt);
    List<SoknadMetadata> hentSvarUtInnsendteSoknaderForBruker(String fnr);
    List<SoknadMetadata> hentAlleInnsendteSoknaderForBruker(String fnr);
    List<SoknadMetadata> hentPabegynteSoknaderForBruker(String fnr);
    List<SoknadMetadata> hentPabegynteSoknaderForBruker(String fnr, boolean lestDittNav);
    List<SoknadMetadata> hentInnsendteSoknaderForBrukerEtterTidspunkt(String fnr, LocalDateTime after);
    void oppdaterLestDittNav(SoknadMetadata soknadMetadata, String fnr);
}
