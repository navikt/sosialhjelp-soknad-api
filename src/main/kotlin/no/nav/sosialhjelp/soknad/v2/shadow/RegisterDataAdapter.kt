package no.nav.sosialhjelp.soknad.v2.shadow

import no.nav.sosialhjelp.soknad.personalia.person.domain.Person
import java.time.LocalDateTime

interface RegisterDataAdapter {
    fun createSoknad(behandlingsId: String, opprettetDato: LocalDateTime, eierId: String)
    fun addArbeidsforholdList(soknadId: String, arbeidsforhold: List<no.nav.sosialhjelp.soknad.arbeid.domain.Arbeidsforhold>)
    fun addAdresserRegister(behandlingsId: String, person: Person?)
    fun addTelefonnummerRegister(behandlingsId: String, telefonnummer: String?)
}
