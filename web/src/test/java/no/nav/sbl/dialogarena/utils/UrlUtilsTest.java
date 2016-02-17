package no.nav.sbl.dialogarena.utils;

import org.junit.Before;
import org.junit.Test;

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.startsWith;
import static org.junit.Assert.assertThat;

public class UrlUtilsTest {

    private static final String BEHANDLING_ID = "100000XXX";
    private static final String DIALOGINNSENDING_SOKNAD_URL = "https://tjenester.nav.no/dialoginnsending";
    private static final String DIALOGINNSENDING_ETTERSENDING_PATH = "/dialoginnsending/ettersending";
    private static final String REQUEST_URL = "https://tjenester.nav.no/something/234q/endamer";

    @Before
    public void setup() {
        System.setProperty("soknadinnsending.link.url", DIALOGINNSENDING_SOKNAD_URL);
        System.setProperty("soknadinnsending.ettersending.path", DIALOGINNSENDING_ETTERSENDING_PATH);
    }

    @Test
    public void skalLageUrlForFortsettSenere() {
        assertThat(UrlUtils.getFortsettUrl(BEHANDLING_ID), startsWith("https://tjenester.nav.no/dialoginnsending/soknad/" + BEHANDLING_ID));
    }

    @Test
    public void skalLageUrlForEttersending() {
        assertThat(UrlUtils.getEttersendelseUrl(REQUEST_URL, BEHANDLING_ID), equalTo("https://tjenester.nav.no/dialoginnsending/ettersending/" + BEHANDLING_ID));
    }

    @Test
    public void skalHandtereBehandlingsIdSomErNull() {
        assertThat(UrlUtils.getEttersendelseUrl(REQUEST_URL, null), equalTo("https://tjenester.nav.no/dialoginnsending/ettersending/null"));
    }

}