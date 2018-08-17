package no.nav.sbl.dialogarena.soknadinnsending.business.service.migrasjon;

import no.nav.sbl.dialogarena.sendsoknad.domain.Faktum;
import no.nav.sbl.dialogarena.sendsoknad.domain.WebSoknad;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.runners.MockitoJUnitRunner;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;

@RunWith(MockitoJUnitRunner.class)
public class MigrasjonHandtererTest {

    @InjectMocks
    private MigrasjonHandterer handterer;

    WebSoknad innsendtSoknad;

    @Before
    public void setup() {
        innsendtSoknad = new WebSoknad().medId(1L)
                .medFaktum(new Faktum().medKey("familie.sivilstatus.gift.ektefelle")
                        .medSystemProperty("navn", "Fornavn Mellomnavn Etternavn"));
    }

    @Test
    public void brukerregistrertNavnMigreringSkjerForSoknadSomIkkeErNull() {
        WebSoknad migrertSoknad = handterer.handterMigrasjon(innsendtSoknad);
        Faktum ektefelle = migrertSoknad.getFaktumMedKey("familie.sivilstatus.gift.ektefelle");

        assertThat(ektefelle.getProperties().get("fornavn"), is("Fornavn Mellomnavn"));
    }
}
