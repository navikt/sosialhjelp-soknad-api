package no.nav.sosialhjelp.soknad.v2.dokumentasjon

import no.nav.sosialhjelp.soknad.v2.config.repository.UpsertRepository
import no.nav.sosialhjelp.soknad.v2.okonomi.OpplysningType
import org.springframework.data.jdbc.repository.query.Modifying
import org.springframework.data.jdbc.repository.query.Query
import org.springframework.data.repository.ListCrudRepository
import org.springframework.stereotype.Repository
import java.util.UUID

@Repository
interface DokumentasjonRepository :
    UpsertRepository<Dokumentasjon>,
    ListCrudRepository<Dokumentasjon, UUID> {
    fun findAllBySoknadId(soknadId: UUID): List<Dokumentasjon>

    fun findBySoknadIdAndType(
        soknadId: UUID,
        type: OpplysningType,
    ): Dokumentasjon?

    @Modifying
    @Query("DELETE FROM dokumentasjon WHERE soknad_id = :soknadId")
    fun deleteAllBySoknadId(soknadId: UUID)
}
