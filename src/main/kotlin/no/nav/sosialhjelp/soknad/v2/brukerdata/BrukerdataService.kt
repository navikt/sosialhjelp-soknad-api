package no.nav.sosialhjelp.soknad.v2.brukerdata

import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDate
import java.util.*

@Service
@Transactional
class BrukerdataService(
    private val brukerFormeltRepository: BrukerdataFormeltRepository,
    private val brukerPersonRepository: BrukerdataPersonRepository,
) {
    @Transactional(readOnly = true)
    fun getBrukerdataFormelt(soknadId: UUID) = brukerFormeltRepository.findByIdOrNull(soknadId)
    @Transactional(readOnly = true)
    fun getBrukerdataPersonlig(soknadId: UUID) = brukerPersonRepository.findByIdOrNull(soknadId)


    fun updateBegrunnelse(soknadId: UUID, begrunnelse: Begrunnelse): BrukerdataPerson {
        return findOrCreateBrukerdataPersonlig(soknadId)
            .run {
                this.begrunnelse = begrunnelse
                brukerPersonRepository.save(this)
            }
    }

    fun updateBosituasjon(soknadId: UUID, bosituasjon: Bosituasjon): BrukerdataPerson {
        return findOrCreateBrukerdataPersonlig(soknadId)
            .run {
                this.bosituasjon = bosituasjon
                brukerPersonRepository.save(this)
            }
    }

    fun updateKontoinformasjon(soknadId: UUID, kontoInformasjonBruker: KontoInformasjonBruker): KontoInformasjonBruker {
        return findOrCreateBrukerdataPersonlig(soknadId)
            .run {
                kontoInformasjon = kontoInformasjonBruker
                brukerPersonRepository.save(this)
                kontoInformasjonBruker
            }
    }

    fun updateTelefonnummer(soknadId: UUID, telefonnummer: String): BrukerdataPerson {
        return findOrCreateBrukerdataPersonlig(soknadId)
            .run {
                this.telefonnummer = telefonnummer
                brukerPersonRepository.save(this)
            }
    }

    fun updateKommentarArbeidsforhold(soknadId: UUID, kommentarArbeidsforhold: String): String {
        return findOrCreateBrukerdataFormelt(soknadId)
            .run {
                this.kommentarArbeidsforhold = kommentarArbeidsforhold
                brukerFormeltRepository.save(this)
                kommentarArbeidsforhold
            }
    }

    fun updateSamtykke(soknadId: UUID, samtykkeType: SamtykkeType, verdi: Boolean): BrukerdataFormelt {
        val brukerdata = findOrCreateBrukerdataFormelt(soknadId)

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

    fun updateBeskrivelseAvAnnet(soknadId: UUID, beskrivelseAvAnnet: BeskrivelseAvAnnet): BrukerdataFormelt {
        return findOrCreateBrukerdataFormelt(soknadId)
            .run {
                this.beskrivelseAvAnnet = beskrivelseAvAnnet
                brukerFormeltRepository.save(this)
            }
    }

    private fun findOrCreateBrukerdataFormelt(soknadId: UUID): BrukerdataFormelt {
        return brukerFormeltRepository.findByIdOrNull(soknadId)
            ?: brukerFormeltRepository.save(BrukerdataFormelt(soknadId))
    }

    private fun findOrCreateBrukerdataPersonlig(soknadId: UUID): BrukerdataPerson {
        return brukerPersonRepository.findByIdOrNull(soknadId)
            ?: brukerPersonRepository.save(BrukerdataPerson(soknadId))
    }

    fun getBegrunnelse(soknadId: UUID): Begrunnelse? = brukerPersonRepository.findByIdOrNull(soknadId)?.begrunnelse
}
