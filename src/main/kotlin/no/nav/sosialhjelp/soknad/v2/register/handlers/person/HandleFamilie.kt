package no.nav.sosialhjelp.soknad.v2.register.handlers.person

import java.util.UUID
import no.nav.sosialhjelp.soknad.app.subjecthandler.SubjectHandlerUtils.getUserIdFromToken
import no.nav.sosialhjelp.soknad.personalia.person.PersonService
import no.nav.sosialhjelp.soknad.personalia.person.domain.Barn
import no.nav.sosialhjelp.soknad.personalia.person.domain.Person
import no.nav.sosialhjelp.soknad.v2.familie.Ektefelle
import no.nav.sosialhjelp.soknad.v2.familie.FamilieService
import no.nav.sosialhjelp.soknad.v2.familie.Sivilstatus
import no.nav.sosialhjelp.soknad.v2.navn.Navn
import org.springframework.stereotype.Component

@Component
class HandleFamilie(
    private val familieService: FamilieService,
    private val personService: PersonService,
): RegisterDataPersonHandler {
    override fun handle(soknadId: UUID, person: Person) {

        // TODO Hvis det av en eller annen årsak skulle finnes brukerinnfylte verdier, for så
        // ..plutselig finnes informasjon om ektefelle i register - hva da ?
        person.checkEktefelle()?.let {
            familieService.updateFamilieFraRegister(
                soknadId = soknadId,
                sivilstatus = person.toSivilstatus(),
                ektefelle = it
            )
        }
        handleForsorgerplikt(soknadId)
    }

    private fun Person.checkEktefelle(): Ektefelle? {
        return when {
            sivilstatus.isNullOrEmpty() -> null
            Sivilstatus.GIFT.name.lowercase() != sivilstatus -> null
            else -> ektefelle?.toV2Ektefelle()
        }
    }

    private fun handleForsorgerplikt(soknadId: UUID) {
        personService.hentBarnForPerson(getUserIdFromToken())
            ?.let { it.ifEmpty { null } }
            ?.let { barnlist ->
                familieService.updateForsorgerPliktRegister(
                    soknadId = soknadId,
                    harForsorgerplikt = true,
                    barn = barnlist.map { it.toV2Barn() }
                )
            }
            ?: familieService.updateForsorgerPliktRegister(
                soknadId = soknadId,
                harForsorgerplikt = false,
                barn = emptyList()
            )
    }
}

private fun Barn.toV2Barn(): no.nav.sosialhjelp.soknad.v2.familie.Barn {
    return no.nav.sosialhjelp.soknad.v2.familie.Barn(
        familieKey = UUID.randomUUID(),
        personId = fnr,
        navn = Navn(
            fornavn = fornavn,
            mellomnavn = mellomnavn,
            etternavn = etternavn
        ),
        fodselsdato = fodselsdato?.toString(),
        folkeregistrertSammen = folkeregistrertSammen,
    )
}

private fun Person.toSivilstatus(): Sivilstatus {
    return Sivilstatus.entries.firstOrNull { it.name == sivilstatus?.uppercase() }
        ?: Sivilstatus.TOM
}

private fun no.nav.sosialhjelp.soknad.personalia.person.domain.Ektefelle.toV2Ektefelle(): Ektefelle {
    return if (ikkeTilgangTilEktefelle) toSkjermetEktefelle()
    else Ektefelle(
        navn = Navn(
            fornavn = fornavn ?: "fornavn finnes ikke på ektefelle",
            mellomnavn = mellomnavn,
            etternavn = etternavn ?: "etternavn finnes ikke på ektefelle"
        ),
        fodselsdato = fodselsdato?.toString(),
        personId = fnr,
        harDiskresjonskode = false,
        folkeregistrertMedEktefelle = folkeregistrertSammen,
        kildeErSystem = true
    )
}

private fun toSkjermetEktefelle(): Ektefelle {
    return Ektefelle(
        navn = Navn("", "", ""),
        fodselsdato = null,
        personId = null,
        harDiskresjonskode = true,
        kildeErSystem = true
    )
}