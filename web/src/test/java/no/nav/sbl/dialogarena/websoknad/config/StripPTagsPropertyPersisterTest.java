package no.nav.sbl.dialogarena.websoknad.config;

import no.nav.sbl.dialogarena.soknadinnsending.business.message.NavMessageSource;
import org.junit.Test;

import java.util.Locale;
import java.util.Properties;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

public class StripPTagsPropertyPersisterTest {

    @Test
    public void skalFjernePTagsFraVerdierIPropertyfil() {
        NavMessageSource messageSource = new NavMessageSource();
        messageSource.setBasenames("classpath:strip_p_tags_test");
        messageSource.setDefaultEncoding("UTF-8");

        messageSource.setPropertiesPersister(new StripPTagsPropertyPersister());

        Properties properties = messageSource.getBundleFor("xxx", new Locale("nb"));

        assertThat(properties.getProperty("nokkel.med.p.tag"), is("This is the text"));
        assertThat(properties.getProperty("nokkel.med.start.p.tag"), is("This is the text"));
        assertThat(properties.getProperty("nokkel.med.slutt.p.tag"), is("This is the text"));
    }

}