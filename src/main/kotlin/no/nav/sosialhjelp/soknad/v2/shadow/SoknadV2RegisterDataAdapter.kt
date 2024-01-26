package no.nav.sosialhjelp.soknad.v2.shadow

import no.nav.sosialhjelp.soknad.arbeid.domain.toV2Arbeidsforhold
import no.nav.sosialhjelp.soknad.db.repositories.soknadunderarbeid.SoknadUnderArbeid
import no.nav.sosialhjelp.soknad.db.repositories.soknadunderarbeid.toV2Eier
import no.nav.sosialhjelp.soknad.personalia.person.domain.Person
import no.nav.sosialhjelp.soknad.v2.navn.Navn
import no.nav.sosialhjelp.soknad.v2.shadow.adapter.AdresseAdapter
import no.nav.sosialhjelp.soknad.v2.shadow.adapter.SoknadAdapter
import no.nav.sosialhjelp.soknad.v2.soknad.Eier
import org.springframework.stereotype.Component
import java.util.*

@Component
class SoknadV2RegisterDataAdapter(
    private val soknadAdapter: SoknadAdapter,
    private val adresseAdapter: AdresseAdapter,
) : RegisterDataAdapter {
    override fun createSoknad(soknadUnderArbeid: SoknadUnderArbeid) {
        with(soknadUnderArbeid) {
            soknadAdapter.createNewSoknad(
                soknadId = UUID.fromString(behandlingsId),
                opprettetDato = opprettetDato,
                eier = toV2Eier()
                    ?: Eier(
                        personId = soknadUnderArbeid.eier,
                        navn = Navn(
                            fornavn = "Ukjent",
                            etternavn = "Ukjent"
                        )
                    )
            )
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
