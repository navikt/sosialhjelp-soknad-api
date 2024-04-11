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
        val bydelsDto = GeografiskTilknytningDto(GtType.BYDEL, "030108", null, null)
        val kommuneDto = GeografiskTilknytningDto(GtType.KOMMUNE, null, "0301", null)

        // nb: usikker på den konkrete verdien for gtLand i dette tilfellet
        val utlandDto = GeografiskTilknytningDto(GtType.UTLAND, null, null, "Sverige")
    }
}
