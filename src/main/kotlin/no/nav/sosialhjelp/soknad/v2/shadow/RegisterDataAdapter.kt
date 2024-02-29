package no.nav.sosialhjelp.soknad.v2.shadow

import no.nav.sosialhjelp.soknad.arbeid.domain.Arbeidsforhold
import no.nav.sosialhjelp.soknad.personalia.person.domain.Person
import java.time.LocalDateTime

interface RegisterDataAdapter {
    fun createSoknad(behandlingsId: String, opprettetDato: LocalDateTime, eierId: String)
    fun addArbeidsforholdList(soknadId: String, arbeidsforhold: List<Arbeidsforhold>)
    fun addAdresserRegister(soknadId: String, person: Person?)
    fun addTelefonnummerRegister(soknadId: String, systemverdi: String?)
}
