package no.nav.sosialhjelp.soknad.personalia.person.domain

import no.nav.sosialhjelp.soknad.app.LoggingUtils.logger
import no.nav.sosialhjelp.soknad.personalia.person.dto.EndringDto
import no.nav.sosialhjelp.soknad.personalia.person.dto.MetadataDto
import no.nav.sosialhjelp.soknad.personalia.person.dto.MetadataInfo
import no.nav.sosialhjelp.soknad.personalia.person.dto.NavnDto
import no.nav.sosialhjelp.soknad.personalia.person.dto.SivilstandDto
import no.nav.sosialhjelp.soknad.personalia.person.dto.SivilstandType
import java.time.LocalDateTime

object MapperHelper {
    private val logger by logger()

    private const val FREG = "FREG"
    private const val PDL = "PDL"
    private val MASTERS = setOf(FREG, PDL)
    private const val BRUKER_SELV = "Bruker selv"

    fun utledGjeldendeSivilstand(sivilstander: List<SivilstandDto>?): SivilstandDto? {
        if (sivilstander.isNullOrEmpty()) return null

        // sorter sivilstander på synkende endringstidspunkt
        val sorted = sivilstander.sortedWith(compareBy(nullsLast(reverseOrder())) { it.getEndringstidspunktOrNull() })

        if (sorted.size > 1) {
            val sivilstandTyper = sorted.joinToString(separator = ",") { it.type.toString() }
            logger.info("Flere gjeldende sivilstander funnet i PDL: [$sivilstandTyper]")
        }
        val sistEndredeSivilstand = sorted[0]
        if (
            flereRegistrertSamtidig(
                sistEndredeSivilstand.getEndringstidspunktOrNull(),
                sorted.map { it.getEndringstidspunktOrNull() },
            ) ||
            sistEndredeSivilstand.type == SivilstandType.UOPPGITT ||
            !MASTERS.contains(sistEndredeSivilstand.metadata.master.uppercase())
        ) {
            return null
        }

        if (erKildeUdokumentert(sistEndredeSivilstand.metadata)) {
            logger.info("PDL sivilstand er udokumentert (kilde = ${sistEndredeSivilstand.metadata.sisteEndringOrNull()?.kilde})")
        }
        return sistEndredeSivilstand
    }

    fun utledGjeldendeNavn(navn: List<NavnDto>?): NavnDto? {
        if (navn.isNullOrEmpty()) return null

        val sorted = navn.sortedWith(compareBy(nullsLast(reverseOrder())) { it.getEndringstidspunktOrNull() })

        if (sorted.size > 1) {
            logger.info("Flere gjeldende navn funnet i PDL")
        }
        val sistEndredeNavn = sorted[0]
        if (flereRegistrertSamtidig(
                sistEndredeNavn.getEndringstidspunktOrNull(),
                sorted.map { it.getEndringstidspunktOrNull() },
            ) || !MASTERS.contains(sistEndredeNavn.metadata.master.uppercase())
        ) {
            return null
        }

        if (erKildeUdokumentert(sistEndredeNavn.metadata)) {
            logger.info("PDL navn er udokumentert (kilde = ${sistEndredeNavn.metadata.sisteEndringOrNull()?.kilde})")
        }
        return sistEndredeNavn
    }

    private fun MetadataInfo.getEndringstidspunktOrNull(): LocalDateTime? {
        return if (metadata.master.equals(FREG, ignoreCase = true)) {
            folkeregistermetadata?.ajourholdstidspunkt
        } else {
            metadata.endringer.maxByOrNull { it.registrert }?.registrert
        }
    }

    private fun MetadataDto.sisteEndringOrNull(): EndringDto? = endringer.maxByOrNull { it.registrert }

    private fun flereRegistrertSamtidig(
        first: LocalDateTime?,
        list: List<LocalDateTime?>,
    ): Boolean {
        return when (first) {
            null -> list.count { it == null } > 1
            else -> list.filterNotNull().count { first.isEqual(it) } > 1
        }
    }

    private fun erKildeUdokumentert(metadata: MetadataDto): Boolean {
        return PDL.equals(metadata.master, ignoreCase = true) &&
            metadata.sisteEndringOrNull() != null &&
            metadata.sisteEndringOrNull()?.kilde == BRUKER_SELV
    }
}
