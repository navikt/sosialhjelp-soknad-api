package no.nav.sosialhjelp.soknad.v2.okonomi

import no.nav.sosialhjelp.soknad.v2.dokumentasjon.ForventetDokumentasjonService
import org.springframework.stereotype.Service
import java.util.UUID

interface UpdateOkonomiService {
    fun getForventetDokumentasjon(soknadId: UUID): ForventetDokumentasjonDto

    fun updateOkonomiskeOpplysninger(
        soknadId: UUID,
        detaljer: List<OkonomiDetalj>,
    ): ForventetDokumentasjonDto
}

@Service
class UpdateOkonomiServiceImpl(
    private val okonomiService: OkonomiService,
    private val forventetDokumentasjonService: ForventetDokumentasjonService,
) : UpdateOkonomiService {
    override fun getForventetDokumentasjon(soknadId: UUID): ForventetDokumentasjonDto {
        return forventetDokumentasjonService.getForventetDokumentasjon(soknadId)
    }

    override fun updateOkonomiskeOpplysninger(
        soknadId: UUID,
        detaljer: List<OkonomiDetalj>,
    ): ForventetDokumentasjonDto {
        TODO("Not yet implemented")
    }
}
