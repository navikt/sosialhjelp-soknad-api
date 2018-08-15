package no.nav.sbl.dialogarena.soknadinnsending.business.service.migrasjon;

import no.nav.sbl.dialogarena.sendsoknad.domain.Faktum;
import no.nav.sbl.dialogarena.sendsoknad.domain.WebSoknad;
import org.junit.Before;
import org.junit.Test;

import java.util.List;
import java.util.Map;

import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.Matchers.isEmptyString;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;

public class BrukerregistrertNavnMigrasjonTest {

    private static final int FRA_VERSJON = 1;
    private static final int TIL_VERSJON = 2;
    private static final String NAVN = "Fornavn Mellomnavn Etternavn";
    private static final String NAVN_2 = "Fornavn2 Mellomnavn2 Etternavn2";
    private static final String NAVN_3 = "Fornavn3 Mellomnavn3 Etternavn3";
    private static final String SAMMENSATT_FORNAVN = "Fornavn Mellomnavn";
    private static final String SAMMENSATT_FORNAVN_2 = "Fornavn2 Mellomnavn2";
    private static final String SAMMENSATT_FORNAVN_3 = "Fornavn3 Mellomnavn3";
    private static final String FORNAVN = "Fornavn";
    private static final String MELLOMNAVN = "Mellomnavn";
    private static final String ETTERNAVN = "Etternavn";
    private static final String FORNAVN_2 = "Fornavn2";
    private static final String MELLOMNAVN_2 = "Mellomnavn2";
    private static final String ETTERNAVN_2 = "Etternavn2";
    private static final String ETTERNAVN_3 = "Etternavn3";
    private final BrukerregistrertNavnMigrasjon migrasjon = new BrukerregistrertNavnMigrasjon();

    private WebSoknad soknad;

    @Before
    public void setUp() {
        soknad = new WebSoknad().medId(1L).medVersjon(FRA_VERSJON);
    }

    @Test
    public void migrerOppdatererKunVersjonForIkkeMigrertSoknadUtenEktefelleEllerBarn() {
        WebSoknad migrertSoknad = migrasjon.migrer(FRA_VERSJON, soknad);

        assertThat(migrertSoknad.getVersjon(), is(TIL_VERSJON));
        assertThat(migrertSoknad.getFaktumMedKey("familie.sivilstatus.gift.ektefelle"), nullValue());
        assertThat(migrertSoknad.getFaktumMedKey("familie.barn.true.barn"), nullValue());
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

    @Test
    public void ikkeMigrertSoknadMedBarnMedNavnPaGammeltFormatMigreresRiktig() {
        soknad.medFaktum(lagBarnFaktumMedNavnPaGammeltFormat(NAVN));

        WebSoknad migrertSoknad = migrasjon.migrer(FRA_VERSJON, soknad);
        Map<String, String> migrertBarnProperties = migrertSoknad.getFaktumMedKey("familie.barn.true.barn").getProperties();

        assertThat(migrertSoknad.getVersjon(), is(TIL_VERSJON));
        assertThat(migrertBarnProperties.get("navn"), is(NAVN));
        assertThat(migrertBarnProperties.get("fornavn"), is(SAMMENSATT_FORNAVN));
        assertThat(migrertBarnProperties.get("mellomnavn"), isEmptyString());
        assertThat(migrertBarnProperties.get("etternavn"), is(ETTERNAVN));
    }

    @Test
    public void ikkeMigrertSoknadMedEktefelleOgToBarnMedNavnPaGammeltFormatMigreresRiktig() {
        soknad.medFaktum(lagEktefelleFaktumMedNavnPaGammeltFormat())
                .medFaktum(lagBarnFaktumMedNavnPaGammeltFormat(NAVN_2))
                .medFaktum(lagBarnFaktumMedNavnPaGammeltFormat(NAVN_3));

        WebSoknad migrertSoknad = migrasjon.migrer(FRA_VERSJON, soknad);
        Map<String, String> migrertEktefelleProperties = migrertSoknad.getFaktumMedKey("familie.sivilstatus.gift.ektefelle").getProperties();
        List<Faktum> migrertBarnFakta = migrertSoknad.getFaktaMedKey("familie.barn.true.barn");
        Map<String, String> migrertBarnProperties = migrertBarnFakta.get(0).getProperties();
        Map<String, String> migrertBarn2Properties = migrertBarnFakta.get(1).getProperties();

        assertThat(migrertSoknad.getVersjon(), is(TIL_VERSJON));
        assertThat(migrertEktefelleProperties.get("navn"), is(NAVN));
        assertThat(migrertEktefelleProperties.get("fornavn"), is(SAMMENSATT_FORNAVN));
        assertThat(migrertEktefelleProperties.get("mellomnavn"), isEmptyString());
        assertThat(migrertEktefelleProperties.get("etternavn"), is(ETTERNAVN));

        assertThat(migrertBarnFakta.size(), is(2));
        assertThat(migrertBarnProperties.get("navn"), is(NAVN_2));
        assertThat(migrertBarnProperties.get("fornavn"), is(SAMMENSATT_FORNAVN_2));
        assertThat(migrertBarnProperties.get("mellomnavn"), isEmptyString());
        assertThat(migrertBarnProperties.get("etternavn"), is(ETTERNAVN_2));

        assertThat(migrertBarn2Properties.get("navn"), is(NAVN_3));
        assertThat(migrertBarn2Properties.get("fornavn"), is(SAMMENSATT_FORNAVN_3));
        assertThat(migrertBarn2Properties.get("mellomnavn"), isEmptyString());
        assertThat(migrertBarn2Properties.get("etternavn"), is(ETTERNAVN_3));
    }

    @Test
    public void ikkeMigrertSoknadMedEktefelleMedNavnPaGammeltFormatOgBarnMedNavnPaNyttFormatMigreresRiktig() {
        soknad.medFaktum(lagEktefelleFaktumMedNavnPaGammeltFormat())
                .medFaktum(lagBarnFaktumMedNavnPaNyttFormat());

        WebSoknad migrertSoknad = migrasjon.migrer(FRA_VERSJON, soknad);
        Map<String, String> migrertEktefelleProperties = migrertSoknad.getFaktumMedKey("familie.sivilstatus.gift.ektefelle").getProperties();
        List<Faktum> migrertBarnFakta = migrertSoknad.getFaktaMedKey("familie.barn.true.barn");
        Map<String, String> migrertBarnProperties = migrertBarnFakta.get(0).getProperties();

        assertThat(migrertSoknad.getVersjon(), is(TIL_VERSJON));
        assertThat(migrertEktefelleProperties.get("navn"), is(NAVN));
        assertThat(migrertEktefelleProperties.get("fornavn"), is(SAMMENSATT_FORNAVN));
        assertThat(migrertEktefelleProperties.get("mellomnavn"), isEmptyString());
        assertThat(migrertEktefelleProperties.get("etternavn"), is(ETTERNAVN));

        assertThat(migrertBarnFakta.size(), is(1));
        assertThat(migrertBarnProperties.get("navn"), nullValue());
        assertThat(migrertBarnProperties.get("fornavn"), is(FORNAVN_2));
        assertThat(migrertBarnProperties.get("mellomnavn"), is(MELLOMNAVN_2));
        assertThat(migrertBarnProperties.get("etternavn"), is(ETTERNAVN_2));
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

    private Faktum lagBarnFaktumMedNavnPaGammeltFormat(String navn) {
        return new Faktum().medKey("familie.barn.true.barn")
                .medSystemProperty("navn", navn);
    }

    private Faktum lagBarnFaktumMedNavnPaNyttFormat() {
        return new Faktum().medKey("familie.barn.true.barn")
                .medSystemProperty("fornavn", FORNAVN_2)
                .medSystemProperty("mellomnavn", MELLOMNAVN_2)
                .medSystemProperty("etternavn", ETTERNAVN_2);
    }
}