package no.nav.sosialhjelp.soknad.v2.eier

import no.nav.sosialhjelp.soknad.v2.config.repository.SoknadBubble
import no.nav.sosialhjelp.soknad.v2.config.repository.UpsertRepository
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
    @Embedded.Nullable
    val navn: Navn,
    @Embedded.Empty
    val kontonummer: Kontonummer = Kontonummer(),
) : SoknadBubble

data class Navn(
    val fornavn: String,
    val mellomnavn: String? = null,
    val etternavn: String,
)

data class Kontonummer(
    val harIkkeKonto: Boolean? = null,
    @Column("konto_bruker")
    val bruker: String? = null,
    @Column("konto_register")
    val register: String? = null,
)
