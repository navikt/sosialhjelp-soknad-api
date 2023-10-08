package no.nav.sosialhjelp.soknad.service

//import no.nav.sosialhjelp.soknad.repository.VedleggRepository
import no.nav.sosialhjelp.soknad.vedlegg.dto.FilForVedlegg
import no.nav.sosialhjelp.soknad.vedlegg.fiks.MellomlagringService
import org.springframework.stereotype.Service
import org.springframework.web.multipart.MultipartFile

@Service
class VedleggService(
//    val vedleggRepository: VedleggRepository,
//    val filRepository: VedleggRepository,
    val mellomlagringService: MellomlagringService
) {

    fun lagreFilTilVedlegg (fil: FilForVedlegg) {



    }

}