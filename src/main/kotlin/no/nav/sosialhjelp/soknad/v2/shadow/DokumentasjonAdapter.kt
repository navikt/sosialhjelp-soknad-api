package no.nav.sosialhjelp.soknad.v2.shadow

import no.nav.sosialhjelp.soknad.app.LoggingUtils.logger
import no.nav.sosialhjelp.soknad.okonomiskeopplysninger.dto.VedleggType
import no.nav.sosialhjelp.soknad.v2.dokumentasjon.Dokument
import no.nav.sosialhjelp.soknad.v2.dokumentasjon.DokumentasjonRepository
import no.nav.sosialhjelp.soknad.v2.dokumentasjon.DokumentasjonStatus
import org.springframework.stereotype.Component
import org.springframework.transaction.TransactionDefinition
import org.springframework.transaction.support.TransactionTemplate
import java.util.UUID

interface DokumentasjonAdapter {
    fun saveDokumentMetadata(
        behandlingsId: String,
        vedleggTypeString: String,
        dokumentId: String,
        filnavn: String,
        sha512: String,
    )

    fun deleteDokumentMetadata(
        behandlingsId: String,
        dokumentId: String,
    )
}

@Component
class SoknadV2DokumentasjonAdapter(
    private val dokumentasjonRepository: DokumentasjonRepository,
    private val transactionTemplate: TransactionTemplate,
) : DokumentasjonAdapter {
    override fun saveDokumentMetadata(
        behandlingsId: String,
        vedleggTypeString: String,
        dokumentId: String,
        filnavn: String,
        sha512: String,
    ) {
        runWithNestedTransaction {
            val okonomiType = (
                VedleggType[vedleggTypeString].okonomiType
                    ?: error("VedleggType $vedleggTypeString har ingen OkonomiType-mapping")
            )

            dokumentasjonRepository.findBySoknadIdAndType(UUID.fromString(behandlingsId), okonomiType)
                ?.run {
                    val dokument =
                        Dokument(
                            dokumentId = UUID.fromString(dokumentId),
                            filnavn = filnavn,
                            sha512 = sha512,
                        )
                    copy(status = DokumentasjonStatus.LASTET_OPP, dokumenter = dokumenter.plus(dokument))
                }
                ?.also { dokumentasjonRepository.save(it) }
                ?: error("Finnes ingen Dokumentasjon for soknad $behandlingsId og okonomi-type ${okonomiType.name}")
        }
            .onFailure { logger.warn("NyModell: Feil ved oppdatering av Dokument-metadata", it) }
    }

    override fun deleteDokumentMetadata(
        behandlingsId: String,
        dokumentId: String,
    ) {
        runWithNestedTransaction {
            val dokumentasjon =
                dokumentasjonRepository.findAllBySoknadId(soknadId = UUID.fromString(behandlingsId))
                    .find { dokumentasjon ->
                        dokumentasjon.dokumenter.map { it.dokumentId }.contains(UUID.fromString(dokumentId))
                    }
                    ?.run {
                        val dokument = dokumenter.find { it.dokumentId == UUID.fromString(dokumentId) }
                        copy(dokumenter = dokumenter.minus(dokument!!))
                    }
                    ?.run { if (dokumenter.isEmpty()) copy(status = DokumentasjonStatus.FORVENTET) else this }
                    ?.also { dokumentasjonRepository.save(it) }
                    ?: error("NyModell: Fant ikke Dokument")
        }
            .onFailure { logger.warn("NyModell: Feil ved sletting av Dokument", it) }
    }

    private fun runWithNestedTransaction(function: () -> Unit): Result<Unit> {
        return kotlin.runCatching {
            transactionTemplate.propagationBehavior = TransactionDefinition.PROPAGATION_NESTED
            transactionTemplate.execute { function.invoke() }
        }
    }

    companion object {
        private val logger by logger()
    }
}
