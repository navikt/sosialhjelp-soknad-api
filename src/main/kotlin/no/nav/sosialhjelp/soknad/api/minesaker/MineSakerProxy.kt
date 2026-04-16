package no.nav.sosialhjelp.soknad.api.minesaker

import no.nav.sosialhjelp.soknad.api.minesaker.dto.InnsendtSoknadDto
import no.nav.sosialhjelp.soknad.v2.metadata.SoknadMetadataServiceImpl
import no.nav.sosialhjelp.soknad.v2.metadata.SoknadStatus
import org.springframework.stereotype.Component

@Component
class MineSakerProxy(private val metadataService: SoknadMetadataServiceImpl) {
    fun hentInnsendteSoknader(personId: String): List<InnsendtSoknadDto> {
        return metadataService.getAllMetadataForPerson(personId)
            .filter { it.status == SoknadStatus.SENDT || it.status == SoknadStatus.MOTTATT_FSL || it.status == SoknadStatus.MANUELT_KVITTERT_UT }
            .map {
                InnsendtSoknadDto(
                    navn = TEMA_NAVN,
                    kode = TEMA_KODE_KOM,
                    sistEndret = it.tidspunkt.sendtInn?.toString() ?: "",
                )
            }
    }

    companion object {
        private const val TEMA_NAVN = "Økonomisk sosialhjelp"
        private const val TEMA_KODE_KOM = "KOM"
    }
}
