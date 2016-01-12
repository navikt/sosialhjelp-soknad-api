package no.nav.sbl.dialogarena.soknadinnsending.consumer.arbeid;

import org.apache.http.*;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.message.BasicStatusLine;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.runners.MockitoJUnitRunner;

import java.io.ByteArrayInputStream;
import java.io.IOException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class ArbeidssokerInfoServiceTest {
    @InjectMocks
    ArbeidssokerInfoService arbeidssokerInfoService;

    @Mock
    private CloseableHttpClient httpclient;

    @Before
    public void setup(){
        MockitoAnnotations.initMocks(ArbeidssokerInfoService.class);
    }

    @Test
    public void getArbeidssokerArenaStatusSkalReturnereBrukerStatus() throws Exception {
        CloseableHttpResponse response = buildResponse("{\"arenaStatusKode\": \"PARBS\"}");
        when(httpclient.execute(any(HttpUriRequest.class))).thenReturn(response);
        assertThat(arbeidssokerInfoService.getArbeidssokerArenaStatus("11111111111")).isEqualTo("PARBS");
    }

    @Test
    public void getArbeidssokerStatusSkalReturnereUkjentForTomRespons() throws Exception {
        CloseableHttpResponse response = buildResponse("{}");
        when(httpclient.execute(any(HttpUriRequest.class))).thenReturn(response);
        assertThat(arbeidssokerInfoService.getArbeidssokerArenaStatus("11111111111")).isEqualTo("UKJENT");
    }

    @Test
    public void getArbeidssokerRegistreringStatusSkalReturnereUkjentVedFeilPaaRestKall() throws Exception {
        when(httpclient.execute(any(HttpUriRequest.class))).thenThrow(new IOException());
        assertThat(arbeidssokerInfoService.getArbeidssokerArenaStatus("11111111111")).isEqualTo("UKJENT");
    }

    public CloseableHttpResponse buildResponse(String content) throws IOException {
        HttpEntity body = mock(HttpEntity.class);
        when(body.getContent()).thenReturn(new ByteArrayInputStream(content.getBytes()));

        CloseableHttpResponse response = mock(CloseableHttpResponse.class);
        when(response.getStatusLine()).thenReturn(new BasicStatusLine(HttpVersion.HTTP_1_1, HttpStatus.SC_OK, "OK"));
        when(response.getEntity()).thenReturn(body);
        return response;
    }
}