package no.nav.sbl.dialogarena.soknadinnsending.business;

import no.nav.sbl.dialogarena.sendsoknad.domain.kravdialoginformasjon.KravdialogInformasjonHolder;
import no.nav.sbl.dialogarena.sendsoknad.domain.kravdialoginformasjon.SosialhjelpInformasjon;
import no.nav.sbl.dialogarena.soknadinnsending.business.db.soknad.SoknadRepository;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.runners.MockitoJUnitRunner;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class WebSoknadConfigTest {

    private static final Long SOKNAD_ID = 123L;
    public static final String AAP_SKJEMANUMMER = "NAV 11-13.05";

    @Mock
    SoknadRepository repository;

    @Spy
    KravdialogInformasjonHolder kravdialog = new KravdialogInformasjonHolder();

    @InjectMocks
    WebSoknadConfig config;

    @Before
    public void setUp() {
        when(repository.hentSoknadType(SOKNAD_ID)).thenReturn(SosialhjelpInformasjon.SKJEMANUMMER);
    }

    @Test
    public void testSoknadtypePrefix() {
        String prefix = config.getSoknadTypePrefix(SOKNAD_ID);
        assertThat(prefix).isEqualTo("soknadsosialhjelp");
    }
}
