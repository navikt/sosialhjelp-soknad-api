package no.nav.sosialhjelp.soknad.nymodell.service

import no.nav.sosialhjelp.soknad.nymodell.controller.dto.BegrunnelseDto
import no.nav.sosialhjelp.soknad.nymodell.controller.dto.NySoknadDto
import no.nav.sosialhjelp.soknad.nymodell.controller.dto.SoknadDto
import no.nav.sosialhjelp.soknad.nymodell.domene.adresse.AdresseObject
import no.nav.sosialhjelp.soknad.nymodell.domene.soknad.SoknadRepository
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Service
import java.util.*

@Service
class SoknadService(
    private val soknadRepository: SoknadRepository
) {

    fun hentFolkeregistrertAdresse(soknadId: UUID): AdresseObject? {
        return soknadRepository.findByIdOrNull(soknadId)?.let {
            it.eier.kontaktInfo?.folkeregistrertAdresse
        }
    }

    fun hentMidlertidigAdresse(soknadId: UUID): AdresseObject? {
        return soknadRepository.findByIdOrNull(soknadId)?.let {
            it.eier.kontaktInfo?.midlertidigAdresse
        }
    }


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