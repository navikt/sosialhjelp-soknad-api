package no.nav.sosialhjelp.soknad.v2.dokumentasjon

import no.nav.sosialhjelp.soknad.v2.config.repository.DomainRoot
import no.nav.sosialhjelp.soknad.v2.config.repository.UpsertRepository
import no.nav.sosialhjelp.soknad.v2.okonomi.OkonomiType
import org.springframework.data.annotation.Id
import org.springframework.data.relational.core.mapping.Table
import org.springframework.data.repository.ListCrudRepository
import org.springframework.stereotype.Repository
import java.util.UUID

@Repository
interface DokumentasjonRepository : UpsertRepository<Dokumentasjon>, ListCrudRepository<Dokumentasjon, UUID> {
    fun findAllBySoknadId(soknadId: UUID): List<Dokumentasjon>

    fun findBySoknadIdAndType(
        soknadId: UUID,
        type: OkonomiType,
    ): Dokumentasjon?
}

fun DokumentasjonRepository.removeDokumentFromDokumentasjon(
    soknadId: UUID,
    dokumentId: UUID,
): Dokumentasjon {
    return findDokumentasjonForDokument(soknadId, dokumentId)
        .removeDokument(dokumentId)
        .let { dokumentasjon -> save(dokumentasjon) }
}

private fun DokumentasjonRepository.findDokumentasjonForDokument(
    soknadId: UUID,
    dokumentId: UUID,
): Dokumentasjon {
    return findAllBySoknadId(soknadId).find { dokumentasjon -> dokumentasjon.hasDokument(dokumentId) }
        ?: error("Dokument finnes ikke på noe Dokumentasjon")
}

private fun Dokumentasjon.hasDokument(dokumentId: UUID): Boolean {
    return dokumenter.map { it.dokumentId }.contains(dokumentId)
}

private fun Dokumentasjon.removeDokument(dokumentId: UUID): Dokumentasjon {
    return dokumenter
        .find { it.dokumentId == dokumentId }
        ?.let { dokument -> copy(dokumenter = dokumenter.minus(dokument)) }
        ?: error("Dokument finnes ikke på Dokumentasjon")
}

@Table
data class Dokumentasjon(
    @Id val id: UUID = UUID.randomUUID(),
    val soknadId: UUID,
    val type: OkonomiType,
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
enum class AnnenDokumentasjonType(override val dokumentasjonForventet: Boolean) : OkonomiType {
    SKATTEMELDING(dokumentasjonForventet = true),
    ;

    override val group: String get() = "Generell Dokumentasjon"
}
