package no.nav.sosialhjelp.soknad.v2.dokumentasjon

import no.nav.sosialhjelp.soknad.v2.config.repository.DomainRoot
import no.nav.sosialhjelp.soknad.v2.config.repository.UpsertRepository
import no.nav.sosialhjelp.soknad.v2.okonomi.OpplysningType
import org.springframework.data.annotation.Id
import org.springframework.data.jdbc.repository.query.Modifying
import org.springframework.data.jdbc.repository.query.Query
import org.springframework.data.relational.core.mapping.Table
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

fun DokumentasjonRepository.removeDokumentFromDokumentasjon(
    soknadId: UUID,
    dokumentId: UUID,
): Dokumentasjon =
    findDokumentasjonForDokument(soknadId, dokumentId)
        .removeDokument(dokumentId)
        .let { dokumentasjon -> save(dokumentasjon) }

private fun DokumentasjonRepository.findDokumentasjonForDokument(
    soknadId: UUID,
    dokumentId: UUID,
): Dokumentasjon =
    findAllBySoknadId(soknadId).find { dokumentasjon -> dokumentasjon.hasDokument(dokumentId) }
        ?: error("Dokument finnes ikke på noe Dokumentasjon")

private fun Dokumentasjon.hasDokument(dokumentId: UUID): Boolean = dokumenter.map { it.dokumentId }.contains(dokumentId)

private fun Dokumentasjon.removeDokument(dokumentId: UUID): Dokumentasjon =
    dokumenter
        .find { it.dokumentId == dokumentId }
        ?.let { dokument -> copy(dokumenter = dokumenter.minus(dokument)) }
        ?: error("Dokument finnes ikke på Dokumentasjon")

@Table
data class Dokumentasjon(
    @Id val id: UUID = UUID.randomUUID(),
    val soknadId: UUID,
    val type: OpplysningType,
    val status: DokumentasjonStatus = DokumentasjonStatus.FORVENTET,
    val dokumenter: Set<Dokument> = emptySet(),
) : DomainRoot {
    override fun getDbId() = id
}

data class Dokument(
    val dokumentId: UUID,
    val filnavn: String,
    val sha512: String,
)

enum class DokumentasjonStatus {
    LASTET_OPP,
    FORVENTET,
    LEVERT_TIDLIGERE,
}

// TODO PS: Denne skal opprettes som forventet dokumentasjon i det en søknad startes
enum class AnnenDokumentasjonType(
    override val dokumentasjonForventet: Boolean?,
) : OpplysningType {
    SKATTEMELDING(dokumentasjonForventet = true),
    SAMVARSAVTALE(dokumentasjonForventet = true),
    OPPHOLDSTILLATELSE(dokumentasjonForventet = true),
    HUSLEIEKONTRAKT(dokumentasjonForventet = true),
    HUSLEIEKONTRAKT_KOMMUNAL(dokumentasjonForventet = true),
    BEHOV(dokumentasjonForventet = true),
    BARNEBIDRAG(dokumentasjonForventet = true),
    ;

    override val group: String get() = "Generell Dokumentasjon"
}
