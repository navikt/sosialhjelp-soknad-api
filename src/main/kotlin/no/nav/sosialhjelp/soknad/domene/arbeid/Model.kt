package no.nav.sosialhjelp.soknad.domene.arbeid

import no.nav.sosialhjelp.soknad.domene.soknad.CommonSoknadModel
import no.nav.sosialhjelp.soknad.domene.soknad.Stillingstype
import org.springframework.data.annotation.Id
import org.springframework.data.relational.core.mapping.MappedCollection
import java.util.*

data class Arbeid (
    @Id val soknadId: UUID,
    var kommentarArbeid: String? = null,
    @MappedCollection(idColumn = "SOKNAD_ID")
    val arbeidsforhold: Set<Arbeidsforhold> = emptySet()
): CommonSoknadModel {
    override val id: UUID
        get() = soknadId
}

data class Arbeidsforhold (
    val soknadId: UUID,
    val orgnummer: String? = null,
    val arbeidsgivernavn: String,
    val fraOgMed: String? = null,
    val tilOgMed: String? = null,
    val stillingsprosent: Int? = null,
    val stillingstype: Stillingstype
)