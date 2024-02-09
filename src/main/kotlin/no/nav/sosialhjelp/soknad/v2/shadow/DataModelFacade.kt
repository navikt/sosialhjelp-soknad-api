package no.nav.sosialhjelp.soknad.v2.shadow

import no.nav.sbl.soknadsosialhjelp.soknad.adresse.JsonAdresse
import no.nav.sosialhjelp.soknad.db.repositories.soknadunderarbeid.SoknadUnderArbeid
import no.nav.sosialhjelp.soknad.personalia.person.domain.Person
import no.nav.sosialhjelp.soknad.v2.soknad.Arbeidsforhold

interface DataModelFacade {
    fun createSoknad(soknadUnderArbeid: SoknadUnderArbeid)
    fun addArbeidsforholdList(soknadId: String, arbeidsforhold: List<no.nav.sosialhjelp.soknad.arbeid.domain.Arbeidsforhold>)
    fun addAdresserRegister(behandlingsId: String, person: Person?)
    fun addTelefonnummerRegister(behandlingsId: String, telefonnummer: String?)
}