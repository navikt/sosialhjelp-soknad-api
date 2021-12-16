package no.nav.sosialhjelp.soknad.tekster

import no.nav.sosialhjelp.soknad.tekster.NavMessageSource.Bundle
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.util.Locale
import java.util.Properties

internal class NavMessageSourceTest {
    private val mockedCmsValues: MutableMap<String, String> = HashMap()

    init {
        mockedCmsValues["classpath:sendsoknad_nb_NO"] = "felles.key=norsk felles fra minne"
        mockedCmsValues["classpath:sendsoknad_en_GB"] = "felles.key=engelsk felles fra minne"
    }

    private var messageSource: NavMessageSource? = null
    private val diskFilesExist = true

    @BeforeEach
    fun setup() {
        messageSource = object : NavMessageSource() {
            override fun getProperties(filename: String): PropertiesHolder {
                var mockedProperties: Properties? = Properties()
                if (!diskFilesExist && filename.contains("c:/")) {
                    mockedProperties = null
                } else if (!mockedCmsValues.containsKey(filename)) {
                    mockedProperties = null
                } else {
                    val mockedValue = mockedCmsValues[filename]
                    for (keyValueString in mockedValue!!.split(";").toTypedArray()) {
                        mockedProperties!![keyValueString.split("=").toTypedArray()[0]] =
                            keyValueString.split("=").toTypedArray()[1]
                    }
                }
                return PropertiesHolder(mockedProperties, 0)
            }
        }
        (messageSource as NavMessageSource).setBasenames(Bundle("sendsoknad", "classpath:sendsoknad"))
    }

    @Test
    fun skalHenteSoknadensEgneTeksterOgFellesTeksterNorsk() {
        val properties = messageSource!!.getBundleFor("sendsoknad", Locale("nb", "NO"))
        assertThat(properties!!.getProperty("felles.key")).isEqualTo("norsk felles fra minne")
    }

    @Test
    fun skalHenteSoknadensEgneTeksterOgFellesTeksterEngelsk() {
        val properties = messageSource!!.getBundleFor("sendsoknad", Locale("en", "GB"))
        assertThat(properties!!.getProperty("felles.key")).isEqualTo("engelsk felles fra minne")
    }

    @Test
    fun skalIkkeHenteAndreSoknadersTekster() {
        val properties = messageSource!!.getBundleFor("sendsoknad", Locale("nb", "NO"))
        assertThat(properties).doesNotContainKey("annen.key")
    }

    @Test
    fun skalHenteAlleTeksterHvisTypeMangler() {
        val properties = messageSource!!.getBundleFor(null, Locale("nb", "NO"))
        assertThat(properties!!.getProperty("felles.key")).isEqualTo("norsk felles fra minne")
    }
}
