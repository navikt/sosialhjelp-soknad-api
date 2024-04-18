package no.nav.sosialhjelp.soknad.db.repositories.soknadunderarbeid

interface BatchSoknadUnderArbeidRepository {
    fun hentSoknadUnderArbeid(behandlingsId: String): SoknadUnderArbeid?

    fun hentSoknadUnderArbeid(soknadUnderArbeidId: Long): SoknadUnderArbeid?

    fun hentGamleSoknadUnderArbeidForBatch(): List<Long>

    fun slettSoknad(soknadUnderArbeidId: Long?)

    fun hentForeldedeEttersendelser(): List<SoknadUnderArbeid>
}
