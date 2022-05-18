package no.nav.sosialhjelp.soknad.db.repositories.soknadunderarbeid

interface BatchSoknadUnderArbeidRepository {
    fun hentSoknadUnderArbeid(behandlingsId: String): SoknadUnderArbeid?
    fun hentGamleSoknadUnderArbeidForBatch(): List<SoknadUnderArbeid>
    fun slettSoknad(soknadUnderArbeidId: Long?)
    fun hentForeldedeEttersendelser(): List<SoknadUnderArbeid>
}
