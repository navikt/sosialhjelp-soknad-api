package no.nav.sbl.dialogarena.websoknad.selftest;

import no.aetat.arena.fodselsnr.Fodselsnr;
import no.nav.arena.tjenester.person.v1.FaultGeneriskMsg;
import no.nav.arena.tjenester.person.v1.PersonInfoServiceSoap;
import no.nav.modig.core.exception.SystemException;
import no.nav.modig.wicket.selftest.SelfTestBase;
import no.nav.sbl.dialogarena.soknadinnsending.business.db.SoknadInnsendingDBConfig;
import no.nav.sbl.dialogarena.soknadinnsending.consumer.ConsumerConfig;
import no.nav.tjeneste.domene.brukerdialog.fillager.v1.FilLagerPortType;
import no.nav.tjeneste.domene.brukerdialog.sendsoknad.v1.SendSoknadPortType;
import no.nav.tjeneste.virksomhet.brukerprofil.v1.BrukerprofilPortType;
import no.nav.tjeneste.virksomhet.kodeverk.v2.KodeverkPortType;
import no.nav.tjeneste.virksomhet.person.v1.PersonPortType;
import org.apache.wicket.request.cycle.RequestCycle;
import org.apache.wicket.request.http.WebRequest;
import org.apache.wicket.request.http.WebResponse;
import org.apache.wicket.request.mapper.parameter.PageParameters;
import org.slf4j.Logger;
import org.springframework.context.annotation.Import;
import org.springframework.jdbc.core.JdbcTemplate;

import javax.inject.Inject;
import javax.inject.Named;
import javax.servlet.http.Cookie;
import javax.sql.DataSource;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.List;

import static java.lang.String.format;
import static java.lang.System.currentTimeMillis;
import static java.net.HttpURLConnection.HTTP_OK;
import static org.slf4j.LoggerFactory.getLogger;

@Import({ConsumerConfig.WsServices.class, SoknadInnsendingDBConfig.class})
public class SelfTestPage extends SelfTestBase {
    private static final Logger logger = getLogger(SelfTestPage.class);

    @Inject
    @Named("sendSoknadSelftestEndpoint")
    private SendSoknadPortType sendSoknadSelftest;

    @Inject
    @Named("kodeverkSelftestEndpoint")
    private KodeverkPortType kodeverkServiceSelftest;

    @Inject
    @Named("brukerProfilEndpoint")
    private BrukerprofilPortType brukerProfilService;

    @Inject
    @Named("personEndpoint")
    private PersonPortType personService;

    @Inject
    @Named("fillagerSelftestEndpoint")
    private FilLagerPortType fillagerServiceSelftest;

    @Inject
    private PersonInfoServiceSoap personInfoServiceSoap;

    @Inject
    private DataSource dataSource;

    @Inject
    @Named(value = "cmsBaseUrl")
    private String cmsBaseUrl;

    public SelfTestPage(PageParameters params) {
        super("sendsoknad", params);
        WebRequest request = (WebRequest)RequestCycle.get().getRequest();

        Cookie cookie = request.getCookie("nav-esso");
        if (cookie == null) {
            cookie = new Cookie("nav-esso", "***REMOVED***-4");
            cookie.setPath("/sendsoknad");
            WebResponse response = (WebResponse)RequestCycle.get().getResponse();
            response.addCookie(cookie);
        }
    }

    @Override
    protected void addToStatusList(List<AvhengighetStatus> statusList) {
        new ServiceStatusHenter("HENVENDELSE_SENDSOKNAD") {
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

        new ServiceStatusHenter("TPS_HENT_PERSON") {
            public void ping() {
                personService.ping();
            }
        }.addStatus(statusList);

        new ServiceStatusHenter("FILLAGER") {
            public void ping() {
               fillagerServiceSelftest.ping();
            }
        }.addStatus(statusList);

        new ServiceStatusHenter("ARENA_PERSONINFO") {
            public void ping() {
                Fodselsnr fodselsnr = new Fodselsnr().withFodselsnummer("***REMOVED***");
                try {
                    personInfoServiceSoap.hentPersonStatus(fodselsnr);
                } catch (FaultGeneriskMsg faultGeneriskMsg) {
                    throw new SystemException("kall mot arena feilet", faultGeneriskMsg);
                }
            }
        }.addStatus(statusList);

        new ServiceStatusHenter("LOKAL_DATABASE") {
            public void ping() {
                try {
                    JdbcTemplate jdbcTemplate = new JdbcTemplate(dataSource);
                    jdbcTemplate.queryForList("select * from dual");
                } catch (Exception e) {
                    throw new SystemException("database feilet", e);
                }
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
            logger.info("<<<<<<<Error contacting CMS! " + e.getMessage());
        } finally {
            if (connection != null) {
                connection.disconnect();
            }
        }
        return new AvhengighetStatus("ENONIC_CMS", status, currentTimeMillis() - startTime, format("URL: %s", cmsBaseUrl));
    }
}