package no.nav.sbl.dialogarena.websoknad.selftest;

import no.nav.modig.wicket.selftest.SelfTestBase;
import no.nav.sbl.dialogarena.websoknad.config.ConsumerConfig;
import no.nav.tjeneste.domene.brukerdialog.henvendelsesbehandling.v1.HenvendelsesBehandlingPortType;
import no.nav.tjeneste.virksomhet.brukerprofil.v1.BrukerprofilPortType;
import org.apache.wicket.request.mapper.parameter.PageParameters;
import org.slf4j.Logger;
import org.springframework.context.annotation.Import;

import javax.inject.Inject;
import javax.inject.Named;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.Socket;
import java.net.URL;
import java.util.List;

import static java.lang.String.format;
import static java.lang.System.currentTimeMillis;
import static java.lang.System.getProperty;
import static java.net.HttpURLConnection.HTTP_OK;
import static org.slf4j.LoggerFactory.getLogger;

@Import({ConsumerConfig.SelfTestStsConfig.class})
public class SelfTestPage extends SelfTestBase {
    private static final Logger LOGGER = getLogger(SelfTestPage.class);

    @Inject
    private HenvendelsesBehandlingPortType henvendelsesBehandlingPortType;

    @Inject
    private BrukerprofilPortType brukerprofilPortType;

    /*@Inject
    private SendSoknadPortType sendSoknadPortType;*/

    @Inject
    @Named(value = "cmsBaseUrl")
    private String cmsBaseUrl;

    public SelfTestPage(PageParameters params) {
        super("sendsoknad", params);
    }

    @Override
    protected void addToStatusList(List<AvhengighetStatus> statusList) {
        new ServiceStatusHenter("HENVENDELSEBEHANDLING") {
            public void ping() {
                henvendelsesBehandlingPortType.ping();
            }
        }.addStatus(statusList);

        new ServiceStatusHenter("TPS_HENT_BRUKERPROFIL") {
            public void ping() {
                brukerprofilPortType.ping();
            }
        }.addStatus(statusList);

        /*new ServiceStatusHenter("SENDSOKNAD") {
            public void ping() {
                sendSoknadPortType.ping();
            }
        }.addStatus(statusList);*/

        statusList.add(smtpStatus());
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
            LOGGER.info("<<<<<<<<Error contacting CMS! " + e.getMessage());
        } finally {
            if (connection != null) {
                connection.disconnect();
            }
        }
        return new AvhengighetStatus("ENONIC_CMS", status, currentTimeMillis() - startTime, format("URL: %s", cmsBaseUrl));
    }

    private AvhengighetStatus smtpStatus() {
        String host = getProperty("dokumentinnsending.smtpServer.host");
        String port = getProperty("dokumentinnsending.smtpServer.port");

        long startTime = currentTimeMillis();
        String status = SelfTestBase.STATUS_ERROR;

        try {
            Socket s = new Socket(host, Integer.valueOf(port));
            if (s.isConnected()) {
                s.close();
                status = SelfTestBase.STATUS_OK;
            }
        } catch (Exception e) {
            LOGGER.info("<<<<<<<<Error contacting SMTP! " + e.getMessage());
        }

        return new AvhengighetStatus("SMTP_SERVER", status, currentTimeMillis() - startTime);
    }
}