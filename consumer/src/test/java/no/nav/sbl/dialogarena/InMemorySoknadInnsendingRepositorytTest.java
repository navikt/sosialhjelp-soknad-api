package no.nav.sbl.dialogarena;

import no.nav.sbl.dialogarena.soknadinnsending.business.domain.Faktum;
import no.nav.sbl.dialogarena.soknadinnsending.business.domain.WebSoknad;
import org.junit.Before;
import org.junit.Test;

import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;


public class InMemorySoknadInnsendingRepositorytTest {

    private InMemorySoknadInnsendingRepository repo;
    private WebSoknad soknad;
    private Long soknadId;

    @Before
    public void setUp() {
        repo = new InMemorySoknadInnsendingRepository();
        soknadId = repo.startSoknad("Dagpenger");
        soknad = repo.hentSoknad(soknadId);

    }

    @Test
    public void skalKunneStarteOgHenteSoknad() {
        assertEquals(soknadId, soknad.getSoknadId());
        assertEquals(0, soknad.getFakta().size());
    }

    @Test
    public void skalKunneLeggeFaktaPaaSoknad() {
        soknad.leggTilFaktum("testFakta", new Faktum(soknadId, "testFakta", "testValue", null));

        Map<String, Faktum> fakta = soknad.getFakta();
        assertEquals(1, fakta.size());
        assertEquals("testValue", fakta.get("testFakta").getValue());
    }

    @Test
    public void skalKunneSletteSoknad() {
        repo.slettSoknad(soknadId);
        soknad = repo.hentSoknad(soknadId);

        assertNull(soknad);
    }
}