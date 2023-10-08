package no.nav.sosialhjelp.soknad.repository.soknadmetadata

interface BatchSoknadMetadataRepository {
    fun hentForBatch(antallDagerGammel: Int): SoknadMetadata?
    fun hentEldreEnn(antallDagerGammel: Int): List<SoknadMetadata>
    fun leggTilbakeBatch(id: Long)
    fun slettSoknadMetaDataer(behandlingsIdList: List<String>)
}
