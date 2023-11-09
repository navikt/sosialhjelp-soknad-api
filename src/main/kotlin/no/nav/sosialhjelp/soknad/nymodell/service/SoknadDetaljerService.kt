package no.nav.sosialhjelp.soknad.nymodell.service

import no.nav.sosialhjelp.soknad.nymodell.controller.dto.BegrunnelseDto
import no.nav.sosialhjelp.soknad.nymodell.controller.dto.NySoknadDto
import no.nav.sosialhjelp.soknad.nymodell.controller.dto.SoknadDto
import org.springframework.stereotype.Service
import java.util.*

@Service
class SoknadDetaljerService {
    fun opprettNySoknad(): NySoknadDto {
        TODO("Not yet implemented")
    }

    fun hentSoknad(soknadId: UUID): SoknadDto? {
        TODO("Not yet implemented")
    }

    fun hentBegrunnelse(soknadId: UUID): BegrunnelseDto {
        TODO("Not yet implemented")
    }

    fun updateBegrunnelse(soknadId: UUID, begrunnelseDto: BegrunnelseDto) {
        TODO("Not yet implemented")
    }
}