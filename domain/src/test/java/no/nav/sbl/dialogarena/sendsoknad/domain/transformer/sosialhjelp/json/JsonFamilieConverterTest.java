package no.nav.sbl.dialogarena.sendsoknad.domain.transformer.sosialhjelp.json;

import no.nav.sbl.dialogarena.sendsoknad.domain.Faktum;
import no.nav.sbl.dialogarena.sendsoknad.domain.WebSoknad;
import no.nav.sbl.soknadsosialhjelp.soknad.familie.JsonEktefelle;
import no.nav.sbl.soknadsosialhjelp.soknad.familie.JsonSivilstatus;
import org.junit.Test;

import static no.nav.sbl.soknadsosialhjelp.soknad.common.JsonKilde.BRUKER;
import static no.nav.sbl.soknadsosialhjelp.soknad.common.JsonKilde.SYSTEM;
import static org.hamcrest.Matchers.isEmptyString;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsNull.nullValue;
import static org.junit.Assert.assertThat;

public class JsonFamilieConverterTest {

    private static final String NAVN = "Fornavn Etternavn";
    private static final String ETTERNAVN = "Etternavn";
    private static final String MELLOMNAVN = "Mellomnavn";
    private static final String FORNAVN = "Fornavn";
    private static final String FODSELSDATO = "03071965";
    private static final String FODSELSDATO_JSON = "1965-07-03";
    private static final String FNR = "***REMOVED***";
    private static final String PERSONNUMMER = "12345";

    @Test
    public void tilJsonSivilstatusReturnererNullHvisBrukerregistrertOgSystemregistrertSivilstatusMangler() {
        final WebSoknad webSoknad = new WebSoknad().medFaktum(new Faktum().medKey("ikke.sivilstatus").medValue("Noe annet"));

        JsonSivilstatus jsonSivilstatus = JsonFamilieConverter.tilJsonSivilstatus(webSoknad);

        assertThat(jsonSivilstatus, nullValue());
    }

    @Test
    public void tilJsonSivilstatusBrukerSystemregistrertSivilstatusHvisBrukerregistrertMangler() {
        final WebSoknad webSoknad = new WebSoknad().medFaktum(new Faktum().medKey("system.familie.sivilstatus").medValue("gift"));

        JsonSivilstatus jsonSivilstatus = JsonFamilieConverter.tilJsonSivilstatus(webSoknad);

        assertThat(jsonSivilstatus.getKilde(), is(SYSTEM));
    }

    @Test
    public void tilJsonSivilstatusBrukerBrukerregistrertSivilstatusHvisSystemregistrertManglerOgBrukerregistrertFinnes() {
        final WebSoknad webSoknad = new WebSoknad().medFaktum(new Faktum().medKey("familie.sivilstatus").medValue("gift"));

        JsonSivilstatus jsonSivilstatus = JsonFamilieConverter.tilJsonSivilstatus(webSoknad);

        assertThat(jsonSivilstatus.getKilde(), is(BRUKER));
    }

    @Test
    public void tilJsonSivilstatusBrukerBrukerregistrertSivilstatusHvisBrukeregistrertOgSystemregistrertSivilstatusFinnes() {
        final WebSoknad webSoknad = new WebSoknad()
                .medFaktum(new Faktum().medKey("familie.sivilstatus").medValue("gift"))
                .medFaktum(new Faktum().medKey("system.familie.sivilstatus").medValue("ugift"));

        JsonSivilstatus jsonSivilstatus = JsonFamilieConverter.tilJsonSivilstatus(webSoknad);

        assertThat(jsonSivilstatus.getKilde(), is(BRUKER));
    }

    @Test
    public void tilJsonSivilstatusSetterRiktigInformasjonForSystemregistrertEktefelle() {
        final WebSoknad webSoknad = new WebSoknad()
                .medFaktum(new Faktum().medKey("system.familie.sivilstatus").medValue("gift"))
                .medFaktum(lagEktefelleFaktum());

        JsonSivilstatus jsonSivilstatus = JsonFamilieConverter.tilJsonSivilstatus(webSoknad);
        JsonEktefelle jsonEktefelle = jsonSivilstatus.getEktefelle();

        assertThat(jsonEktefelle.getNavn().getEtternavn(), is(ETTERNAVN));
        assertThat(jsonEktefelle.getNavn().getFornavn(), is(FORNAVN));
        assertThat(jsonEktefelle.getNavn().getMellomnavn(), is(MELLOMNAVN));
        assertThat(jsonEktefelle.getFodselsdato(), is(FODSELSDATO_JSON));
        assertThat(jsonEktefelle.getPersonIdentifikator(), is(FNR));
        assertThat(jsonSivilstatus.getStatus().value(), is("gift"));
        assertThat(jsonSivilstatus.getFolkeregistrertMedEktefelle(), is(true));
        assertThat(jsonSivilstatus.getEktefelleHarDiskresjonskode(), is(false));
        assertThat(jsonSivilstatus.getBorIkkeSammenMedBegrunnelse(), nullValue());
    }

    @Test
    public void tilJsonSivilstatusSetterRiktigInformasjonForBrukerregistrertEktefelleMedGammelModell() {
        final WebSoknad webSoknad = new WebSoknad()
                .medFaktum(new Faktum().medKey("familie.sivilstatus").medValue("gift"))
                .medFaktum(lagBrukerregistrertEktefelleFaktumMedGammelModell());

        JsonSivilstatus jsonSivilstatus = JsonFamilieConverter.tilJsonSivilstatus(webSoknad);
        JsonEktefelle jsonEktefelle = jsonSivilstatus.getEktefelle();

        assertThat(jsonEktefelle.getNavn().getEtternavn(), is(ETTERNAVN));
        assertThat(jsonEktefelle.getNavn().getFornavn(), is(FORNAVN));
        assertThat(jsonEktefelle.getNavn().getMellomnavn(), isEmptyString());
        assertThat(jsonEktefelle.getFodselsdato(), is(FODSELSDATO_JSON));
        assertThat(jsonEktefelle.getPersonIdentifikator(), is(FNR));
        assertThat(jsonSivilstatus.getStatus().value(), is("gift"));
        assertThat(jsonSivilstatus.getBorSammenMed(), is(false));
        assertThat(jsonSivilstatus.getEktefelleHarDiskresjonskode(), nullValue());
        assertThat(jsonSivilstatus.getBorIkkeSammenMedBegrunnelse(), notNullValue());
    }

    @Test
    public void tilJsonSivilstatusFlaggerAtEktefelleHarDiskesjonskodeOgSenderIkkePersoninfo() {
        final WebSoknad webSoknad = new WebSoknad()
                .medFaktum(new Faktum().medKey("system.familie.sivilstatus").medValue("gift"))
                .medFaktum(lagEktefelleMedDiskresjonskodeFaktum());

        JsonSivilstatus jsonSivilstatus = JsonFamilieConverter.tilJsonSivilstatus(webSoknad);
        JsonEktefelle jsonEktefelle = jsonSivilstatus.getEktefelle();

        assertThat(jsonEktefelle.getNavn().getEtternavn(), isEmptyString());
        assertThat(jsonEktefelle.getNavn().getFornavn(), isEmptyString());
        assertThat(jsonEktefelle.getFodselsdato(), nullValue());
        assertThat(jsonEktefelle.getPersonIdentifikator(), nullValue());
        assertThat(jsonSivilstatus.getStatus().value(), is("gift"));
        assertThat(jsonSivilstatus.getFolkeregistrertMedEktefelle(), is(false));
        assertThat(jsonSivilstatus.getEktefelleHarDiskresjonskode(), is(true));
    }

    private Faktum lagEktefelleFaktum() {
        return new Faktum().medKey("system.familie.sivilstatus.gift.ektefelle")
                .medSystemProperty("fornavn", FORNAVN)
                .medSystemProperty("mellomnavn", MELLOMNAVN)
                .medSystemProperty("etternavn", ETTERNAVN)
                .medSystemProperty("fodselsdato", FODSELSDATO)
                .medSystemProperty("fnr", FNR)
                .medSystemProperty("folkeregistrertsammen", "true")
                .medSystemProperty("ikketilgangtilektefelle", "false");
    }

    private Faktum lagBrukerregistrertEktefelleFaktumMedGammelModell() {
        return new Faktum().medKey("familie.sivilstatus.gift.ektefelle")
                .medSystemProperty("navn", NAVN)
                .medSystemProperty("fnr", FODSELSDATO)
                .medSystemProperty("pnr", PERSONNUMMER)
                .medSystemProperty("borsammen", "false")
                .medSystemProperty("ikkesammenbeskrivelse", "Bor ikke sammen");
    }

    private Faktum lagEktefelleMedDiskresjonskodeFaktum() {
        return new Faktum().medKey("system.familie.sivilstatus.gift.ektefelle")
                .medSystemProperty("fornavn", "")
                .medSystemProperty("mellomnavn", null)
                .medSystemProperty("etternavn", null)
                .medSystemProperty("fodselsdato", null)
                .medSystemProperty("fnr", null)
                .medSystemProperty("folkeregistrertsammen", "false")
                .medSystemProperty("ikketilgangtilektefelle", "true");
    }

}