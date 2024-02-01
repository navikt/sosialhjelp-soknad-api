package no.nav.sosialhjelp.soknad.v2.brukerdata

import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDate
import java.util.*

@Service
@Transactional
class BrukerdataService(
    private val brukerdataRepository: BrukerdataRepository
) {
    @Transactional(readOnly = true)
    fun getBrukerdata(soknadId: UUID) = brukerdataRepository.findByIdOrNull(soknadId)
    @Transactional(readOnly = true)
    fun getKontoinformasjon(soknadId: UUID) = brukerdataRepository.findByIdOrNull(soknadId)?.kontoInformasjon
    @Transactional
    fun getTelefonnummer(soknadId: UUID) = brukerdataRepository.findByIdOrNull(soknadId)?.telefonnummer

    fun updateBegrunnelse(soknadId: UUID, begrunnelse: Begrunnelse): Brukerdata {
        return findOrCreateBrukerdata(soknadId)
            .run {
                this.begrunnelse = begrunnelse
                brukerdataRepository.save(this)
            }
    }

    fun updateKontoinformasjon(soknadId: UUID, kontoInformasjonBruker: KontoInformasjonBruker): Brukerdata {
        return findOrCreateBrukerdata(soknadId)
            .run {
                kontoInformasjon = kontoInformasjonBruker
                brukerdataRepository.save(this)
            }
    }

    fun updateTelefonnummer(soknadId: UUID, telefonnummer: String): Brukerdata {
        return findOrCreateBrukerdata(soknadId)
            .run {
                this.telefonnummer = telefonnummer
                brukerdataRepository.save(this)
            }
    }

    fun updateKommentarArbeidsforhold(soknadId: UUID, kommentarArbeidsforhold: String): Brukerdata {
        return findOrCreateBrukerdata(soknadId)
            .run {
                this.kommentarArbeidsforhold = kommentarArbeidsforhold
                brukerdataRepository.save(this)
            }
    }

    fun updateSamtykke(soknadId: UUID, samtykkeType: SamtykkeType, verdi: Boolean): Brukerdata {
        val brukerdata = findOrCreateBrukerdata(soknadId)

        brukerdata.samtykker.find { it.type == samtykkeType }
            ?.let {
                if (it.verdi != verdi) {
                    it.verdi = verdi
                    it.dato = LocalDate.now()
                }
            }
            ?: throw IllegalStateException("SamtykkeType $samtykkeType finnes ikke.")

        return brukerdata
    }

    fun updateBeskrivelseAvAnnet(soknadId: UUID, beskrivelseAvAnnet: BeskrivelseAvAnnet): Brukerdata {
        return findOrCreateBrukerdata(soknadId)
            .run {
                this.beskrivelseAvAnnet = beskrivelseAvAnnet
                brukerdataRepository.save(this)
            }
    }

    private fun findOrCreateBrukerdata(soknadId: UUID): Brukerdata {
        return brukerdataRepository.findByIdOrNull(soknadId) ?: Brukerdata(soknadId)
    }
}
