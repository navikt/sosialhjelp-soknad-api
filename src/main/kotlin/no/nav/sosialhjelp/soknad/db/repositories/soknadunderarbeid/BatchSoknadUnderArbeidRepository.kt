package no.nav.sosialhjelp.soknad.db.repositories.soknadunderarbeid

import java.util.Optional

interface BatchSoknadUnderArbeidRepository {
    fun hentSoknadUnderArbeidIdFromBehandlingsIdOptional(behandlingsId: String?): Optional<Long>
    fun hentGamleSoknadUnderArbeidForBatch(): List<Long>
    fun slettSoknad(soknadUnderArbeidId: Long?)
    fun hentForeldedeEttersendelser(): List<SoknadUnderArbeid>
}
