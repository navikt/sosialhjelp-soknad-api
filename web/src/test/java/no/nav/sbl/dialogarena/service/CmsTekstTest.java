package no.nav.sbl.dialogarena.service;

import no.nav.sbl.dialogarena.sendsoknad.domain.message.NavMessageSource;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.io.IOException;
import java.util.Locale;
import java.util.Properties;

import static org.apache.commons.lang3.LocaleUtils.toLocale;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class CmsTekstTest {

    @InjectMocks
    CmsTekst cmsTekst;

    @Mock
    NavMessageSource navMessageSource;

    Properties properties = new Properties();

    @Before
    public void setup() {
        when(navMessageSource.getBundleFor(anyString(), any(Locale.class))).thenReturn(properties);
    }

    @Test
    public void kallerMessageSourceToGangerMedOgUtenPrefixNarKeyIkkeEksisterer() throws IOException {
        properties.put("min.key", "jegFinnes");

        String tekst = this.cmsTekst.getCmsTekst("min.key", null, "prefix", "bundlename", toLocale("nb_NO"));
        assertThat(tekst).isEqualTo("jegFinnes");

        properties.put("prefikset.kul.key", "finnesOgså");
        String prefiksTekst = this.cmsTekst.getCmsTekst("kul.key", null, "prefikset", "bundlename", toLocale("nb_NO"));
        assertThat(prefiksTekst).isEqualTo("finnesOgså");
    }

    @Test
    public void getCmsTekstReturnererNullNarKeyMangler() throws IOException {
        String tekst = cmsTekst.getCmsTekst("min.key", null, "prefix", "bundlename", toLocale("nb_NO"));

        assertThat(tekst).isEqualTo(null);
    }
}