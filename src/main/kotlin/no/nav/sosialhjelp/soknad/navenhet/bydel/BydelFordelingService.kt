package no.nav.sosialhjelp.soknad.navenhet.bydel

import com.fasterxml.jackson.core.JsonProcessingException
import java.io.BufferedReader
import java.io.IOException
import java.io.InputStreamReader
import java.nio.charset.StandardCharsets
import java.util.stream.Collectors
import no.nav.sosialhjelp.soknad.adressesok.domain.AdresseForslag
import no.nav.sosialhjelp.soknad.app.exceptions.SosialhjelpSoknadApiException
import org.apache.commons.lang3.StringUtils
import org.springframework.core.io.ClassPathResource
import org.springframework.stereotype.Component
import tools.jackson.databind.DeserializationFeature
import tools.jackson.module.kotlin.jacksonMapperBuilder
import tools.jackson.module.kotlin.readValue

@Component
class BydelFordelingService {
    private val markaBydelFordeling: List<BydelFordeling>
        get() {
            val json = readBydelsfordelingFromFile()
            return try {
                objectMapper.readValue(json)
            } catch (e: JsonProcessingException) {
                throw SosialhjelpSoknadApiException("BydelFordeling marka: Failed to parse json", e)
            }
        }

    fun getBydelTilForMarka(adresseForslag: AdresseForslag): String {
        return markaBydelFordeling
            .filter { it.veiadresse.trim().equals(adresseForslag.adresse?.trim(), true) }
            .firstOrNull { isInHusnummerFordeling(it.husnummerfordeling, adresseForslag.husnummer) }
            ?.bydelTil ?: adresseForslag.geografiskTilknytning ?: ""
    }

    private fun isInHusnummerFordeling(
        husnummerfordeling: List<Husnummerfordeling>,
        husnummer: String?,
    ): Boolean {
        return husnummerfordeling.any { isInRangeHusnummer(it, husnummer) }
    }

    private fun isInRangeHusnummer(
        husnummerfordeling: Husnummerfordeling,
        husnummer: String?,
    ): Boolean {
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

        private val objectMapper = jacksonMapperBuilder()
            .enable(DeserializationFeature.ACCEPT_SINGLE_VALUE_AS_ARRAY)
            .build()

        private fun readBydelsfordelingFromFile(): String {
            val resource = ClassPathResource("pdl/marka-bydelsfordeling.json")
            return try {
                resource.inputStream.use { inputStream ->
                    BufferedReader(InputStreamReader(inputStream, StandardCharsets.UTF_8)).use {
                        it.lines().collect(Collectors.joining("\n"))
                    }
                }
            } catch (e: IOException) {
                throw SosialhjelpSoknadApiException("BydelFordeling marka: Failed to read file", e)
            }
        }
    }
}
