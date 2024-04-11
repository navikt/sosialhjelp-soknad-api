package no.nav.sosialhjelp.soknad.navenhet.gt.dto

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class GeografiskTilknytningDtoTest {

    @Test
    fun erNorsk() {
        assertTrue(bydelsDto.erNorsk())
        assertTrue(kommuneDto.erNorsk())
        assertFalse(utlandDto.erNorsk())
    }

    @Test
    fun toGtStringOrThrow() {
        assertEquals("030108", bydelsDto.toGtStringOrThrow())
        assertEquals("0301", kommuneDto.toGtStringOrThrow())
        assertThrows(IllegalStateException::class.java) { utlandDto.toGtStringOrThrow() }
    }

    companion object {
        val bydelsDto = GeografiskTilknytningDto(gtType = GtType.BYDEL, gtKommune = null, gtBydel = "030108", gtLand = null)
        val kommuneDto = GeografiskTilknytningDto(gtType = GtType.KOMMUNE, gtKommune = "0301", gtBydel = null, gtLand = null)

        // nb: usikker på den konkrete verdien for gtLand i dette tilfellet
        val utlandDto = GeografiskTilknytningDto(gtType = GtType.UTLAND, gtKommune = null, gtBydel = null, gtLand = "Sverige")
    }
}
