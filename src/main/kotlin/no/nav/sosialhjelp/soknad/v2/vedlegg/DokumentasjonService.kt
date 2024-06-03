package no.nav.sosialhjelp.soknad.v2.vedlegg

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
    )
}

@Service
@Transactional
class DokumentasjonService(
    private val dokumentasjonRepository: DokumentasjonRepository,
) : DokumentasjonForventningService {
    override fun updateForventedeVedlegg(
        soknadId: UUID,
        type: OkonomiType,
        isPresent: Boolean,
    ) {
        val vedlegg = dokumentasjonRepository.findAllBySoknadId(soknadId).firstOrNull { it.type == type }

        if (isPresent) {
            vedlegg ?: dokumentasjonRepository.save(Dokumentasjon(soknadId = soknadId, type = type))
        } else {
            vedlegg?.let { dokumentasjonRepository.deleteById(it.id) }
        }
    }
}
