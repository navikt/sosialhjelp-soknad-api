package no.nav.sosialhjelp.soknad.nymodell.service

import no.nav.sosialhjelp.soknad.nymodell.domene.adresse.AdresseObject
import no.nav.sosialhjelp.soknad.nymodell.domene.adresse.AdresseValg
import no.nav.sosialhjelp.soknad.nymodell.domene.brukerdata.Brukerdata
import no.nav.sosialhjelp.soknad.nymodell.domene.brukerdata.BrukerdataRepository
import no.nav.sosialhjelp.soknad.nymodell.domene.brukerdata.GenerelleDataKey
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Service
import java.util.*

@Service
class BrukerdataService(
    private val brukerdataRepository: BrukerdataRepository
) {

    fun hentOppholdsadresse(soknadId: UUID): AdresseObject? {
        return brukerdataRepository.findByIdOrNull(soknadId)?.oppholdsadresse
    }

    fun updateAdresseValg(soknadId: UUID, adresseValg: AdresseValg) {

    }

    fun updateOppholdsadresse(soknadId: UUID, adresseObject: AdresseObject) {
        val brukerdata = brukerdataRepository.findByIdOrNull(soknadId)
            ?: Brukerdata(
                soknadId = soknadId,
                oppholdsadresse = adresseObject
            )
        brukerdataRepository.save(brukerdata)
    }




    fun updateKommentarArbeidsforhold(soknadId: UUID, kommentar: String): String {
        val key = GenerelleDataKey.KOMMENTAR_ARBEIDSFORHOLD

        val brukerdata = brukerdataRepository.findByIdOrNull(soknadId) ?: Brukerdata(soknadId)
        val oppdatertBrukerdata = brukerdata.let {
            it.keyValueStore.update(key, kommentar)
            brukerdataRepository.save(it)
        }

        return oppdatertBrukerdata.keyValueStore.getValue(key)
            ?: throw RuntimeException("Lagring av kommentar til arbeid feilet")
    }

    fun hentKommentarArbeidsforhold(soknadId: UUID): String? {
        return brukerdataRepository.findByIdOrNull(soknadId)?.let {
            it.keyValueStore.getValue(GenerelleDataKey.KOMMENTAR_ARBEIDSFORHOLD)
        }
    }
}
