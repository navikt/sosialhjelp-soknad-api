package no.nav.sosialhjelp.soknad.v2.shadow

import no.nav.sosialhjelp.soknad.arbeid.domain.toV2Arbeidsforhold
import no.nav.sosialhjelp.soknad.personalia.person.domain.Person
import no.nav.sosialhjelp.soknad.v2.shadow.adapter.ArbeidsforholdAdapter
import no.nav.sosialhjelp.soknad.v2.shadow.adapter.KontaktAdapter
import no.nav.sosialhjelp.soknad.v2.shadow.adapter.SoknadAdapter
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component
import java.time.LocalDateTime
import java.util.*

@Component
class SoknadV2RegisterDataAdapter(
    private val soknadAdapter: SoknadAdapter,
    private val arbeidsforholdAdapter: ArbeidsforholdAdapter,
    private val kontaktAdapter: KontaktAdapter,
) : RegisterDataAdapter {

    private val log = LoggerFactory.getLogger(this::class.java)

    override fun createSoknad(behandlingsId: String, opprettetDato: LocalDateTime, eierId: String) {
        log.info("NyModell: Oppretter ny soknad for $behandlingsId")

        kotlin.runCatching {
            soknadAdapter.createNewSoknad(
                soknadId = UUID.fromString(behandlingsId),
                opprettetDato = opprettetDato,
                eierPersonId = eierId
            )
        }
            .onFailure { log.error("Ny modell: Feil ved oppretting av ny soknad i adapter", it) }
    }

    override fun addArbeidsforholdList(soknadId: String, arbeidsforhold: List<no.nav.sosialhjelp.soknad.arbeid.domain.Arbeidsforhold>) {
        log.info("NyModell: Legger til arbeidsforhold for $soknadId")
        kotlin.runCatching {
            arbeidsforholdAdapter.saveArbeidsforhold(
                UUID.fromString(soknadId),
                arbeidsforhold.map { it.toV2Arbeidsforhold() }
            )
        }
            .onFailure { log.error("Ny modell: Kunne ikke legge til arbeidsforhold", it) }
    }

    override fun addAdresserRegister(behandlingsId: String, person: Person?) {
        log.info("NyModell: Legger til adresser for $behandlingsId")

        person?.let {
            kotlin.runCatching {
                kontaktAdapter.saveAdresser(
                    soknadId = UUID.fromString(behandlingsId),
                    bostedsadresse = it.bostedsadresse,
                    oppholdsadresse = it.oppholdsadresse,
                )
            }
                .onFailure { log.error("Legge til adresser feilet for $behandlingsId", it) }
        }
    }

    override fun addTelefonnummerRegister(behandlingsId: String, telefonnummer: String?) {
        log.info("NyModell: Legger til telefonnummer for $behandlingsId")
        kotlin.runCatching {
            telefonnummer?.let { kontaktAdapter.addTelefonnummerRegister(UUID.fromString(behandlingsId), it) }
        }
            .onFailure { log.error("Kunne ikke legge til telefonnummer fra register", it) }
    }
}
