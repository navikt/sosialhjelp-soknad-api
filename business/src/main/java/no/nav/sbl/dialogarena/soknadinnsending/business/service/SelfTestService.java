package no.nav.sbl.dialogarena.soknadinnsending.business.service;

import no.aetat.arena.fodselsnr.Fodselsnr;
import no.nav.arena.tjenester.person.v1.FaultGeneriskMsg;
import no.nav.arena.tjenester.person.v1.PersonInfoServiceSoap;
import no.nav.tjeneste.domene.brukerdialog.fillager.v1.FilLagerPortType;
import no.nav.tjeneste.domene.brukerdialog.sendsoknad.v1.SendSoknadPortType;
import no.nav.tjeneste.virksomhet.brukerprofil.v1.BrukerprofilPortType;
import no.nav.tjeneste.virksomhet.kodeverk.v2.KodeverkPortType;
import no.nav.tjeneste.virksomhet.person.v1.PersonPortType;
import org.joda.time.LocalDateTime;
import org.slf4j.Logger;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import javax.inject.Inject;
import javax.inject.Named;
import javax.sql.DataSource;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static java.lang.String.format;


import static java.lang.System.currentTimeMillis;
import static java.net.HttpURLConnection.HTTP_OK;
import static java.util.Arrays.asList;
import static org.slf4j.LoggerFactory.getLogger;

@Component
public class SelfTestService extends SelfTestBase {

    private static final Logger logger = getLogger(SelfTestService.class);

    @Inject
    @Named("sendSoknadSelftestEndpoint")
    private SendSoknadPortType CMSEndpoint;

    @Inject
    @Named("fillagerSelftestEndpoint")
    private FilLagerPortType fillagerEndpoint;

    @Inject
    @Named("personEndpoint")
    private PersonPortType personEndpoint;

    @Inject
    @Named("kodeverkSelftestEndpoint")
    private KodeverkPortType kodeverkEndpoint;

    @Inject
    @Named("brukerProfilEndpoint")
    private BrukerprofilPortType brukerProfilEndpoint;

    @Inject
    private PersonInfoServiceSoap personInfoServiceSoap;

    @Inject
    private DataSource dataSource;

    public SelfTestService() {
        this("Søknads api");
    }

    public SelfTestService(String applikasjonsNavn) {
        super(applikasjonsNavn);
    }

    public String getAsHTML() {
        this.replaceStatusList(statusList);
        html = new SelfTestHTML(applikasjonsNavn + " selftest");

        html.appendToBody("h1", "Service status: " + status);
        html.appendToBody("h3", applikasjonsNavn + " - " + version);
        html.appendToBody("h3", host);
        html.appendToBody(statusList);
        html.appendToBody("h5", "Siden generert: " + LocalDateTime.now().toString("yyyy-MM-dd HH:mm:ss"));

        html.appendToBody("h6", message);

        return html.buildPage();
    }

    private AvhengighetStatus getCMSStatus() {
        long start = currentTimeMillis();
        String status = STATUS_ERROR;
        String url = "";
        HttpURLConnection connection = null;
        try {
            url = System.getProperty("dialogarena.cms.url");
            connection = (HttpURLConnection) new URL(url).openConnection();
            connection.setConnectTimeout(10000);
            status = connection.getResponseCode() == HTTP_OK ? STATUS_OK : STATUS_ERROR;
        } catch (IOException e) {
            logger.warn("CMS not reachable on " + url , e);
        } finally {
            if (connection != null) {
                connection.disconnect();
            }
        }
        return new AvhengighetStatus("ENONIC_APPRES", status, currentTimeMillis() - start, format("URL: %s", url));
    }

    private AvhengighetStatus getHenvendelseWSStatus() {
        long start = currentTimeMillis();
        String status = STATUS_ERROR;
        try {
            CMSEndpoint.ping();
            status = STATUS_OK;
        } catch (Exception e) {
            logger.warn("<<<<<<Error Contacting Henvendelse WS", e);
        }
        return new AvhengighetStatus("HENVENDELSE_TJENESTE_PING", status, currentTimeMillis() - start);
    }

    private AvhengighetStatus getFillagerStatus() {
        long start = currentTimeMillis();
        String status = STATUS_ERROR;
        try {
            fillagerEndpoint.ping();
            status = STATUS_OK;
        } catch (Exception e) {
            logger.warn("<<<<<<Error Contacting Fillager (i Henvendelse)WS", e);
        }
        return new AvhengighetStatus("FILLAGER_PING", status, currentTimeMillis() - start);
    }

    private AvhengighetStatus getPersonStatus() {
        long start = currentTimeMillis();
        String status = STATUS_ERROR;
        try {
            personEndpoint.ping();
            status = STATUS_OK;
        } catch (Exception e) {
            logger.warn("<<<<<<Error Contacting TPS WS (Person-servicen)", e);
        }
        return new AvhengighetStatus("TPS_PERSON_PING", status, currentTimeMillis() - start);
    }

    private AvhengighetStatus getKodeverkStatus() {
        long start = currentTimeMillis();
        String status = STATUS_ERROR;
        try {
            kodeverkEndpoint.ping();
            status = STATUS_OK;
        } catch (Exception e) {
            logger.warn("<<<<<<Error Contacting Kodeverk WS", e);
        }
        return new AvhengighetStatus("KODEVERK_PING", status, currentTimeMillis() - start);
    }


    private AvhengighetStatus getBrukerProfilStatus() {
        long start = currentTimeMillis();
        String status = STATUS_ERROR;
        try {
            brukerProfilEndpoint.ping();
            status = STATUS_OK;
        } catch (Exception e) {
            logger.warn("<<<<<<Error Contacting Brukerprofil WS", e);
        }
        return new AvhengighetStatus("TPS_BRUKERPROFIL_PING", status, currentTimeMillis() - start);
    }

    private AvhengighetStatus getPersonInfoStatus() {
        long start = currentTimeMillis();
        String status = STATUS_ERROR;
        Fodselsnr fodselsnr = new Fodselsnr().withFodselsnummer("***REMOVED***");
        try {
            personInfoServiceSoap.hentPersonStatus(fodselsnr);
            status = STATUS_OK;
        } catch (FaultGeneriskMsg faultGeneriskMsg) {
            logger.warn("<<<<<<Error Contacting Arena WS", faultGeneriskMsg);
        }

        return new AvhengighetStatus("ARENA_PERSONINFO_PING", status, currentTimeMillis() - start);
    }

    private AvhengighetStatus getLokalDatabaseStatus() {
        long start = currentTimeMillis();

        String status = STATUS_ERROR;

        try {
            JdbcTemplate jdbcTemplate = new JdbcTemplate(dataSource);
            jdbcTemplate.queryForList("select * from dual");
            status = STATUS_OK;
        } catch (Exception e) {
            logger.warn("<<<<<<Error Contacting Arena WS", e);
        }

        return new AvhengighetStatus("LOKAL_DATABASE_PING", status, currentTimeMillis() - start);
    }

    protected void replaceStatusList(List<AvhengighetStatus> statusList) {
        statusList.clear();
        statusList.addAll(asList(
                getCMSStatus(),
                getHenvendelseWSStatus(),
                getFillagerStatus(),
                getKodeverkStatus(),
                getPersonStatus(),
                getBrukerProfilStatus(),
                getPersonInfoStatus(),
                getLokalDatabaseStatus()
        ));
    }

    public Map<String, Object> getAsJSON() {
        this.replaceStatusList(statusList);
        Map<String, Object> content = new HashMap<>();
        content.put("navn", applikasjonsNavn + "selftest");
        content.put("host", this.host);
        content.put("version", this.version);
        content.put("status", this.status);
        content.put("message", this.message);
        content.put("avhengigheter", this.statusList);

        return content;
    }


}
