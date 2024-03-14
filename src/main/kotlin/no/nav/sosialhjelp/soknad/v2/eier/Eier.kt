package no.nav.sosialhjelp.soknad.v2.eier

import no.nav.sosialhjelp.soknad.v2.config.repository.AggregateRoot
import no.nav.sosialhjelp.soknad.v2.config.repository.UpsertRepository
import no.nav.sosialhjelp.soknad.v2.navn.Navn
import org.springframework.data.annotation.Id
import org.springframework.data.jdbc.repository.query.Query
import org.springframework.data.relational.core.mapping.Column
import org.springframework.data.relational.core.mapping.Embedded
import org.springframework.data.repository.ListCrudRepository
import org.springframework.stereotype.Repository
import java.util.*

@Repository
interface EierRepository : UpsertRepository<Eier>, ListCrudRepository<Eier, UUID> {
    @Query("SELECT eier_person_id FROM soknad where id = :soknadId")
    fun getEierPersonId(soknadId: UUID): String
}

data class Eier(
    @Id
    override val soknadId: UUID,
    val statsborgerskap: String? = null,
    val nordiskBorger: Boolean? = null,
    @Embedded.Empty
    val navn: Navn,
    @Embedded.Nullable
    val kontonummer: Kontonummer? = null,
) : AggregateRoot

data class Kontonummer(
    val harIkkeKonto: Boolean? = null,
    @Column("konto_bruker")
    val fraBruker: String? = null,
    @Column("konto_register")
    val fraRegister: String? = null,
)
