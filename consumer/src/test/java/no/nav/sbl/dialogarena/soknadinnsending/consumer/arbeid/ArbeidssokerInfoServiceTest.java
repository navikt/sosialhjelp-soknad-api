package no.nav.sbl.dialogarena.soknadinnsending.consumer.arbeid;

import org.apache.http.HttpResponse;
import org.apache.http.ProtocolVersion;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.entity.BasicHttpEntity;
import org.apache.http.message.BasicHttpResponse;
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
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class ArbeidssokerInfoServiceTest {
    @InjectMocks
    ArbeidssokerInfoService arbeidssokerInfoService;

    @Mock
    private HttpClient httpclient;

    @Before
    public void setup(){
        MockitoAnnotations.initMocks(ArbeidssokerInfoService.class);
    }

    @Test
    public void getArbeidssokerRegistreringStatusSkalReturnereBrukerStatus() throws Exception {
        when(httpclient.execute(any(HttpUriRequest.class))).thenReturn(buildResponse("{\"brukerStatus\": \"PARBS\"}"));
        assertThat(arbeidssokerInfoService.getArbeidssokerRegistreringStatus("***REMOVED***")).isEqualTo("PARBS");
    }

    @Test
    public void getArbeidssokerRegistreringStatusSkalReturnereUkjentForTomRespons() throws Exception {
        when(httpclient.execute(any(HttpUriRequest.class))).thenReturn(buildResponse("{}"));
        assertThat(arbeidssokerInfoService.getArbeidssokerRegistreringStatus("***REMOVED***")).isEqualTo("UKJENT");
    }

    @Test
    public void getArbeidssokerRegistreringStatusSkalReturnereUkjentVedFeilPaaRestKall() throws Exception {
        when(httpclient.execute(any(HttpUriRequest.class))).thenThrow(new IOException());
        assertThat(arbeidssokerInfoService.getArbeidssokerRegistreringStatus("***REMOVED***")).isEqualTo("UKJENT");
    }

    public HttpResponse buildResponse(String content) {
        HttpResponse response = new BasicHttpResponse(new BasicStatusLine(new ProtocolVersion("HTTP", 1, 1), 200, "OK"));
        BasicHttpEntity body = new BasicHttpEntity();
        body.setContent(new ByteArrayInputStream(content.getBytes()));
        body.setContentLength(content.length());
        response.setEntity(body);
        return response;
    }
}