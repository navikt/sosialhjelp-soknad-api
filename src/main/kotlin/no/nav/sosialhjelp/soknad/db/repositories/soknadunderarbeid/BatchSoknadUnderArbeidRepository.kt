package no.nav.sosialhjelp.soknad.db.repositories.soknadunderarbeid

interface BatchSoknadUnderArbeidRepository {
    fun hentSoknadUnderArbeidIdFromBehandlingsId(behandlingsId: String?): Long?
    fun hentGamleSoknadUnderArbeidForBatch(): List<Long>
    fun slettSoknad(soknadUnderArbeidId: Long?)
    fun hentForeldedeEttersendelser(): List<SoknadUnderArbeid>
}
