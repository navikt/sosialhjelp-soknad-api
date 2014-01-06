package no.nav.sbl.dialogarena.websoknad.selftest;

import no.nav.modig.wicket.selftest.SelfTestBase;
import no.nav.sbl.dialogarena.soknadinnsending.consumer.ConsumerConfig;
import no.nav.tjeneste.domene.brukerdialog.sendsoknad.v1.SendSoknadPortType;
import no.nav.tjeneste.virksomhet.brukerprofil.v1.BrukerprofilPortType;
import no.nav.tjeneste.virksomhet.kodeverk.v2.KodeverkPortType;
import org.apache.wicket.request.mapper.parameter.PageParameters;
import org.slf4j.Logger;
import org.springframework.context.annotation.Import;

import javax.inject.Inject;
import javax.inject.Named;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.List;

import static java.lang.String.format;
import static java.lang.System.currentTimeMillis;
import static java.net.HttpURLConnection.HTTP_OK;
import static org.slf4j.LoggerFactory.getLogger;

@Import({ConsumerConfig.WsServices.class})
public class SelfTestPage extends SelfTestBase {
    private static final Logger LOGGER = getLogger(SelfTestPage.class);

    @Inject
    @Named("sendSoknadSelftest")
    private SendSoknadPortType sendSoknadSelftest;

    @Inject
    @Named("kodeverkServiceSelftest")
    private KodeverkPortType kodeverkServiceSelftest;

    @Inject
    @Named("brukerProfilService")
    private BrukerprofilPortType brukerProfilService;

    @Inject
    @Named(value = "cmsBaseUrl")
    private String cmsBaseUrl;

    public SelfTestPage(PageParameters params) {
        super("sendsoknad", params);
    }

    @Override
    protected void addToStatusList(List<AvhengighetStatus> statusList) {
        new ServiceStatusHenter("SENDSOKNAD") {
            public void ping() {
                sendSoknadSelftest.ping();
            }
        }.addStatus(statusList);

        new ServiceStatusHenter("KODEVERK") {
            public void ping() {
                kodeverkServiceSelftest.ping();
            }
        }.addStatus(statusList);

        new ServiceStatusHenter("TPS_HENT_BRUKERPROFIL") {
            public void ping() {
                brukerProfilService.ping();
            }
        }.addStatus(statusList);

        statusList.add(cmsStatus());
    }

    private AvhengighetStatus cmsStatus() {
        long startTime = currentTimeMillis();
        String status = SelfTestBase.STATUS_ERROR;

        HttpURLConnection connection = null;
        try {
            URL url = new URL(cmsBaseUrl);
            connection = (HttpURLConnection) url.openConnection();
            connection.setConnectTimeout(10000);
            if (HTTP_OK == connection.getResponseCode()) {
                status = SelfTestBase.STATUS_OK;
            }
        } catch (IOException e) {
            LOGGER.info("<<<<<<<Error contacting CMS! " + e.getMessage());
        } finally {
            if (connection != null) {
                connection.disconnect();
            }
        }
        return new AvhengighetStatus("ENONIC_CMS", status, currentTimeMillis() - startTime, format("URL: %s", cmsBaseUrl));
    }
}