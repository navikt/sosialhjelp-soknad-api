package no.nav.sosialhjelp.soknad.v2.shadow

import no.nav.sbl.soknadsosialhjelp.soknad.personalia.JsonPersonalia
import no.nav.sosialhjelp.soknad.arbeid.domain.toV2Arbeidsforhold
import no.nav.sosialhjelp.soknad.personalia.person.domain.Person
import no.nav.sosialhjelp.soknad.v2.shadow.adapter.V2KontaktAdapter
import no.nav.sosialhjelp.soknad.v2.shadow.adapter.V2SoknadAdapter
import no.nav.sosialhjelp.soknad.v2.soknad.SoknadService
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component
import java.time.LocalDateTime
import java.util.*

// TODO Fjerne ekstra abstraksjonslag med adaptere
@Component
class SoknadV2RegisterDataAdapter(
    private val v2SoknadAdapter: V2SoknadAdapter,
    private val v2KontaktAdapter: V2KontaktAdapter,
    private val soknadService: SoknadService,
) : RegisterDataAdapter {

    private val log = LoggerFactory.getLogger(this::class.java)

    override fun createSoknad(behandlingsId: String, opprettetDato: LocalDateTime, eierId: String) {
        log.info("NyModell: Oppretter ny soknad for $behandlingsId")

        kotlin.runCatching {
            v2SoknadAdapter.createNewSoknad(
                soknadId = UUID.fromString(behandlingsId),
                opprettetDato = opprettetDato,
                eierPersonId = eierId
            )
        }
            .onFailure { log.error("Ny modell: Feil ved oppretting av ny soknad i adapter", it) }
    }

    override fun addArbeidsforholdList(soknadId: String, arbeidsforhold: List<no.nav.sosialhjelp.soknad.arbeid.domain.Arbeidsforhold>?) {
        arbeidsforhold?.let {
            log.info("NyModell: Legger til arbeidsforhold for $soknadId")

            kotlin.runCatching {
                v2SoknadAdapter.saveArbeidsforhold(
                    UUID.fromString(soknadId),
                    it.map { it.toV2Arbeidsforhold() }
                )
            }
                .onFailure { log.error("Ny modell: Kunne ikke legge til arbeidsforhold", it) }
        }
    }

    override fun addAdresserRegister(soknadId: String, person: Person?) {
        log.info("NyModell: Legger til adresser for $soknadId")

        person?.let {
            kotlin.runCatching {
                v2KontaktAdapter.saveAdresser(
                    soknadId = UUID.fromString(soknadId),
                    bostedsadresse = it.bostedsadresse,
                    oppholdsadresse = it.oppholdsadresse,
                )
            }
                .onFailure { log.error("NyModell: Legge til Adresser feilet for $soknadId", it) }
        }
    }

    override fun addTelefonnummerRegister(soknadId: String, telefonnummer: String?) {
        log.info("NyModell: Legger til Telefonnummer for $soknadId")
        kotlin.runCatching {
            telefonnummer?.let { v2KontaktAdapter.addTelefonnummerRegister(UUID.fromString(soknadId), it) }
        }
            .onFailure { log.error("Kunne ikke legge til telefonnummer fra register", it) }
    }

    override fun addBasisPersonalia(soknadId: String, personalia: JsonPersonalia) {
        log.info("Ny modell: Legger til Eier")
        kotlin.runCatching {
            v2SoknadAdapter.createEier(
                UUID.fromString(soknadId),
                personalia
            )
        }
            .onFailure { log.error("NyModell: Kunne ikke legge til ny Eier fra register", it) }
    }

    override fun setInnsendingstidspunkt(soknadId: String, innsendingsTidspunkt: LocalDateTime) {
        log.info("NyModell: Setter innsendingstidspunkt")

        kotlin.runCatching {
            v2SoknadAdapter.setInnsendingstidspunkt(
                UUID.fromString(soknadId),
                innsendingsTidspunkt
            )
        }
            .onFailure { log.error("NyModell: Kunne ikke sette innsendingstidspunkt", it) }
    }

    override fun slettSoknad(behandlingsId: String) {
        log.info("NyModell: Sletter SoknadV2")

        kotlin.runCatching {
            soknadService.slettSoknad(UUID.fromString(behandlingsId))
        }
            .onFailure { log.error("NyModell: Kunne ikke slette Soknad V2") }
    }
}
