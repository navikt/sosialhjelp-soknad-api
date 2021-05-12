package no.nav.sosialhjelp.soknad.business.db.soknadmetadata;


import no.nav.sosialhjelp.soknad.business.domain.SoknadMetadata;

import java.util.Optional;

public interface BatchSoknadMetadataRepository {

    Optional<SoknadMetadata> hentForBatch(int antallDagerGammel);
    Optional<SoknadMetadata> hentEldreEnn(int antallDagerGammel);
    void leggTilbakeBatch(Long id);
    void slettSoknadMetaData(String behandlingsId, String eier);
}
