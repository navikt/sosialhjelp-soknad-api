package no.nav.sosialhjelp.soknad.tekster;

import org.junit.Before;
import org.junit.Test;

import no.nav.sosialhjelp.soknad.tekster.NavMessageSource;

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
        mockedCmsValues.put("classpath:dagpenger_nb_NO", "dagpenger.key=norsk dagpenger fra minne;dagpenger.key.minne=norsk dagpenger kun i minne");
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
                    for (String keyValueString : mockedValue.split(";")) {
                        mockedProperties.put(keyValueString.split("=")[0], keyValueString.split("=")[1]);
                    }
                }

                return new PropertiesHolder(mockedProperties, 0);
            }
        };

        messageSource.setBasenames(
                new NavMessageSource.Bundle("sendsoknad", "classpath:sendsoknad"),
                new NavMessageSource.Bundle("dagpenger", "classpath:dagpenger"),
                new NavMessageSource.Bundle("foreldrepenger", "classpath:foreldrepenger")
        );
    }

    @Test
    public void skalHenteSoknadensEgneTeksterOgFellesTeksterNorsk() {
        Properties properties = messageSource.getBundleFor("dagpenger", new Locale("nb", "NO"));
        assertEquals("norsk felles fra minne", properties.getProperty("felles.key"));
        assertEquals("norsk dagpenger fra minne", properties.getProperty("dagpenger.key"));

        properties = messageSource.getBundleFor("foreldrepenger", new Locale("nb", "NO"));
        assertEquals("norsk felles fra minne", properties.getProperty("felles.key"));
        assertEquals("norsk foreldrepenger fra minne", properties.getProperty("foreldrepenger.key"));
    }

    @Test
    public void skalHenteSoknadensEgneTeksterOgFellesTeksterEngelsk() {
        Properties properties = messageSource.getBundleFor("dagpenger", new Locale("en", "GB"));
        assertEquals("engelsk felles fra minne", properties.getProperty("felles.key"));
        assertEquals("engelsk dagpenger fra minne", properties.getProperty("dagpenger.key"));

        properties = messageSource.getBundleFor("foreldrepenger", new Locale("en", "GB"));
        assertEquals("engelsk felles fra minne", properties.getProperty("felles.key"));
        assertEquals("engelsk foreldrepenger fra minne", properties.getProperty("foreldrepenger.key"));
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
        assertEquals("norsk felles fra minne", properties.getProperty("felles.key"));
        assertEquals("norsk dagpenger fra minne", properties.getProperty("dagpenger.key"));
        assertEquals("norsk foreldrepenger fra minne", properties.getProperty("foreldrepenger.key"));
    }

}