package no.nav.sosialhjelp.soknad.v2.vedlegg

import no.nav.sosialhjelp.soknad.v2.okonomi.OkonomiType
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.util.UUID

interface VedleggForventningService {
    fun updateForventedeVedlegg(
        soknadId: UUID,
        okonomiType: OkonomiType,
        isPresent: Boolean,
    )
}

@Service
@Transactional
class VedleggService(
    private val vedleggRepository: VedleggRepository,
) : VedleggForventningService {
    override fun updateForventedeVedlegg(
        soknadId: UUID,
        type: OkonomiType,
        isPresent: Boolean,
    ) {
        val vedlegg = vedleggRepository.findAllBySoknadId(soknadId).firstOrNull { it.type == type }

        if (isPresent) {
            vedlegg ?: vedleggRepository.save(Vedlegg(soknadId = soknadId, type = type))
        } else {
            vedlegg?.let { vedleggRepository.deleteById(it.id) }
        }
    }
}
