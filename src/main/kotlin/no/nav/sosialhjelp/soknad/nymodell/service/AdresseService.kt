package no.nav.sosialhjelp.soknad.nymodell.service

import no.nav.sosialhjelp.soknad.nymodell.domene.adresse.AdresseObject
import no.nav.sosialhjelp.soknad.nymodell.domene.adresse.AdresseValg
import no.nav.sosialhjelp.soknad.nymodell.domene.adresse.AdresseValg.FOLKEREGISTRERT
import no.nav.sosialhjelp.soknad.nymodell.domene.adresse.AdresseValg.MIDLERTIDIG
import no.nav.sosialhjelp.soknad.nymodell.domene.adresse.AdresseValg.SOKNAD
import no.nav.sosialhjelp.soknad.nymodell.domene.brukerdata.Brukerdata
import no.nav.sosialhjelp.soknad.nymodell.domene.brukerdata.BrukerdataRepository
import no.nav.sosialhjelp.soknad.nymodell.domene.soknad.SoknadRepository
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Service
import java.util.*

@Service
class AdresseService(
    private val brukerdataRepository: BrukerdataRepository,
    private val soknadRepository: SoknadRepository
) {
    fun hentAdresser(soknadId: UUID): Map<AdresseValg, AdresseObject?> {
        return soknadRepository.findById(soknadId).get().let {
            mapOf(
                FOLKEREGISTRERT to it.eier.kontaktInfo?.folkeregistrertAdresse,
                MIDLERTIDIG to it.eier.kontaktInfo?.midlertidigAdresse,
                SOKNAD to brukerdataRepository.findByIdOrNull(soknadId)?.oppholdsadresse
            )
        }
    }

    fun hentAdresseValg(soknadId: UUID): AdresseValg? = brukerdataRepository.findByIdOrNull(soknadId)?.valgtAdresse

    fun updateAdresseValg(soknadId: UUID, adresseValg: AdresseValg) {
        val brukerdata = (brukerdataRepository.findByIdOrNull(soknadId) ?: Brukerdata(soknadId))
        brukerdata.valgtAdresse = adresseValg
        brukerdataRepository.save(brukerdata)
    }

    fun updateOppholdsadresse(soknadId: UUID, adresseObject: AdresseObject) {
        val brukerdata = brukerdataRepository.findByIdOrNull(soknadId) ?: Brukerdata(soknadId)
        brukerdata.oppholdsadresse = adresseObject
        brukerdataRepository.save(brukerdata)
    }
}
