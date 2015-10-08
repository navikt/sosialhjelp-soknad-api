package no.nav.sbl.dialogarena.soknadinnsending.business.service;


import no.nav.modig.common.MDCOperations;
import no.nav.sbl.dialogarena.soknadinnsending.business.domain.Faktum;
import no.nav.sbl.dialogarena.soknadinnsending.consumer.wsconfig.SakOgAktivitetWSConfig;
import no.nav.tjeneste.virksomhet.sakogaktivitet.v1.SakOgAktivitetV1;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.mockserver.client.server.MockServerClient;
import org.mockserver.junit.MockServerRule;
import org.mockserver.model.HttpCallback;
import org.mockserver.model.HttpRequest;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

public class AktivitetServiceIntegrationTest {
    private static int PORT = 10010;
    @Rule
    public MockServerRule mockServerRule = new MockServerRule(PORT, this);
    AktivitetService service;
    private SakOgAktivitetV1 aktivitetWebService;
    private MockServerClient client;

    @Before
    public void setup() {
        MDCOperations.putToMDC(MDCOperations.MDC_CALL_ID, MDCOperations.generateCallId());
        SakOgAktivitetWSConfig config = new SakOgAktivitetWSConfig();
        ReflectionTestUtils.setField(config, "sakOgAktivitetEndpoint", "http://localhost:" + PORT);
        aktivitetWebService = config.factory().get();
        service = new AktivitetService();
        ReflectionTestUtils.setField(service, "aktivitetWebService", aktivitetWebService);
    }

    @Test
    public void shouldThrowException() {
        client.when(HttpRequest.request().withMethod("POST"))
                .callback(new HttpCallback().withCallbackClass("no.nav.sbl.dialogarena.soknadinnsending.business.service.AktivitetServiceIntegrationCallback"));
        List<Faktum> response = service.hentAktiviteter("123");
        assertThat(response).isEmpty();
    }
}
