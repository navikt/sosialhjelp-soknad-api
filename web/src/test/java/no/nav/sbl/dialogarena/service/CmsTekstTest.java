package no.nav.sbl.dialogarena.service;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.springframework.context.MessageSource;
import org.springframework.context.NoSuchMessageException;

import java.io.IOException;
import java.util.Locale;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class CmsTekstTest {

    @InjectMocks
    CmsTekst cmsTekst;

    @Mock
    MessageSource messagesourceMock;


    @Before
    public void setup() {
        when(messagesourceMock.getMessage(anyString(), any(Object[].class), any(Locale.class))).thenThrow(new NoSuchMessageException(""));
    }

    @Test
    public void kallerMessageSourceToGangerMedOgUtenPrefixNarKeyIkkeEksisterer() throws IOException {
        cmsTekst.getCmsTekst("min.key", null, "prefix");

        verify(messagesourceMock, times(1)).getMessage(eq("prefix.min.key"), any(Object[].class), any(Locale.class));
        verify(messagesourceMock, times(1)).getMessage(eq("min.key"), any(Object[].class), any(Locale.class));
    }

    @Test
    public void getCmsTekstSierAtKeyManglerNarKeyMangler() throws IOException {
        String tekst = cmsTekst.getCmsTekst("min.key", null, "prefix");

        assertThat(tekst).isEqualTo("KEY MANGLER: [min.key]");
    }
}