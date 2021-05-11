package no.nav.sosialhjelp.soknad.business.db.soknadmetadata;

import no.nav.sosialhjelp.soknad.business.domain.SoknadMetadata;

import java.time.LocalDateTime;
import java.util.List;

public interface SoknadMetadataRepository {

    Long hentNesteId();

    void opprett(SoknadMetadata metadata);

    void oppdater(SoknadMetadata metadata);

    SoknadMetadata hent(String behandlingsId);

//    Optional<SoknadMetadata> hentForBatch(int antallDagerGammel);
//
//    Optional<SoknadMetadata> hentEldreEnn(int antallDagerGammel);
//
//    void leggTilbakeBatch(Long id);

    List<SoknadMetadata> hentBehandlingskjede(String behandlingsId);

    int hentAntallInnsendteSoknaderEtterTidspunkt(String fnr, LocalDateTime tidspunkt);

    List<SoknadMetadata> hentSvarUtInnsendteSoknaderForBruker(String fnr);

    List<SoknadMetadata> hentAlleInnsendteSoknaderForBruker(String fnr);

    List<SoknadMetadata> hentPabegynteSoknaderForBruker(String fnr);

    List<SoknadMetadata> hentInnsendteSoknaderForBrukerEtterTidspunkt(String fnr, LocalDateTime after);

//    void slettSoknadMetaData(String behandlingsId, String eier);
}
