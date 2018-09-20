package no.nav.sbl.dialogarena.soknadinnsending.business.db.fillager;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import no.nav.sbl.dialogarena.soknadinnsending.business.db.DbTestConfig;
import no.nav.sbl.soknadsosialhjelp.json.JsonSosialhjelpValidator;
import no.nav.sbl.soknadsosialhjelp.soknad.JsonInternalSoknad;
import no.nav.sbl.soknadsosialhjelp.soknad.internal.JsonTestInternal;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import javax.inject.Inject;
import java.io.*;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = {DbTestConfig.class})
public class FillagerRepositoryJdbcTest {

    private static final String BEHANDLINGSID = "114200003";
    private static final String UUID = "1";
    private static final String EIER = "***REMOVED***";
    private static final String TESTSTRENG = "nyTest";
    private static final String ANNENTESTSTRENG = "Ny annen teststreng";

    // Kommenteres inn for å kjøre mot reell database
    /*static {
        System.setProperty("no.nav.sbl.dialogarena.sendsoknad.hsqldb", "false");
    }*/

    @Inject
    private FillagerRepository fillagerRepository;

    private final ObjectMapper mapper = new ObjectMapper();
    private final ObjectWriter writer = mapper.writerWithDefaultPrettyPrinter();

    /**
     * Denne testen brukes for å undersøke om det er mulig å skrive en hel søknad på json-format til en blob i
     * databasen, hente søknaden opp igjen, endre på felter, og lagre søknaden igjen, og så hente den opp for å sjekke
     * at endringene er persistert. Testen kan også brukes for å sjekke hvor lang tid operasjonene tar. Testen bør
     * ryddes og skrives om når vi kommer lenger med å erstatte dagens faktummodell og lagringsmekanisme.
     *
     **/
    @Test(timeout = 4000)
    public void lagreSoknadLagrerSoknadenIDatabasen() throws IOException {
        final String jsonSoknad = hentSoknadFraFilSomString();
        fillagerRepository.lagreFil(opprettFil(jsonSoknad));

        final long starttid = System.currentTimeMillis();
        final FillagerRepository.Fil hentetFil = fillagerRepository.hentFil(UUID);

        System.out.println("Hent fil fra DB tar (ms): " + (System.currentTimeMillis() - starttid));
        final JsonInternalSoknad hentetSoknadFraDB = mapper.readValue(hentetFil.data, JsonInternalSoknad.class);
        hentetSoknadFraDB.setTestinternal(new JsonTestInternal().withTestStreng(TESTSTRENG).withAnnenTestStreng(ANNENTESTSTRENG));

        System.out.println("Hent fil fra DB og endre felter på objekt tar (ms): " + (System.currentTimeMillis() - starttid));
        final String jsonSoknadOppdatert = writer.writeValueAsString(hentetSoknadFraDB);
        JsonSosialhjelpValidator.ensureValidInternalSoknad(jsonSoknadOppdatert);

        System.out.println("Hent fil fra DB, endre felter på objekt og skrive til json tar (ms): "
                + (System.currentTimeMillis() - starttid));
        final FillagerRepository.Fil oppdatertFil = opprettFil(jsonSoknadOppdatert);
        fillagerRepository.lagreFil(oppdatertFil);

        System.out.println("Hent fil fra DB, endre felter på objekt, skrive til json og lagre i DB tar (ms): "
                + (System.currentTimeMillis() - starttid));
        final FillagerRepository.Fil hentetOppdatertFilFraDB = fillagerRepository.hentFil(UUID);
        final JsonInternalSoknad opphentetOppdatertSoknadFraDB = mapper.readValue(hentetOppdatertFilFraDB.data,
                JsonInternalSoknad.class);

        assertThat(opphentetOppdatertSoknadFraDB.getTestinternal().getTestStreng(), is(TESTSTRENG));
        assertThat(opphentetOppdatertSoknadFraDB.getTestinternal().getAnnenTestStreng(), is(ANNENTESTSTRENG));
    }

    /**
     * Her kan man fint bare lese filen direkte i stedet for å konvertere til JsonInternalSoknad og så skrive tilbake
     * til json med validering, men det er sånn nå for å være sikker på at filen vi leser er i henhold til skjemaet for
     * den interne søknaden.
     **/

    private String hentSoknadFraFilSomString() throws IOException {
        InputStreamReader reader = new InputStreamReader(this.getClass().getResourceAsStream("/eksempelsoknad.json"), "UTF-8");
        final JsonInternalSoknad internalSoknad = mapper.readValue(reader, JsonInternalSoknad.class);
        final String jsonSoknad = writer.writeValueAsString(internalSoknad);
        JsonSosialhjelpValidator.ensureValidInternalSoknad(jsonSoknad);
        return jsonSoknad;
    }

    private FillagerRepository.Fil opprettFil(String jsonSoknad) throws UnsupportedEncodingException {
        return new FillagerRepository.Fil(BEHANDLINGSID, UUID, jsonSoknad.getBytes("UTF-8"), EIER);
    }
}