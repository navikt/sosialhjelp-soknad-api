package no.nav.sosialhjelp.soknad.repository.opplastetvedlegg

interface OpplastetVedleggRepository {
    fun hentVedlegg(uuid: String?, eier: String): OpplastetVedlegg?
    fun hentVedleggForSoknad(soknadId: Long, eier: String?): List<OpplastetVedlegg>
    fun opprettVedlegg(opplastetVedlegg: OpplastetVedlegg, eier: String): String
    fun slettVedlegg(uuid: String?, eier: String)
    fun slettAlleVedleggForSoknad(soknadId: Long, eier: String)
    fun hentSamletVedleggStorrelse(soknadId: Long, eier: String): Int
}
