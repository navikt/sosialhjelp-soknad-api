package no.nav.sosialhjelp.soknad.nymodell.service

//import no.nav.sosialhjelp.soknad.repository.VedleggRepository
import no.nav.sosialhjelp.soknad.nymodell.domene.vedlegg.VedleggRepository
import no.nav.sosialhjelp.soknad.vedlegg.dto.FilForVedlegg
import no.nav.sosialhjelp.soknad.vedlegg.fiks.MellomlagringService
import org.springframework.stereotype.Service

@Service
class VedleggService(
    private val vedleggRepository: VedleggRepository,
    private val filRepository: VedleggRepository,
    private val mellomlagringService: MellomlagringService
) {

    fun lagreFilTilVedlegg (fil: FilForVedlegg) {



    }

}