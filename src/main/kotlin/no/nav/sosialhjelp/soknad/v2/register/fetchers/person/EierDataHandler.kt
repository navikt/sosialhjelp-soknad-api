package no.nav.sosialhjelp.soknad.v2.register.fetchers.person

import no.nav.sosialhjelp.soknad.app.LoggingUtils.logger
import no.nav.sosialhjelp.soknad.personalia.person.domain.Person
import no.nav.sosialhjelp.soknad.v2.eier.Eier
import no.nav.sosialhjelp.soknad.v2.eier.Kontonummer
import no.nav.sosialhjelp.soknad.v2.eier.service.EierRegisterService
import no.nav.sosialhjelp.soknad.v2.navn.Navn
import no.nav.sosialhjelp.soknad.v2.register.fetchers.PersonRegisterDataHandler
import org.springframework.stereotype.Component
import java.util.UUID

@Component
class EierDataHandler(
    private val eierService: EierRegisterService,
) : PersonRegisterDataHandler {
    override fun saveData(
        soknadId: UUID,
        person: Person,
    ) {
        logger.info("Oppdaterer informasjon om soker fra PDL")
        person.deriveStatsborgerskap()
            .let {
                Eier(
                    soknadId = soknadId,
                    statsborgerskap = it.landkode,
                    nordiskBorger = it.erNordiskBorger,
                    navn =
                        Navn(
                            fornavn = person.fornavn,
                            mellomnavn = person.mellomnavn ?: "",
                            etternavn = person.etternavn,
                        ),
                    kontonummer = eierService.getKontonummer(soknadId) ?: Kontonummer(),
                )
            }
            .also { eier -> eierService.updateFromRegister(eier) }
    }

    override fun continueOnError() = false

    private data class Statsborgerskap(
        val landkode: String? = null,
        val erNordiskBorger: Boolean? = null,
    )

    companion object {
        private val logger by logger()

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
