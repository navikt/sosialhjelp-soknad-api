package no.nav.sosialhjelp.soknad.v2.shadow

import java.time.LocalDateTime
import java.time.ZonedDateTime
import java.util.UUID
import no.nav.sbl.soknadsosialhjelp.soknad.personalia.JsonPersonalia
import no.nav.sosialhjelp.soknad.arbeid.domain.toV2Arbeidsforhold
import no.nav.sosialhjelp.soknad.personalia.adresse.adresseregister.HentAdresseService
import no.nav.sosialhjelp.soknad.personalia.person.domain.Person
import no.nav.sosialhjelp.soknad.v2.eier.Eier
import no.nav.sosialhjelp.soknad.v2.eier.EierRegisterService
import no.nav.sosialhjelp.soknad.v2.kontakt.KontaktRegisterService
import no.nav.sosialhjelp.soknad.v2.livssituasjon.LivssituasjonRegisterService
import no.nav.sosialhjelp.soknad.v2.navn.Navn
import no.nav.sosialhjelp.soknad.v2.register.handlers.person.toV2Adresse
import no.nav.sosialhjelp.soknad.v2.soknad.SoknadServiceImpl
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Propagation
import org.springframework.transaction.annotation.Transactional

@Service
@Transactional(propagation = Propagation.NESTED)
class SoknadV2AdapterService(
    private val soknadServiceImpl: SoknadServiceImpl,
    private val livssituasjonService: LivssituasjonRegisterService,
    private val kontaktService: KontaktRegisterService,
    private val hentAdresseService: HentAdresseService,
    private val eierService: EierRegisterService,
) : V2AdapterService {
    private val log = LoggerFactory.getLogger(this::class.java)

    override fun createSoknad(
        behandlingsId: String,
        opprettetDato: LocalDateTime,
        eierId: String,
    ) {
        log.info("NyModell: Oppretter ny soknad for $behandlingsId")

        kotlin.runCatching {
            soknadServiceImpl.createSoknad(
                soknadId = UUID.fromString(behandlingsId),
                opprettetDato = opprettetDato,
                eierId = eierId,
            )
        }
            .onFailure { log.warn("Ny modell: Feil ved oppretting av ny soknad i adapter", it) }
    }

    override fun addArbeidsforholdList(
        soknadId: String,
        arbeidsforhold: List<no.nav.sosialhjelp.soknad.arbeid.domain.Arbeidsforhold>?,
    ) {
        arbeidsforhold?.let {
            log.info("NyModell: Legger til arbeidsforhold for $soknadId")

            kotlin.runCatching {
                livssituasjonService.updateArbeidsforhold(
                    UUID.fromString(soknadId),
                    it.map { it.toV2Arbeidsforhold() },
                )
            }
                .onFailure { log.warn("Ny modell: Kunne ikke legge til arbeidsforhold", it) }
        }
    }

    override fun addAdresserRegister(
        soknadId: String,
        person: Person?,
    ) {
        log.info("NyModell: Legger til adresser for $soknadId")

        person?.let {
            kotlin.runCatching {
                kontaktService.saveAdresserRegister(
                    soknadId = UUID.fromString(soknadId),
                    folkeregistrert = it.bostedsadresse?.toV2Adresse(hentAdresseService),
                    midlertidig = it.oppholdsadresse?.toV2Adresse(),
                )
            }
                .onFailure { log.warn("NyModell: Legge til Adresser feilet for $soknadId", it) }
        }
    }

    override fun updateTelefonRegister(
        soknadId: String,
        telefonnummer: String?,
    ) {
        log.info("NyModell: Legger til Telefonnummer for $soknadId")

        telefonnummer?.let {
            kotlin.runCatching {
                kontaktService.updateTelefonRegister(UUID.fromString(soknadId), it)
            }
                .onFailure { log.warn("Kunne ikke legge til telefonnummer fra register", it) }
        }
    }

    override fun updateEier(
        soknadId: String,
        personalia: JsonPersonalia,
    ) {
        log.info("Ny modell: Legger til Eier")
        kotlin.runCatching {
            eierService.updateEier(personalia.toV2Eier(UUID.fromString(soknadId)))
        }
            .onFailure { log.warn("NyModell: Kunne ikke legge til ny Eier fra register", it) }
    }

    override fun setInnsendingstidspunkt(
        soknadId: String,
        innsendingsTidspunkt: String,
    ) {
        log.info("NyModell: Setter innsendingstidspunkt")

        kotlin.runCatching {
            val zonedDateTime = ZonedDateTime.parse(innsendingsTidspunkt)

            soknadServiceImpl.setInnsendingstidspunkt(
                UUID.fromString(soknadId),
                zonedDateTime.toLocalDateTime(),
            )
        }
            .onFailure { log.warn("NyModell: Kunne ikke sette innsendingstidspunkt", it) }
    }

    override fun slettSoknad(behandlingsId: String) {
        log.info("NyModell: Sletter SoknadV2")

        kotlin.runCatching {
            soknadServiceImpl.slettSoknad(UUID.fromString(behandlingsId))
        }
            .onFailure { log.warn("NyModell: Kunne ikke slette Soknad V2") }
    }
}

private fun JsonPersonalia.toV2Eier(soknadId: UUID): Eier {
    return Eier(
        soknadId = soknadId,
        navn =
            Navn(
                fornavn = this.navn.fornavn,
                mellomnavn = this.navn.mellomnavn,
                etternavn = this.navn.etternavn,
            ),
        statsborgerskap = this.statsborgerskap.verdi,
        nordiskBorger = this.nordiskBorger.verdi,
    )
}
