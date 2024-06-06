package no.nav.sosialhjelp.soknad.v2.dokumentasjon

import no.nav.sosialhjelp.soknad.v2.okonomi.OkonomiType
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.util.UUID

interface DokumentasjonForventningService {
    fun opprettForventetVedlegg(
        soknadId: UUID,
        okonomiType: OkonomiType,
    )

    fun fjernForventetVedlegg(
        soknadId: UUID,
        okonomiType: OkonomiType,
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
    override fun opprettForventetVedlegg(
        soknadId: UUID,
        okonomiType: OkonomiType,
    ) {
        dokumentasjonRepository.findAllBySoknadId(soknadId).find { it.type == okonomiType }
            ?: dokumentasjonRepository.save(Dokumentasjon(soknadId = soknadId, type = okonomiType))
    }

    override fun fjernForventetVedlegg(
        soknadId: UUID,
        okonomiType: OkonomiType,
    ) {
        dokumentasjonRepository.findAllBySoknadId(soknadId).find { it.type == okonomiType }
            ?.let { dokumentasjonRepository.deleteById(it.id) }
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
