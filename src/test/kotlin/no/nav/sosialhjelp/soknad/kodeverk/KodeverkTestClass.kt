package no.nav.sosialhjelp.soknad.kodeverk

import no.nav.sosialhjelp.soknad.kodeverk.dto.BeskrivelseDto
import no.nav.sosialhjelp.soknad.kodeverk.dto.BetydningDto
import no.nav.sosialhjelp.soknad.kodeverk.dto.KodeverkDto
import java.time.LocalDate

open class KodeverkTestClass {
    companion object {
        private fun betydningListeDto(term: String) =
            listOf(
                BetydningDto(
                    LocalDate.now(),
                    LocalDate.now(),
                    mapOf(
                        KodeverkClient.SPRÅK_NORSK_BOKMÅL to
                            BeskrivelseDto(
                                term = term,
                                tekst = "Tekst for $term",
                            ),
                    ),
                ),
            )

        fun mockTermDto(kodeverk: Map<String, String>): KodeverkDto =
            KodeverkDto(kodeverk.map { it.key to betydningListeDto(it.value) }.toMap())

        const val KOMMUNE_BERGEN_NUMMER = "4601"
        const val KOMMUNE_BERGEN_TERM = "Bergen"
        const val KOMMUNE_OSLO_NUMMER = "0301"
        const val KOMMUNE_OSLO_TERM = "Oslo"

        val TEST_KOMMUNER =
            mapOf(
                KOMMUNE_OSLO_NUMMER to KOMMUNE_OSLO_TERM,
                KOMMUNE_BERGEN_NUMMER to KOMMUNE_BERGEN_TERM,
            )

        const val LAND_NORGE_KODE = "NOR"
        const val LAND_NORGE_TERM = "NORGE"
        const val LAND_SVERIGE_KODE = "SWE"
        const val LAND_SVERIGE_TERM = "SVERIGE"

        val TEST_LANDKODER =
            mapOf(
                LAND_NORGE_KODE to LAND_NORGE_TERM,
                LAND_SVERIGE_KODE to LAND_SVERIGE_TERM,
            )

        const val POSTNUMMER_RØDTVET_KODE = "0951"
        const val POSTNUMMER_RØDTVET_TERM = "OSLO"
        const val POSTNUMMER_SAMFUNDET_KODE = "7030"
        const val POSTNUMMER_SAMFUNDET_TERM = "TRONDHEIM"
        const val POSTNUMMER_SESAM_STASJON_KODE = "1470"
        const val POSTNUMMER_SESAM_STASJON_TERM = "LØRENSKOG"

        val TEST_POSTNUMMER =
            mapOf(
                POSTNUMMER_RØDTVET_KODE to POSTNUMMER_RØDTVET_TERM,
                POSTNUMMER_SAMFUNDET_KODE to POSTNUMMER_SAMFUNDET_TERM,
                POSTNUMMER_SESAM_STASJON_KODE to POSTNUMMER_SESAM_STASJON_TERM,
            )
    }
}
