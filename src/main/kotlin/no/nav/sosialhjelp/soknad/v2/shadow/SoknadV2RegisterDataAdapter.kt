package no.nav.sosialhjelp.soknad.v2.shadow

import no.nav.sosialhjelp.soknad.arbeid.domain.toV2Arbeidsforhold
import no.nav.sosialhjelp.soknad.personalia.person.domain.Person
import no.nav.sosialhjelp.soknad.v2.navn.Navn
import no.nav.sosialhjelp.soknad.v2.shadow.adapter.AdresseAdapter
import no.nav.sosialhjelp.soknad.v2.shadow.adapter.SoknadAdapter
import no.nav.sosialhjelp.soknad.v2.soknad.Eier
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component
import java.time.LocalDateTime
import java.util.*

@Component
class SoknadV2RegisterDataAdapter(
    private val soknadAdapter: SoknadAdapter,
    private val adresseAdapter: AdresseAdapter,
) : RegisterDataAdapter {
    val log: Logger = LoggerFactory.getLogger(this::class.java)

    override fun createSoknad(behandlingsId: String, opprettetDato: LocalDateTime, eierId: String) {

        val result = kotlin.runCatching {
            soknadAdapter.createNewSoknad(
                soknadId = UUID.fromString(behandlingsId),
                opprettetDato = opprettetDato,
                eier = Eier(
                    personId = eierId,
                    navn = Navn(
                        fornavn = "Ukjent",
                        etternavn = "Ukjent"
                    )
                )
            )
        }
        result.onFailure {
            log.error("Ny modell: Feil ved oppretting av ny soknad i adapter", it)
        }
    }

    override fun addArbeidsforholdList(soknadId: String, arbeidsforhold: List<no.nav.sosialhjelp.soknad.arbeid.domain.Arbeidsforhold>) {
        soknadAdapter.handleArbeidsforholdList(
            UUID.fromString(soknadId),
            arbeidsforhold.map { it.toV2Arbeidsforhold() }
        )
    }

    override fun addAdresserRegister(behandlingsId: String, person: Person?) {
        person?.let {
            adresseAdapter.updateAdresserFraRegister(
                soknadId = UUID.fromString(behandlingsId),
                folkeregistrertAdresse = it.bostedsadresse,
                midlertidigAdresse = it.oppholdsadresse,
            )
        }
    }

    override fun addTelefonnummerRegister(behandlingsId: String, telefonnummer: String?) {
        telefonnummer?.let { soknadAdapter.addTelefonnummer(UUID.fromString(behandlingsId), it) }
    }
}
