package no.nav.sosialhjelp.soknad.v2.kontakt

import no.nav.sosialhjelp.soknad.v2.config.repository.SoknadBubble
import no.nav.sosialhjelp.soknad.v2.config.repository.UpsertRepository
import no.nav.sosialhjelp.soknad.v2.kontakt.adresse.Adresse
import org.springframework.data.annotation.Id
import org.springframework.data.relational.core.mapping.Column
import org.springframework.data.relational.core.mapping.Embedded
import org.springframework.data.repository.ListCrudRepository
import org.springframework.stereotype.Repository
import java.util.*

@Repository
interface KontaktRepository : UpsertRepository<Kontakt>, ListCrudRepository<Kontakt, UUID>

data class Kontakt(
    @Id
    override val soknadId: UUID,
    @Embedded.Empty
    val telefonnummer: Telefonnummer = Telefonnummer(),
    @Embedded.Empty
    val adresser: Adresser = Adresser(),
) : SoknadBubble

data class Telefonnummer(
    @Column("telefon_register")
    val register: String? = null,
    @Column("telefon_bruker")
    val bruker: String? = null,
)

data class Adresser(
    val folkeregistrertAdresse: Adresse? = null,
    val midlertidigAdresse: Adresse? = null,
    val brukerAdresse: Adresse? = null,
    val adressevalg: AdresseValg? = null
) {
    fun getOppholdsadresse(): Adresse {
        return when (adressevalg) {
            AdresseValg.FOLKEREGISTRERT -> folkeregistrertAdresse ?: valgtAdresseNullError(AdresseValg.FOLKEREGISTRERT)
            AdresseValg.MIDLERTIDIG -> midlertidigAdresse ?: valgtAdresseNullError(AdresseValg.MIDLERTIDIG)
            AdresseValg.SOKNAD -> brukerAdresse ?: valgtAdresseNullError(AdresseValg.SOKNAD)
            else -> throw IllegalStateException("AdresseValg ikke satt eller ukjent adressetype")
        }
    }
    private fun valgtAdresseNullError(valgtAdresse: AdresseValg?): Nothing {
        throw IllegalStateException("Adressevalg er $valgtAdresse, men adresse-objektet er null")
    }
}

enum class AdresseValg {
    FOLKEREGISTRERT,
    MIDLERTIDIG,
    SOKNAD;
}
