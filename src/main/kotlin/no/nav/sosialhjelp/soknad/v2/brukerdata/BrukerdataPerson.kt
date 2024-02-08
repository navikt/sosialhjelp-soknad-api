package no.nav.sosialhjelp.soknad.v2.brukerdata

import no.nav.sosialhjelp.soknad.v2.config.repository.SoknadBubble
import no.nav.sosialhjelp.soknad.v2.config.repository.UpsertRepository
import org.springframework.data.annotation.Id
import org.springframework.data.relational.core.mapping.Embedded
import org.springframework.data.repository.ListCrudRepository
import org.springframework.stereotype.Repository
import java.util.*

@Repository
interface BrukerdataPersonRepository : UpsertRepository<BrukerdataPerson>, ListCrudRepository<BrukerdataPerson, UUID>

data class BrukerdataPerson(
    @Id
    override val soknadId: UUID,
    var telefonnummer: String? = null,

    @Embedded.Nullable
    var begrunnelse: Begrunnelse? = null,

    @Embedded.Nullable
    var bosituasjon: Bosituasjon? = null,

    @Embedded.Nullable
    var kontoInformasjon: KontoInformasjonBruker? = null,
) : SoknadBubble

data class Begrunnelse(
    val hvorforSoke: String? = null,
    val hvaSokesOm: String? = null,
)

data class Bosituasjon(
    var botype: Botype?,
    var antallHusstand: Int?
)

enum class Botype {
    EIER,
    LEIER,
    KOMMUNAL,
    INGEN,
    INSTITUSJON,
    KRISESENTER,
    FENGSEL,
    VENNER,
    FORELDRE,
    FAMILIE,
    ANNET;
}

data class KontoInformasjonBruker(
    val kontonummer: String? = null,
    val harIkkeKonto: Boolean? = null,
)
