package no.nav.sosialhjelp.soknad.adressesok.sok

import no.nav.sosialhjelp.soknad.kodeverk.KodeverkService
import java.util.regex.Pattern

object AdresseStringSplitter {

    fun testFun() = "hei"

    fun toSokedata(kodeverkService: KodeverkService?, adresse: String?): Sokedata? {
        return if (isAddressTooShortOrNull(adresse)) {
            Sokedata(adresse = adresse)
        } else firstNonNull(
            fullstendigGateadresseMatch(kodeverkService, adresse),
            Sokedata(adresse = adresse)
        )
    }

    private fun fullstendigGateadresseMatch(kodeverkService: KodeverkService?, adresse: String?): Sokedata? {
        val p = Pattern.compile("^([^0-9,]*) *([0-9]*)?([^,])? *,? *([0-9]{1,4})? *[0-9]* *([^0-9]*[^ ])? *$")
        val m = p.matcher(adresse)
        if (m.matches()) {
            val postnummer = m.group(4)
            val kommunenavn = if (postnummer == null) m.group(5) else null
            val kommunenummer = getKommunenummer(kodeverkService, kommunenavn)
            val poststed = if (kommunenummer == null) m.group(5) else null
            val gateAdresse = m.group(1).trim { it <= ' ' }.replace(" +".toRegex(), " ")
            return Sokedata(gateAdresse, m.group(2), m.group(3), postnummer, poststed, kommunenummer)
        }
        return null
    }

    private fun getKommunenummer(kodeverkService: KodeverkService?, kommunenavn: String?): String? {
        return if (kommunenavn != null && kommunenavn.trim { it <= ' ' }.isNotEmpty() && kodeverkService != null) {
            kodeverkService.gjettKommunenummer(kommunenavn)
        } else null
    }

    private fun firstNonNull(vararg elems: Sokedata?): Sokedata? {
        return elems.firstOrNull { it != null }
    }

    fun isAddressTooShortOrNull(address: String?): Boolean {
        return address == null || address.trim { it <= ' ' }.length < 2
    }
}
