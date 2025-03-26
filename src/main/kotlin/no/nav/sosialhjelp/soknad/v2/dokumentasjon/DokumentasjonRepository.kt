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

fun DokumentasjonRepository.removeDokumentFromDokumentasjon(
    soknadId: UUID,
    dokumentId: UUID,
): Dokumentasjon? =
    findDokumentasjonForDokumentOrNull(soknadId, dokumentId)
        ?.removeDokument(dokumentId)
        ?.updateDokumentasjon()
        ?.let { dokumentasjon -> save(dokumentasjon) }

private fun DokumentasjonRepository.findDokumentasjonForDokument(
    soknadId: UUID,
    dokumentId: UUID,
): Dokumentasjon =
    findAllBySoknadId(soknadId).find { dokumentasjon -> dokumentasjon.hasDokument(dokumentId) }
        ?: error("Dokument finnes ikke på noe Dokumentasjon")

private fun DokumentasjonRepository.findDokumentasjonForDokumentOrNull(
    soknadId: UUID,
    dokumentId: UUID,
): Dokumentasjon? = findAllBySoknadId(soknadId).find { dokumentasjon -> dokumentasjon.hasDokument(dokumentId) }

private fun Dokumentasjon.hasDokument(dokumentId: UUID): Boolean = dokumenter.map { it.dokumentId }.contains(dokumentId)

private fun Dokumentasjon.removeDokument(dokumentId: UUID): Dokumentasjon =
    dokumenter
        .find { it.dokumentId == dokumentId }
        ?.let { dokument -> copy(dokumenter = dokumenter.minus(dokument)) }
        ?: error("Dokument finnes ikke på Dokumentasjon")

private fun Dokumentasjon.updateDokumentasjon(): Dokumentasjon {
    return when {
        status == DokumentasjonStatus.LEVERT_TIDLIGERE -> clearDokumenter()
        dokumenter.isEmpty() -> copy(status = DokumentasjonStatus.FORVENTET)
        else -> copy(status = DokumentasjonStatus.LASTET_OPP)
    }
}

private fun Dokumentasjon.clearDokumenter(): Dokumentasjon = copy(dokumenter = emptySet())
