package no.nav.sosialhjelp.soknad.v2.kontakt

import no.nav.sosialhjelp.soknad.v2.config.repository.DomainRoot
import no.nav.sosialhjelp.soknad.v2.config.repository.UpsertRepository
import org.springframework.data.annotation.Id
import org.springframework.data.relational.core.mapping.Column
import org.springframework.data.relational.core.mapping.Embedded
import org.springframework.data.relational.core.mapping.Table
import org.springframework.data.repository.ListCrudRepository
import org.springframework.stereotype.Repository
import java.util.UUID

@Repository
interface KontaktRepository : UpsertRepository<Kontakt>, ListCrudRepository<Kontakt, UUID>

@Table
data class Kontakt(
    @Id val soknadId: UUID,
    @Embedded.Empty
    val telefonnummer: Telefonnummer = Telefonnummer(),
    @Embedded.Empty
    val adresser: Adresser = Adresser(),
    val mottaker: NavEnhet = NavEnhet(),
) : DomainRoot {
    override fun getDbId() = soknadId
}

data class Telefonnummer(
    @Column("telefon_register")
    val fraRegister: String? = null,
    @Column("telefon_bruker")
    val fraBruker: String? = null,
)

data class Adresser(
    @Column("folkeregistrert_adresse")
    val folkeregistrert: Adresse? = null,
    @Column("midlertidig_adresse")
    val midlertidig: Adresse? = null,
    @Column("bruker_adresse")
    val fraBruker: Adresse? = null,
    val adressevalg: AdresseValg? = null,
) {
    fun getOppholdsadresse(): Adresse {
        return when (adressevalg) {
            AdresseValg.FOLKEREGISTRERT -> folkeregistrert ?: valgtAdresseNullError(AdresseValg.FOLKEREGISTRERT)
            AdresseValg.MIDLERTIDIG -> midlertidig ?: valgtAdresseNullError(AdresseValg.MIDLERTIDIG)
            AdresseValg.SOKNAD -> fraBruker ?: valgtAdresseNullError(AdresseValg.SOKNAD)
            else -> throw IllegalStateException("AdresseValg ikke satt eller ukjent adressetype: $adressevalg")
        }
    }

    private fun valgtAdresseNullError(valgtAdresse: AdresseValg?): Nothing {
        throw IllegalStateException("Adressevalg er $valgtAdresse, men adresse-objektet er null")
    }
}

enum class AdresseValg {
    FOLKEREGISTRERT,
    MIDLERTIDIG,
    SOKNAD,
}

data class NavEnhet(
    val enhetsnavn: String? = null,
    val enhetsnummer: String? = null,
    val kommunenummer: String? = null,
    val orgnummer: String? = null,
    val kommunenavn: String? = null,
)
