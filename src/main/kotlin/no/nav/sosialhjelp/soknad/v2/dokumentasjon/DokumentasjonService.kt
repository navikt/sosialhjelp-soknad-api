package no.nav.sosialhjelp.soknad.v2.dokumentasjon

import no.nav.sosialhjelp.soknad.v2.okonomi.OkonomiType
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.util.UUID

interface DokumentasjonForventningService {
    fun updateForventedeVedlegg(
        soknadId: UUID,
        okonomiType: OkonomiType,
        isPresent: Boolean,
    )
}

interface DokumentasjonStatusService {
    fun updateDokumentStatus(
        soknadId: UUID,
        okonomiType: OkonomiType,
        status: DokumentasjonStatus,
    )
}

@Service
@Transactional
class DokumentasjonService(
    private val dokumentasjonRepository: DokumentasjonRepository,
) : DokumentasjonForventningService, DokumentasjonStatusService {
    override fun updateForventedeVedlegg(
        soknadId: UUID,
        okonomiType: OkonomiType,
        isPresent: Boolean,
    ) {
        val dokumentasjon = dokumentasjonRepository.findAllBySoknadId(soknadId).firstOrNull { it.type == okonomiType }

        if (isPresent) {
            dokumentasjon ?: dokumentasjonRepository.save(Dokumentasjon(soknadId = soknadId, type = okonomiType))
        } else {
            dokumentasjon?.let { dokumentasjonRepository.deleteById(it.id) }
        }
    }

    override fun updateDokumentStatus(
        soknadId: UUID,
        okonomiType: OkonomiType,
        status: DokumentasjonStatus,
    ) {
        dokumentasjonRepository.findBySoknadIdAndType(soknadId, okonomiType)?.run {
            if (this.status != status) {
                copy(status = status)
                    .also { dokumentasjonRepository.save(it) }
            }
        } ?: error("Dokument finnes ikke")
    }
}
