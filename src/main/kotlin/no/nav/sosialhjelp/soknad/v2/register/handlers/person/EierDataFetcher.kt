package no.nav.sosialhjelp.soknad.v2.register.handlers.person

import no.nav.sosialhjelp.soknad.app.LoggingUtils.logger
import no.nav.sosialhjelp.soknad.app.subjecthandler.SubjectHandlerUtils.getUserIdFromToken
import no.nav.sosialhjelp.soknad.personalia.kontonummer.KontonummerService
import no.nav.sosialhjelp.soknad.personalia.person.domain.Person
import no.nav.sosialhjelp.soknad.v2.eier.Eier
import no.nav.sosialhjelp.soknad.v2.eier.Kontonummer
import no.nav.sosialhjelp.soknad.v2.eier.service.EierRegisterService
import no.nav.sosialhjelp.soknad.v2.navn.Navn
import no.nav.sosialhjelp.soknad.v2.register.handlers.PersonRegisterDataFetcher
import org.springframework.stereotype.Component
import java.util.UUID

@Component
class EierDataFetcher(
    private val kontonummerService: KontonummerService,
    private val eierService: EierRegisterService,
) : PersonRegisterDataFetcher {
    private val logger by logger()

    // oppretter et helt nytt eier-objekt istedetfor å hente eventuelt eksisterende
    override fun fetchAndSave(
        soknadId: UUID,
        person: Person,
    ) {
        logger.info("NyModell: Henter ut kontonummer fra Kontoregister")
        val kontonummer = kontonummerService.getKontonummer(getUserIdFromToken())

        logger.info("NyModell: Register: Henter ut person-info fra søker")
        person.deriveStatsborgerskap()
            .let {
                Eier(
                    soknadId = soknadId,
                    statsborgerskap = it.landkode,
                    nordiskBorger = it.erNordiskBorger,
                    navn =
                        Navn(
                            fornavn = person.fornavn,
                            mellomnavn = person.mellomnavn,
                            etternavn = person.etternavn,
                        ),
                    kontonummer = Kontonummer(fraRegister = kontonummer),
                )
            }
            .also { eier -> eierService.updateFromRegister(eier) }
            .also { logger.info("NyModell: Lagret personalia og kontonummer for søker") }
    }

    private data class Statsborgerskap(
        val landkode: String? = null,
        val erNordiskBorger: Boolean? = null,
    )

    companion object {
        const val PDL_UKJENT_STATSBORGERSKAP = "XUK"
        const val PDL_STATSLOS = "XXX"

        private enum class Prioritert {
            NOR, // 1
            SWE, // 2
            FRO, // 3
            ISL, // 4
            DNK, // 5
            FIN, // 6
        }

        private fun Person.deriveStatsborgerskap(): Statsborgerskap {
            return statsborgerskap
                ?.let { list -> Prioritert.entries.firstOrNull { list.contains(it.name) }?.name ?: list[0] }
                ?.let { kode -> Statsborgerskap(landkode = kode, erNordiskBorger = isNordiskStatsborger(kode)) }
                ?: Statsborgerskap()
        }

        private fun isNordiskStatsborger(landkode: String): Boolean? {
            return when (landkode) {
                PDL_UKJENT_STATSBORGERSKAP, PDL_STATSLOS -> null
                else -> Prioritert.entries.map { it.name }.contains(landkode)
            }
        }
    }
}
