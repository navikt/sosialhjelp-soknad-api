package no.nav.sosialhjelp.soknad.consumer.sts;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.Invocation;
import javax.ws.rs.client.WebTarget;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class STSConsumerTest {

    @Mock
    private Client client;
    @Mock
    private WebTarget webTarget;
    @Mock
    private Invocation.Builder request;

    @InjectMocks
    private final STSConsumer consumer = new STSConsumer(client, "endpointurl");

    @BeforeEach
    public void setUp() {
        when(client.target(anyString())).thenReturn(webTarget);
        when(webTarget.queryParam(anyString(), any())).thenReturn(webTarget);
        when(webTarget.request()).thenReturn(request);
        when(request.get(FssToken.class)).thenReturn(new FssToken("asdf", "type", 3600L));
    }

    @Test
    void getFssTokenSkalHenteTokenHvisCacheErTom() {
        FssToken fssToken = consumer.getFSSToken();

        assertThat(fssToken.getAccessToken()).isEqualTo("asdf");
        assertThat(fssToken.getTokenType()).isEqualTo("type");
        assertThat(fssToken.getExpiresIn()).isEqualTo(3600L);
        assertThat(fssToken.isExpired()).isFalse();
    }

    @Test
    void toPafolgendeKallSkalSetteCache() {
        FssToken first = consumer.getFSSToken();

        assertThat(first.getAccessToken()).isEqualTo("asdf");

        verify(request, times(1)).get(FssToken.class);
        verify(client, times(1)).target(anyString());

        FssToken second = consumer.getFSSToken();

        assertThat(second).isEqualTo(first);

        verify(request, times(1)).get(FssToken.class);
        verify(client, times(1)).target(anyString());
    }

    @Test
    void utgattTokenSkalTriggeRenew() {
        // token som har gått ut
        when(request.get(FssToken.class)).thenReturn(new FssToken("asdf", "type", 59L));

        FssToken first = consumer.getFSSToken();

        verify(request, times(1)).get(FssToken.class);
        verify(client, times(1)).target(anyString());
        assertThat(first.isExpired()).isTrue();

        // token som er ikke har gått ut
        when(request.get(FssToken.class)).thenReturn(new FssToken("qwer", "type", 61L));

        FssToken second = consumer.getFSSToken();

        verify(request, times(2)).get(FssToken.class);
        verify(client, times(2)).target(anyString());
        assertThat(second.isExpired()).isFalse();

        assertThat(second).isNotEqualTo(first);
    }
}