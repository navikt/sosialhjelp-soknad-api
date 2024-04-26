package no.nav.sosialhjelp.soknad.v2.register.handlers.person

import java.util.UUID
import no.nav.sosialhjelp.soknad.app.subjecthandler.SubjectHandlerUtils.getUserIdFromToken
import no.nav.sosialhjelp.soknad.personalia.kontonummer.KontonummerService
import no.nav.sosialhjelp.soknad.personalia.person.domain.Person
import no.nav.sosialhjelp.soknad.v2.eier.Eier
import no.nav.sosialhjelp.soknad.v2.eier.EierService
import no.nav.sosialhjelp.soknad.v2.eier.Kontonummer
import no.nav.sosialhjelp.soknad.v2.navn.Navn
import org.springframework.stereotype.Component

@Component
class HandleEierData(
    private val kontonummerService: KontonummerService,
    private val eierService: EierService,
): RegisterDataPersonHandler {

    // oppretter et helt nytt eier-objekt istedetfor å hente eventuelt eksisterende
    override fun handle(soknadId: UUID, person: Person) {
        val kontonummer = kontonummerService.getKontonummer(getUserIdFromToken())

        person.deriveStatsborgerskap()
            .let {
                Eier(
                    soknadId = soknadId,
                    statsborgerskap = it.landkode,
                    nordiskBorger = it.erNordiskBorger,
                    navn = Navn(
                        fornavn = person.fornavn,
                        mellomnavn = person.mellomnavn,
                        etternavn = person.etternavn
                    ),
                    kontonummer = Kontonummer(fraRegister = kontonummer)
                )
            }
            .also { eier -> eierService.updateEierFromRegister(eier) }
    }

    private data class Statsborgerskap(
        val landkode: String? = null,
        val erNordiskBorger: Boolean? = null
    )

    companion object {
        const val PDL_UKJENT_STATSBORGERSKAP = "XUK"
        const val PDL_STATSLOS = "XXX"

        private enum class Prioritert {
            NOR, //1
            SWE, //2
            FRO, //3
            ISL, //4
            DNK, //5
            FIN; //6
        }

        private fun Person.deriveStatsborgerskap(): Statsborgerskap {
            return statsborgerskap
                ?.let { list -> Prioritert.entries.firstOrNull { list.contains(it.name) }?.name ?: list[0] }
                ?.let { kode -> Statsborgerskap(landkode = kode, erNordiskBorger = isNordiskStatsborger(kode) ) }
                ?: Statsborgerskap()
        }

        private fun isNordiskStatsborger(landkode: String): Boolean? {
            return when(landkode) {
                PDL_UKJENT_STATSBORGERSKAP, PDL_STATSLOS -> null
                else -> Prioritert.entries.map { it.name }.contains(landkode)
            }
        }
    }
}
