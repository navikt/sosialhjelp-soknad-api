package no.nav.sbl.dialogarena.service;

import no.nav.sbl.dialogarena.soknadinnsending.business.domain.WebSoknad;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;

import java.io.IOException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

@RunWith(MockitoJUnitRunner.class)
public class HandleBarHelperTest {

    @Test
    public void hvisLikSkalViseInnholdVedToLikeStrenger() throws IOException {
        WebSoknad mockSoknad = mock(WebSoknad.class);
        String html = new HandleBarKjoerer().fyllHtmlMalMedInnhold(mockSoknad, "/skjema/hvisLik");
        assertThat(html).contains("erLik:true");
    }

    @Test
    public void hvisLikSkalViseInnholdIElseVedToUlikeStrenger() throws IOException {
        WebSoknad mockSoknad = mock(WebSoknad.class);
        String html = new HandleBarKjoerer().fyllHtmlMalMedInnhold(mockSoknad, "/skjema/hvisLik");
        assertThat(html).contains("ulike:false");
    }

    @Test
    public void hvisLikSkalViseNostetInnhold() throws IOException {
        WebSoknad mockSoknad = mock(WebSoknad.class);
        String html = new HandleBarKjoerer().fyllHtmlMalMedInnhold(mockSoknad, "/skjema/hvisLik");
        assertThat(html).contains("erLik:nested:true");
    }
}