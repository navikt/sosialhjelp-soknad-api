package no.nav.sosialhjelp.soknad.personalia.person.domain

import no.nav.sosialhjelp.soknad.app.LoggingUtils.logger
import no.nav.sosialhjelp.soknad.personalia.person.dto.EndringDto
import no.nav.sosialhjelp.soknad.personalia.person.dto.FolkeregisterMetadataDto
import no.nav.sosialhjelp.soknad.personalia.person.dto.MetadataDto
import no.nav.sosialhjelp.soknad.personalia.person.dto.NavnDto
import no.nav.sosialhjelp.soknad.personalia.person.dto.SivilstandDto
import no.nav.sosialhjelp.soknad.personalia.person.dto.SivilstandType
import org.springframework.stereotype.Component
import java.time.LocalDateTime

@Component
class MapperHelper {

    companion object {
        private val log by logger()

        private const val FREG = "FREG"
        private const val PDL = "PDL"
        private val MASTERS = setOf(FREG, PDL)
        private const val BRUKER_SELV = "Bruker selv"
    }

    fun utledGjeldendeSivilstand(sivilstander: List<SivilstandDto>?): SivilstandDto? {
        if (sivilstander.isNullOrEmpty()) {
            return null
        }
        // sorter sivilstander på synkende endringstidspunkt
        val sorted = sivilstander.sortedWith(compareBy(nullsLast(reverseOrder())) { getEndringstidspunktOrNull(it) })

        if (sorted.size > 1) {
            val sivilstandTyper = sorted.joinToString(separator = ",") { it.type.toString() }
            log.info("Flere gjeldende sivilstander funnet i PDL: [$sivilstandTyper]")
        }
        val sistEndredeSivilstand = sorted[0]
        if (flereSivilstanderRegistrertSamtidig(sistEndredeSivilstand, sorted) ||
            sistEndredeSivilstand.type == SivilstandType.UOPPGITT ||
            //  Kommentert ut fordi vi ikke er 100% sikre på om vi skal vise sivilstander fra udokumenterte kilder (master == "bruker selv").
            //  Hvis disse skal filtreres vekk, kan linjen kommenteres inn igjen.
            //  || erKildeUdokumentert(sistEndredeSivilstand)
            !MASTERS.contains(sistEndredeSivilstand.metadata.master.uppercase())
        ) {
            return null
        }
        if (erKildeUdokumentert(sistEndredeSivilstand.metadata)) {
            log.info("PDL sivilstand er udokumentert (kilde = ${sisteEndringOrNull(sistEndredeSivilstand.metadata)?.kilde})")
        }
        return sistEndredeSivilstand
    }

    fun utledGjeldendeNavn(navn: List<NavnDto>?): NavnDto? {
        if (navn.isNullOrEmpty()) {
            return null
        }
        val sorted = navn.sortedWith(compareBy(nullsLast(reverseOrder())) { getEndringstidspunktOrNull(it) })

        if (sorted.size > 1) {
            log.info("Flere gjeldende navn funnet i PDL")
        }
        val sistEndredeNavn = sorted[0]
        if (flereNavnRegistrertSamtidig(sistEndredeNavn, sorted) ||
            // Kommentert ut fordi vi ikke er 100% sikre på om vi skal vise navn fra udokumenterte kilder (master == "bruker selv").
            // Hvis disse skal filtreres vekk, kan linjen kommenteres inn igjen.
            // || erKildeUdokumentert(sistEndredeNavn)
            !MASTERS.contains(sistEndredeNavn.metadata.master.uppercase())
        ) {
            return null
        }
        if (erKildeUdokumentert(sistEndredeNavn.metadata)) {
            log.info("PDL navn er udokumentert (kilde = ${sisteEndringOrNull(sistEndredeNavn.metadata)?.kilde})")
        }
        return sistEndredeNavn
    }

    private fun getEndringstidspunktOrNull(sivilstandDto: SivilstandDto): LocalDateTime? {
        return getEndringstidspunktOrNull(sivilstandDto.metadata, sivilstandDto.folkeregistermetadata)
    }

    private fun getEndringstidspunktOrNull(navnDto: NavnDto): LocalDateTime? {
        return getEndringstidspunktOrNull(navnDto.metadata, navnDto.folkeregistermetadata)
    }

    private fun getEndringstidspunktOrNull(metadata: MetadataDto, folkeregistermetadata: FolkeregisterMetadataDto?): LocalDateTime? {
        return if (metadata.master.equals(FREG, ignoreCase = true)) {
            folkeregistermetadata?.ajourholdstidspunkt
        } else {
            return metadata.endringer.maxByOrNull { it.registrert }?.registrert
        }
    }

    private fun sisteEndringOrNull(metadata: MetadataDto): EndringDto? {
        return metadata.endringer.maxByOrNull { it.registrert }
    }

    private fun flereSivilstanderRegistrertSamtidig(first: SivilstandDto, list: List<SivilstandDto>): Boolean {
        return list.count { getEndringstidspunktOrNull(it) == getEndringstidspunktOrNull(first) } > 1
    }

    private fun flereNavnRegistrertSamtidig(first: NavnDto, list: List<NavnDto>): Boolean {
        return list.count { getEndringstidspunktOrNull(it) == getEndringstidspunktOrNull(first) } > 1
    }

    private fun erKildeUdokumentert(metadata: MetadataDto): Boolean {
        return PDL.equals(metadata.master, ignoreCase = true) &&
            sisteEndringOrNull(metadata) != null &&
            sisteEndringOrNull(metadata)?.kilde == BRUKER_SELV
    }
}
