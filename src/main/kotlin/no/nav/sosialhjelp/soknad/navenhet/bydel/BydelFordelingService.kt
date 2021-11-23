package no.nav.sosialhjelp.soknad.navenhet.bydel

import no.nav.sosialhjelp.soknad.adressesok.domain.AdresseForslag
import org.apache.commons.lang3.StringUtils

open class BydelFordelingService(
    private val markaBydelFordeling: List<BydelFordeling>
) {

    open fun getBydelTilForMarka(adresseForslag: AdresseForslag): String {
        return markaBydelFordeling
            .filter { it.veiadresse.trim().equals(adresseForslag.adresse?.trim(), true) }
            .filter { isInHusnummerFordeling(it.husnummerfordeling, adresseForslag.husnummer) }
            .firstOrNull()
            ?.bydelTil ?: adresseForslag.geografiskTilknytning ?: ""
    }

    private fun isInHusnummerFordeling(husnummerfordeling: List<Husnummerfordeling>, husnummer: String?): Boolean {
        return husnummerfordeling.any { isInRangeHusnummer(it, husnummer) }
    }

    private fun isInRangeHusnummer(husnummerfordeling: Husnummerfordeling, husnummer: String?): Boolean {
        if (husnummer == null || !StringUtils.isNumeric(husnummer)) {
            return false
        }
        val intHusnummer = husnummer.trim { it <= ' ' }.toInt()
        val isEven = intHusnummer % 2 == 0
        return when (husnummerfordeling.type) {
            HusnummerfordelingType.ALL -> true
            HusnummerfordelingType.EVEN -> isEven && intHusnummer >= husnummerfordeling.fra && intHusnummer <= husnummerfordeling.til
            HusnummerfordelingType.ODD -> !isEven && intHusnummer >= husnummerfordeling.fra && intHusnummer <= husnummerfordeling.til
        }
    }

    companion object {
        const val BYDEL_MARKA_OSLO = "030117"
    }
}
