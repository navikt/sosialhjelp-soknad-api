package no.nav.sosialhjelp.soknad.db.repositories.opplastetvedlegg

interface BatchOpplastetVedleggRepository {
    fun slettAlleVedleggForSoknad(soknadId: Long)
}
