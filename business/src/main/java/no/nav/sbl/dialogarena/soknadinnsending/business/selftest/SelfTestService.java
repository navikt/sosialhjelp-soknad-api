package no.nav.sbl.dialogarena.soknadinnsending.business.selftest;

import no.aetat.arena.fodselsnr.Fodselsnr;
import no.nav.arena.tjenester.person.v1.PersonInfoServiceSoap;
import no.nav.tjeneste.domene.brukerdialog.fillager.v1.FilLagerPortType;
import no.nav.tjeneste.domene.brukerdialog.sendsoknad.v1.SendSoknadPortType;
import no.nav.tjeneste.virksomhet.arbeidsforhold.v3.binding.ArbeidsforholdV3;
import no.nav.tjeneste.virksomhet.brukerprofil.v1.BrukerprofilPortType;
import no.nav.tjeneste.virksomhet.kodeverk.v2.KodeverkPortType;
import no.nav.tjeneste.virksomhet.organisasjon.v4.binding.OrganisasjonV4;
import no.nav.tjeneste.virksomhet.person.v1.PersonPortType;
import org.slf4j.Logger;
import org.springframework.jdbc.core.JdbcTemplate;

import javax.inject.Inject;
import javax.inject.Named;
import javax.sql.DataSource;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.List;

import static java.lang.String.format;
import static java.lang.System.currentTimeMillis;
import static java.net.HttpURLConnection.HTTP_OK;
import static java.util.Arrays.asList;
import static org.slf4j.LoggerFactory.getLogger;

public class SelfTestService  {

    private static final Logger logger = getLogger(SelfTestService.class);
    public static final String STATUS_OK = "OK";
    public static final String STATUS_ERROR = "ERROR";

    @Inject
    @Named("sendSoknadSelftestEndpoint")
    private SendSoknadPortType cmsEndpoint;

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
    @Named("arbeidSelftestEndpoint")
    private ArbeidsforholdV3 arbeidsforhold;
    @Inject
    @Named("organisasjonSelftestEndpoint")
    private OrganisasjonV4 organisasjon;

    @Inject
    private PersonInfoServiceSoap personInfoServiceSoap;

    @Inject
    private DataSource dataSource;

    public List<AvhengighetStatus> hentStatusliste() {
        return asList(
                getCMSStatus(),
                getHenvendelseWSStatus(),
                getFillagerStatus(),
                getKodeverkStatus(),
                getPersonStatus(),
                getBrukerProfilStatus(),
                getPersonInfoStatus(),
                getArbeidtatus(),
                getOrganisasjonStatus(),
                getLokalDatabaseStatus()
        );
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
            logger.warn("CMS not reachable on " + url, e);
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
            cmsEndpoint.ping();
            status = STATUS_OK;
        } catch (Exception e) {
            logger.warn("<<<<<<Error Contacting Henvendelse WS", e);
        }
        String beskrivelse = "Web service for Henvendelse";
        return new AvhengighetStatus("HENVENDELSE_TJENESTE_PING", status, currentTimeMillis() - start, beskrivelse);
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
        String beskrivelse = "Web service for fillager (i Henvendelse)";
        return new AvhengighetStatus("FILLAGER_PING", status, currentTimeMillis() - start, beskrivelse);
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
        String beskrivelse = "Web service for TPS (Person-service)";
        return new AvhengighetStatus("TPS_PERSON_PING", status, currentTimeMillis() - start, beskrivelse);
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
        String beskrivelse = "Web service for Kodeverk" ;
        return new AvhengighetStatus("KODEVERK_PING", status, currentTimeMillis() - start, beskrivelse);
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
        String beskrivelse = "Web service for Brukerprofil";
        return new AvhengighetStatus("TPS_BRUKERPROFIL_PING", status, currentTimeMillis() - start, beskrivelse);
    }

    private AvhengighetStatus getPersonInfoStatus() {
        long start = currentTimeMillis();
        String status = STATUS_ERROR;
        Fodselsnr fodselsnr = new Fodselsnr().withFodselsnummer("01034128789");
        try {
            personInfoServiceSoap.hentPersonStatus(fodselsnr);
            status = STATUS_OK;
        } catch (Exception exception) {
            logger.warn("<<<<<<Error Contacting Personinfo (i  Arena) ", exception);
        }
        String beskrivelse = "Web service for Personinfo (i Arena)";
        return new AvhengighetStatus("ARENA_PERSONINFO_PING", status, currentTimeMillis() - start, beskrivelse);
    }
    private AvhengighetStatus getArbeidtatus() {
        long start = currentTimeMillis();
        String status = STATUS_ERROR;
        try {
            arbeidsforhold.ping();
            status = STATUS_OK;
        } catch (Exception exception) {
            logger.warn("<<<<<<Error Contacting Arbeidsforhold_v3", exception);
        }
        String beskrivelse = "Web service virksomhet:Arbeidsforhold_v3";
        return new AvhengighetStatus("Arbeidsforhold_v3", status, currentTimeMillis() - start, beskrivelse);
    }
    private AvhengighetStatus getOrganisasjonStatus() {
        long start = currentTimeMillis();
        String status = STATUS_ERROR;
        try {
            organisasjon.ping();
            status = STATUS_OK;
        } catch (Exception exception) {
            logger.warn("<<<<<<Error Contacting Organisasjon_v4", exception);
        }
        String beskrivelse = "Web service virksomhet:organisasjon_v4";
        return new AvhengighetStatus("Organisasjon_v4", status, currentTimeMillis() - start, beskrivelse);
    }

    private AvhengighetStatus getLokalDatabaseStatus() {
        long start = currentTimeMillis();

        String status = STATUS_ERROR;

        try {
            JdbcTemplate jdbcTemplate = new JdbcTemplate(dataSource);
            jdbcTemplate.queryForList("select * from dual");
            status = STATUS_OK;
        } catch (Exception e) {
            logger.warn("<<<<<<Error Contacting Local database", e);
        }
        String beskrivelse = "Lokal database";
        return new AvhengighetStatus("LOKAL_DATABASE_PING", status, currentTimeMillis() - start, beskrivelse);
    }

}
