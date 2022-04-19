package no.nav.sosialhjelp.soknad.db.repositories.soknadmetadata

interface BatchSoknadMetadataRepository {
    fun hentForBatch(antallDagerGammel: Int): SoknadMetadata?
    fun hentEldreEnn(antallDagerGammel: Int): SoknadMetadata?
    fun leggTilbakeBatch(id: Long)
    fun slettSoknadMetaData(behandlingsId: String)
}
