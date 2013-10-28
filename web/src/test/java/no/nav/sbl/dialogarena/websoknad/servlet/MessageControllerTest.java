package no.nav.sbl.dialogarena.websoknad.servlet;


import no.nav.sbl.dialogarena.websoknad.config.NavMessageSource;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.util.Locale;

import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.verify;

@RunWith(MockitoJUnitRunner.class)
public class MessageControllerTest {
    @InjectMocks
    private MessageController messageController;
    @Mock
    private NavMessageSource messageSource;

    @Test
    public void shouldForceNorwegian() {
        messageController.setTvingNorsk(true);
        messageController.hentTekster("Dagpenger", Locale.ENGLISH);
        verify(messageSource).getBundleFor(eq("Dagpenger"), eq(new Locale("nb", "NO")));
    }

    @Test
    public void shouldSendEnglish() {
        messageController.setTvingNorsk(false);
        messageController.hentTekster("Dagpenger", Locale.ENGLISH);
        verify(messageSource).getBundleFor(eq("Dagpenger"), eq(Locale.ENGLISH));
    }
}
