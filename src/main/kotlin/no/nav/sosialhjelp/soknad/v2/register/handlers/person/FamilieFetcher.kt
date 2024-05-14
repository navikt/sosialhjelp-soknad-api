package no.nav.sosialhjelp.soknad.v2.register.handlers.person

import no.nav.sosialhjelp.soknad.app.LoggingUtils.logger
import no.nav.sosialhjelp.soknad.app.subjecthandler.SubjectHandlerUtils.getUserIdFromToken
import no.nav.sosialhjelp.soknad.personalia.person.PersonService
import no.nav.sosialhjelp.soknad.personalia.person.domain.Barn
import no.nav.sosialhjelp.soknad.personalia.person.domain.Person
import no.nav.sosialhjelp.soknad.v2.familie.Ektefelle
import no.nav.sosialhjelp.soknad.v2.familie.Sivilstatus
import no.nav.sosialhjelp.soknad.v2.familie.service.FamilieRegisterService
import no.nav.sosialhjelp.soknad.v2.navn.Navn
import org.springframework.stereotype.Component
import java.util.UUID
import no.nav.sosialhjelp.soknad.v2.register.handlers.PersonRegisterDataFetcher
import no.nav.sosialhjelp.soknad.personalia.person.domain.Ektefelle as V2Ektefelle

@Component
class FamilieFetcher(
    private val familieService: FamilieRegisterService,
    private val personService: PersonService,
) : PersonRegisterDataFetcher {
    private val logger by logger()

    override fun fetchAndSave(
        soknadId: UUID,
        person: Person,
    ) {
        logger.info("NyModell: Register: Henter ut familie-info for søker")
        // TODO Hvis det av en eller annen årsak skulle finnes brukerinnfylte verdier, for så
        // ..plutselig finnes informasjon om ektefelle i register - hva da ?
        person.checkEktefelle()?.let {
            logger.info("NyModell: Register: Oppdaterer ektefelle for søker")
            familieService.updateSivilstatusFromRegister(
                soknadId = soknadId,
                sivilstatus = person.toSivilstatus(),
                ektefelle = it,
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

                logger.info("NyModell: Register: Henter info om barn for søker")

                familieService.updateForsorgerpliktRegister(
                    soknadId = soknadId,
                    harForsorgerplikt = true,
                    barn = barnlist.map { it.toV2Barn() },
                )
            }
            ?: familieService.updateForsorgerpliktRegister(
                soknadId = soknadId,
                harForsorgerplikt = false,
                barn = emptyList(),
            )
    }
}

private fun Barn.toV2Barn(): no.nav.sosialhjelp.soknad.v2.familie.Barn {
    return no.nav.sosialhjelp.soknad.v2.familie.Barn(
        familieKey = UUID.randomUUID(),
        personId = fnr,
        navn =
            Navn(
                fornavn = fornavn,
                mellomnavn = mellomnavn,
                etternavn = etternavn,
            ),
        fodselsdato = fodselsdato?.toString(),
        folkeregistrertSammen = folkeregistrertSammen,
    )
}

private fun Person.toSivilstatus(): Sivilstatus {
    return Sivilstatus.entries.firstOrNull { it.name == sivilstatus?.uppercase() }
        ?: Sivilstatus.TOM
}

private fun V2Ektefelle.toV2Ektefelle(): Ektefelle {
    return if (ikkeTilgangTilEktefelle) {
        toSkjermetEktefelle()
    } else {
        Ektefelle(
            navn =
                Navn(
                    fornavn = fornavn ?: "fornavn finnes ikke på ektefelle",
                    mellomnavn = mellomnavn,
                    etternavn = etternavn ?: "etternavn finnes ikke på ektefelle",
                ),
            fodselsdato = fodselsdato?.toString(),
            personId = fnr,
            harDiskresjonskode = false,
            folkeregistrertMedEktefelle = folkeregistrertSammen,
            kildeErSystem = true,
        )
    }
}

private fun toSkjermetEktefelle(): Ektefelle {
    return Ektefelle(
        navn = Navn("", "", ""),
        fodselsdato = null,
        personId = null,
        harDiskresjonskode = true,
        kildeErSystem = true,
    )
}
