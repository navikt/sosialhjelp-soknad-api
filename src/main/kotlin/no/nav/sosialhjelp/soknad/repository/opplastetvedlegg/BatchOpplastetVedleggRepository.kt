package no.nav.sosialhjelp.soknad.repository.opplastetvedlegg

interface BatchOpplastetVedleggRepository {
    fun slettAlleVedleggForSoknad(soknadId: Long)
}
