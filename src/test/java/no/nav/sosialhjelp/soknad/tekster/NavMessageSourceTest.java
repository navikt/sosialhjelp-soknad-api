package no.nav.sosialhjelp.soknad.tekster;

import org.junit.Before;
import org.junit.Test;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Properties;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

public class NavMessageSourceTest {
    private Map<String, String> mockedCmsValues = new HashMap<>();
    {
        mockedCmsValues.put("classpath:sendsoknad_nb_NO", "felles.key=norsk felles fra minne");
        mockedCmsValues.put("classpath:sendsoknad_en_GB", "felles.key=engelsk felles fra minne");
    }

    private NavMessageSource messageSource;
    private boolean diskFilesExist = true;

    @Before
    public void setup() {
        messageSource = new NavMessageSource() {
            @Override
            protected PropertiesHolder getProperties(String filename) {
                Properties mockedProperties = new Properties();

                if (!diskFilesExist && filename.contains("c:/")) {
                    mockedProperties = null;
                } else if (!mockedCmsValues.containsKey(filename)) {
                    mockedProperties = null;
                } else {
                    String mockedValue = mockedCmsValues.get(filename);
                    for (String keyValueString : mockedValue.split(";")) {
                        mockedProperties.put(keyValueString.split("=")[0], keyValueString.split("=")[1]);
                    }
                }

                return new PropertiesHolder(mockedProperties, 0);
            }
        };

        messageSource.setBasenames(
                new NavMessageSource.Bundle("sendsoknad", "classpath:sendsoknad")
        );
    }

    @Test
    public void skalHenteSoknadensEgneTeksterOgFellesTeksterNorsk() {
        Properties properties = messageSource.getBundleFor("sendsoknad", new Locale("nb", "NO"));
        assertEquals("norsk felles fra minne", properties.getProperty("felles.key"));
    }

    @Test
    public void skalHenteSoknadensEgneTeksterOgFellesTeksterEngelsk() {
        Properties properties = messageSource.getBundleFor("sendsoknad", new Locale("en", "GB"));
        assertEquals("engelsk felles fra minne", properties.getProperty("felles.key"));
    }

    @Test
    public void skalIkkeHenteAndreSoknadersTekster() {
        Properties properties = messageSource.getBundleFor("sendsoknad", new Locale("nb", "NO"));
        assertFalse(properties.containsKey("annen.key"));
    }

    @Test
    public void skalHenteAlleTeksterHvisTypeMangler() {
        Properties properties = messageSource.getBundleFor(null, new Locale("nb", "NO"));
        assertEquals("norsk felles fra minne", properties.getProperty("felles.key"));
    }

}