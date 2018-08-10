package no.nav.sbl.dialogarena.soknadinnsending.business.service.migrasjon;

import no.nav.sbl.dialogarena.sendsoknad.domain.Faktum;
import no.nav.sbl.dialogarena.sendsoknad.domain.WebSoknad;
import org.junit.Before;
import org.junit.Test;

import java.util.Map;

import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.Matchers.isEmptyString;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.*;

public class BrukerregistrertEktefelleNavnMigrasjonTest {

    private static final int FRA_VERSJON = 1;
    private static final int TIL_VERSJON = 2;
    private static final String NAVN = "Fornavn Mellomnavn Etternavn";
    private static final String SAMMENSATT_FORNAVN = "Fornavn Mellomnavn";
    private static final String FORNAVN = "Fornavn";
    private static final String MELLOMNAVN = "Mellomnavn";
    private static final String ETTERNAVN = "Etternavn";
    private final BrukerregistrertEktefelleNavnMigrasjon migrasjon = new BrukerregistrertEktefelleNavnMigrasjon();

    private WebSoknad soknad;

    @Before
    public void setUp() {
        soknad = new WebSoknad().medId(1L).medVersjon(FRA_VERSJON);
    }

    @Test
    public void migrerOppdatererKunVersjonForIkkeMigrertSoknadUtenEktefelle() {
        WebSoknad migrertSoknad = migrasjon.migrer(FRA_VERSJON, soknad);

        assertThat(migrertSoknad.getVersjon(), is(TIL_VERSJON));
        assertThat(migrertSoknad.getFaktumMedKey("familie.sivilstatus.gift.ektefelle"), nullValue());
    }

    @Test
    public void migrerOppdatererKunVersjonForIkkeMigrertSoknadMedNavnPaNyttFormat() {
        soknad.medFaktum(lagEktefelleFaktumMedNavnPaNyttFormat());

        WebSoknad migrertSoknad = migrasjon.migrer(FRA_VERSJON, soknad);
        Map<String, String> migrertEktefelleProperties = migrertSoknad.getFaktumMedKey("familie.sivilstatus.gift.ektefelle").getProperties();

        assertThat(migrertSoknad.getVersjon(), is(TIL_VERSJON));
        assertThat(migrertEktefelleProperties.get("navn"), nullValue());
        assertThat(migrertEktefelleProperties.get("fornavn"), is(FORNAVN));
        assertThat(migrertEktefelleProperties.get("mellomnavn"), is(MELLOMNAVN));
        assertThat(migrertEktefelleProperties.get("etternavn"), is(ETTERNAVN));
    }

    @Test
    public void ikkeMigrertSoknadMedEktefelleMedNavnPaGammeltFormatMigreresRiktig() {
        soknad.medFaktum(lagEktefelleFaktumMedNavnPaGammeltFormat());

        WebSoknad migrertSoknad = migrasjon.migrer(FRA_VERSJON, soknad);
        Map<String, String> migrertEktefelleProperties = migrertSoknad.getFaktumMedKey("familie.sivilstatus.gift.ektefelle").getProperties();

        assertThat(migrertSoknad.getVersjon(), is(TIL_VERSJON));
        assertThat(migrertEktefelleProperties.get("navn"), is(NAVN));
        assertThat(migrertEktefelleProperties.get("fornavn"), is(SAMMENSATT_FORNAVN));
        assertThat(migrertEktefelleProperties.get("mellomnavn"), isEmptyString());
        assertThat(migrertEktefelleProperties.get("etternavn"), is(ETTERNAVN));
    }

    private Faktum lagEktefelleFaktumMedNavnPaGammeltFormat() {
        return new Faktum().medKey("familie.sivilstatus.gift.ektefelle")
                .medSystemProperty("navn", NAVN);

    }

    private Faktum lagEktefelleFaktumMedNavnPaNyttFormat() {
        return new Faktum().medKey("familie.sivilstatus.gift.ektefelle")
                .medSystemProperty("fornavn", FORNAVN)
                .medSystemProperty("mellomnavn", MELLOMNAVN)
                .medSystemProperty("etternavn", ETTERNAVN);
    }
}