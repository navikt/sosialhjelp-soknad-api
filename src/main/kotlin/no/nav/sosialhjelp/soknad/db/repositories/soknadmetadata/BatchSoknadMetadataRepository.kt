package no.nav.sosialhjelp.soknad.db.repositories.soknadmetadata

import no.nav.sosialhjelp.soknad.domain.SoknadMetadata
import java.util.Optional

interface BatchSoknadMetadataRepository {
    fun hentForBatch(antallDagerGammel: Int): Optional<SoknadMetadata>
    fun hentEldreEnn(antallDagerGammel: Int): Optional<SoknadMetadata>
    fun leggTilbakeBatch(id: Long)
    fun slettSoknadMetaData(behandlingsId: String)
}
