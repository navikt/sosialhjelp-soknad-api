package no.nav.sosialhjelp.soknad.v2.shadow

import no.nav.sbl.soknadsosialhjelp.soknad.familie.JsonAnsvar
import no.nav.sbl.soknadsosialhjelp.soknad.familie.JsonSivilstatus
import no.nav.sbl.soknadsosialhjelp.soknad.personalia.JsonPersonalia
import no.nav.sosialhjelp.soknad.arbeid.domain.Arbeidsforhold
import no.nav.sosialhjelp.soknad.personalia.person.domain.Person
import java.time.LocalDateTime

interface V2AdapterService {
    fun createSoknad(
        behandlingsId: String,
        opprettetDato: LocalDateTime,
        eierId: String,
    )

    fun addArbeidsforholdList(
        soknadId: String,
        arbeidsforhold: List<Arbeidsforhold>?,
    )

    fun addAdresserRegister(
        soknadId: String,
        person: Person?,
    )

    fun updateTelefonRegister(
        soknadId: String,
        telefonnummer: String?,
    )

    fun updateEier(
        soknadId: String,
        personalia: JsonPersonalia,
    )

    fun setInnsendingstidspunkt(
        soknadId: String,
        innsendingsTidspunkt: LocalDateTime,
    )

    fun slettSoknad(behandlingsId: String)

    fun addEktefelle(
        behandlingsId: String,
        systemverdiSivilstatus: JsonSivilstatus?,
    )

    fun addBarn(
        behandlingsId: String,
        ansvarList: List<JsonAnsvar>,
    )
}
