package no.nav.sbl.dialogarena.soknadinnsending.business.message;

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
        mockedCmsValues.put("c:/sendsoknad_nb_NO", "felles.key=norsk felles fra disk");
        mockedCmsValues.put("c:/sendsoknad_en_GB", "felles.key=engelsk felles fra disk");
        mockedCmsValues.put("c:/dagpenger_nb_NO", "dagpenger.key=norsk dagpenger fra disk");
        mockedCmsValues.put("c:/dagpenger_en_GB", "dagpenger.key=engelsk dagpenger fra disk");
        mockedCmsValues.put("c:/foreldrepenger_nb_NO", "foreldrepenger.key=norsk foreldrepenger fra disk");
        mockedCmsValues.put("c:/foreldrepenger_en_GB", "foreldrepenger.key=engelsk foreldrepenger fra disk");

        mockedCmsValues.put("classpath:sendsoknad_nb_NO", "felles.key=norsk felles fra minne");
        mockedCmsValues.put("classpath:sendsoknad_en_GB", "felles.key=engelsk felles fra minne");
        mockedCmsValues.put("classpath:dagpenger_nb_NO", "dagpenger.key=norsk dagpenger fra minne");
        mockedCmsValues.put("classpath:dagpenger_en_GB", "dagpenger.key=engelsk dagpenger fra minne");
        mockedCmsValues.put("classpath:foreldrepenger_nb_NO", "foreldrepenger.key=norsk foreldrepenger fra minne");
        mockedCmsValues.put("classpath:foreldrepenger_en_GB", "foreldrepenger.key=engelsk foreldrepenger fra minne");
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
                    mockedProperties.put(mockedValue.split("=")[0], mockedValue.split("=")[1]);
                }

                return new PropertiesHolder(mockedProperties, 0);
            }
        };

        messageSource.setBasenames(
                new NavMessageSource.Bundle("sendsoknad", "c:/sendsoknad", "classpath:sendsoknad"),
                new NavMessageSource.Bundle("dagpenger", "c:/dagpenger", "classpath:dagpenger"),
                new NavMessageSource.Bundle("foreldrepenger", "c:/foreldrepenger", "classpath:foreldrepenger")
        );
    }

    @Test
    public void skalHenteSoknadensEgneTeksterOgFellesTeksterNorsk() {
        Properties properties = messageSource.getBundleFor("dagpenger", new Locale("nb", "NO"));
        assertEquals("norsk felles fra disk", properties.getProperty("felles.key"));
        assertEquals("norsk dagpenger fra disk", properties.getProperty("dagpenger.key"));

        properties = messageSource.getBundleFor("foreldrepenger", new Locale("nb", "NO"));
        assertEquals("norsk felles fra disk", properties.getProperty("felles.key"));
        assertEquals("norsk foreldrepenger fra disk", properties.getProperty("foreldrepenger.key"));
    }

    @Test
    public void skalHenteSoknadensEgneTeksterOgFellesTeksterEngelsk() {
        Properties properties = messageSource.getBundleFor("dagpenger", new Locale("en", "GB"));
        assertEquals("engelsk felles fra disk", properties.getProperty("felles.key"));
        assertEquals("engelsk dagpenger fra disk", properties.getProperty("dagpenger.key"));

        properties = messageSource.getBundleFor("foreldrepenger", new Locale("en", "GB"));
        assertEquals("engelsk felles fra disk", properties.getProperty("felles.key"));
        assertEquals("engelsk foreldrepenger fra disk", properties.getProperty("foreldrepenger.key"));
    }

    @Test
    public void skalIkkeHenteAndreSoknadersTekster() {
        Properties properties = messageSource.getBundleFor("dagpenger", new Locale("nb", "NO"));
        assertFalse(properties.containsKey("foreldrepenger.key"));

        properties = messageSource.getBundleFor("foreldrepenger", new Locale("nb", "NO"));
        assertFalse(properties.containsKey("dagpenger.key"));
    }

    @Test
    public void skalHenteAlleTeksterHvisTypeMangler() {
        Properties properties = messageSource.getBundleFor(null, new Locale("nb", "NO"));
        assertEquals("norsk felles fra disk", properties.getProperty("felles.key"));
        assertEquals("norsk dagpenger fra disk", properties.getProperty("dagpenger.key"));
        assertEquals("norsk foreldrepenger fra disk", properties.getProperty("foreldrepenger.key"));
    }

    @Test
    public void skalHenteTeksterFraMinneOmDiskIkkeFinnes() {
        diskFilesExist = false;

        Properties properties = messageSource.getBundleFor("dagpenger", new Locale("nb", "NO"));
        assertEquals("norsk felles fra minne", properties.getProperty("felles.key"));
        assertEquals("norsk dagpenger fra minne", properties.getProperty("dagpenger.key"));

        diskFilesExist = true;

        properties = messageSource.getBundleFor("dagpenger", new Locale("nb", "NO"));
        assertEquals("norsk felles fra disk", properties.getProperty("felles.key"));
        assertEquals("norsk dagpenger fra disk", properties.getProperty("dagpenger.key"));
    }
}