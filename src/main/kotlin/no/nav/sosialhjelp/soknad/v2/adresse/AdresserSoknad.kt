package no.nav.sosialhjelp.soknad.v2.adresse

import no.nav.sosialhjelp.soknad.v2.brukerdata.AdresseValg
import no.nav.sosialhjelp.soknad.v2.brukerdata.AdresseValg.FOLKEREGISTRERT
import no.nav.sosialhjelp.soknad.v2.brukerdata.AdresseValg.MIDLERTIDIG
import no.nav.sosialhjelp.soknad.v2.brukerdata.AdresseValg.SOKNAD
import no.nav.sosialhjelp.soknad.v2.config.repository.SoknadBubble
import no.nav.sosialhjelp.soknad.v2.config.repository.UpsertRepository
import org.springframework.data.annotation.Id
import org.springframework.data.relational.core.mapping.Embedded
import org.springframework.data.relational.core.mapping.Table
import org.springframework.data.repository.ListCrudRepository
import org.springframework.stereotype.Repository
import java.util.*

@Repository
interface AdresseRepository : UpsertRepository<AdresserSoknad>, ListCrudRepository<AdresserSoknad, UUID>

@Table("adresser")
data class AdresserSoknad(
    @Id override val soknadId: UUID,
    val midlertidigAdresse: Adresse?,
    val folkeregistrertAdresse: Adresse?,
    @Embedded(onEmpty = Embedded.OnEmpty.USE_NULL)
    var brukerInput: BrukerInputAdresse?
) : SoknadBubble {

    fun getOppholdsadresse(): Adresse {
        return brukerInput?.let {
            return when (it.valgtAdresse) {
                FOLKEREGISTRERT -> folkeregistrertAdresse ?: valgtAdresseNullError(FOLKEREGISTRERT)
                MIDLERTIDIG -> midlertidigAdresse ?: valgtAdresseNullError(MIDLERTIDIG)
                SOKNAD -> it.brukerAdresse ?: valgtAdresseNullError(SOKNAD)
            }
        } ?: throw IllegalStateException("AdresseValg finnes ikke for soknad.")
    }

    private fun valgtAdresseNullError(valgtAdresse: AdresseValg?): Nothing {
        throw IllegalStateException("Adressevalg er $valgtAdresse, men adresse-objektet er null")
    }
}

data class BrukerInputAdresse(
    val valgtAdresse: AdresseValg,
    val brukerAdresse: Adresse?
)
