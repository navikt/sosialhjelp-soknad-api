package no.nav.sosialhjelp.soknad.v2.adresse

import no.nav.sosialhjelp.soknad.v2.config.repository.DomainRoot
import no.nav.sosialhjelp.soknad.v2.config.repository.UpsertRepository
import no.nav.sosialhjelp.soknad.v2.kontakt.Adresse
import org.springframework.data.annotation.Id
import org.springframework.data.relational.core.mapping.Embedded
import org.springframework.data.relational.core.mapping.Table
import org.springframework.data.repository.ListCrudRepository
import org.springframework.stereotype.Repository
import java.util.UUID

@Repository
interface AdresseRepository : UpsertRepository<AdresserSoknad>, ListCrudRepository<AdresserSoknad, UUID>

@Table("ADRESSER")
data class AdresserSoknad(
    @Id override val soknadId: UUID,
    val midlertidigAdresse: Adresse? = null,
    val folkeregistrertAdresse: Adresse? = null,
    @Embedded(onEmpty = Embedded.OnEmpty.USE_NULL)
    var brukerInput: BrukerInputAdresse? = null,
) : DomainRoot {
    fun getOppholdsadresse(): Adresse {
        return brukerInput?.let {
            return when (it.valgtAdresse) {
                AdresseValg.FOLKEREGISTRERT -> folkeregistrertAdresse ?: valgtAdresseNullError(AdresseValg.FOLKEREGISTRERT)
                AdresseValg.MIDLERTIDIG -> midlertidigAdresse ?: valgtAdresseNullError(AdresseValg.MIDLERTIDIG)
                AdresseValg.SOKNAD -> it.brukerAdresse ?: valgtAdresseNullError(AdresseValg.SOKNAD)
            }
        } ?: throw IllegalStateException("AdresseValg finnes ikke for soknad.")
    }

    private fun valgtAdresseNullError(valgtAdresse: AdresseValg?): Nothing {
        throw IllegalStateException("Adressevalg er $valgtAdresse, men adresse-objektet er null")
    }
}

data class BrukerInputAdresse(
    val valgtAdresse: AdresseValg,
    val brukerAdresse: Adresse? = null,
)

enum class AdresseValg {
    FOLKEREGISTRERT,
    MIDLERTIDIG,
    SOKNAD,
}
