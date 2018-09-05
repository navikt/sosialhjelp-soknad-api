package no.nav.sbl.dialogarena.sendsoknad.domain.transformer.sosialhjelp.json;

import no.nav.sbl.dialogarena.sendsoknad.domain.Faktum;
import no.nav.sbl.dialogarena.sendsoknad.domain.WebSoknad;
import no.nav.sbl.soknadsosialhjelp.soknad.common.JsonKildeBruker;
import no.nav.sbl.soknadsosialhjelp.soknad.common.JsonKildeSystem;
import no.nav.sbl.soknadsosialhjelp.soknad.familie.*;
import org.junit.Test;

import static java.lang.String.valueOf;
import static no.nav.sbl.dialogarena.sendsoknad.domain.transformer.sosialhjelp.json.JsonFamilieConverter.faktumTilAnsvar;
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
    private static final String FNR = "03076512345";
    private static final String PERSONNUMMER = "12345";
    private static final int GRAD = 30;

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

    @Test
    public void faktumTilAnsvarSetterRiktigInformasjonForBarn() {
        JsonAnsvar jsonAnsvar = faktumTilAnsvar(lagBarnFaktum());
        JsonBarn jsonBarn = jsonAnsvar.getBarn();

        assertThat(jsonAnsvar.getBorSammenMed(), nullValue());
        assertThat(jsonAnsvar.getHarDeltBosted(), nullValue());
        assertThat(jsonAnsvar.getErFolkeregistrertSammen().getVerdi(), is(false));
        assertThat(jsonAnsvar.getErFolkeregistrertSammen().getKilde(), is(JsonKildeSystem.SYSTEM));
        assertThat(jsonAnsvar.getSamvarsgrad().getVerdi(), is(GRAD));
        assertThat(jsonAnsvar.getSamvarsgrad().getKilde(), is(JsonKildeBruker.BRUKER));

        assertThat(jsonBarn.getPersonIdentifikator(), is(FNR));
        assertThat(jsonBarn.getNavn().getFornavn(), is(FORNAVN));
        assertThat(jsonBarn.getNavn().getMellomnavn(), is(MELLOMNAVN));
        assertThat(jsonBarn.getNavn().getEtternavn(), is(ETTERNAVN));
        assertThat(jsonBarn.getFodselsdato(), is(FODSELSDATO_JSON));
        assertThat(jsonBarn.getHarDiskresjonskode(), is(false));
        assertThat(jsonBarn.getKilde(), is(SYSTEM));
    }

    @Test
    public void faktumTilAnsvarSetterRiktigInformasjonForBarnMedDiskresjonskode() {
        JsonAnsvar jsonAnsvar = faktumTilAnsvar(lagBarnMedDiskresjonskodeFaktum());
        JsonBarn jsonBarn = jsonAnsvar.getBarn();

        assertThat(jsonAnsvar.getBorSammenMed(), nullValue());
        assertThat(jsonAnsvar.getErFolkeregistrertSammen().getVerdi(), is(false));
        assertThat(jsonAnsvar.getErFolkeregistrertSammen().getKilde(), is(JsonKildeSystem.SYSTEM));
        assertThat(jsonAnsvar.getSamvarsgrad(), nullValue());

        assertThat(jsonBarn.getPersonIdentifikator(), nullValue());
        assertThat(jsonBarn.getNavn().getFornavn(), isEmptyString());
        assertThat(jsonBarn.getNavn().getMellomnavn(), isEmptyString());
        assertThat(jsonBarn.getNavn().getEtternavn(), isEmptyString());
        assertThat(jsonBarn.getFodselsdato(), nullValue());
        assertThat(jsonBarn.getHarDiskresjonskode(), is(true));
        assertThat(jsonBarn.getKilde(), is(SYSTEM));
    }

    @Test
    public void faktumTilAnsvarSetterRiktigInformasjonForBarnMedDeltBosted() {
        JsonAnsvar jsonAnsvar = faktumTilAnsvar(lagBarnMedSammeFolkeregistrertAdresseOgDeltBostedFaktum());

        assertThat(jsonAnsvar.getBorSammenMed(), nullValue());
        assertThat(jsonAnsvar.getSamvarsgrad(), nullValue());
        assertThat(jsonAnsvar.getErFolkeregistrertSammen().getVerdi(), is(true));
        assertThat(jsonAnsvar.getErFolkeregistrertSammen().getKilde(), is(JsonKildeSystem.SYSTEM));
        assertThat(jsonAnsvar.getHarDeltBosted().getVerdi(), is(true));
        assertThat(jsonAnsvar.getHarDeltBosted().getKilde(), is(JsonKildeBruker.BRUKER));
    }

    @Test
    public void tilJsonFodselsdatoEndrerIkkeDatoSomAlleredeErRiktigFormattert() {
        String jsonFodselsdato = JsonFamilieConverter.tilJsonFodselsdato(FODSELSDATO_JSON);

        assertThat(jsonFodselsdato, is(FODSELSDATO_JSON));
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
                .medSystemProperty("fornavn", "Skal aldri vises")
                .medSystemProperty("mellomnavn", "Skal aldri vises")
                .medSystemProperty("etternavn", "Skal aldri vises")
                .medSystemProperty("fodselsdato", "Skal aldri vises")
                .medSystemProperty("fnr", "Skal aldri vises")
                .medSystemProperty("folkeregistrertsammen", "false")
                .medSystemProperty("ikketilgangtilektefelle", "true");
    }

    private Faktum lagBarnFaktum() {
        return new Faktum().medKey("system.familie.barn.true.barn")
                .medUnikProperty("fnr")
                .medSystemProperty("fnr", FNR)
                .medSystemProperty("fornavn", FORNAVN)
                .medSystemProperty("mellomnavn", MELLOMNAVN)
                .medSystemProperty("etternavn", ETTERNAVN)
                .medSystemProperty("fodselsdato", FODSELSDATO)
                .medSystemProperty("ikketilgangtilbarn", "false")
                .medSystemProperty("folkeregistrertsammen", "false")
                .medSystemProperty("utvandret", "false")
                .medSystemProperty("grad", valueOf(GRAD));
    }

    private Faktum lagBarnMedSammeFolkeregistrertAdresseOgDeltBostedFaktum() {
        return new Faktum().medKey("system.familie.barn.true.barn")
                .medUnikProperty("fnr")
                .medSystemProperty("fnr", FNR)
                .medSystemProperty("fornavn", FORNAVN)
                .medSystemProperty("mellomnavn", null)
                .medSystemProperty("etternavn", ETTERNAVN)
                .medSystemProperty("fodselsdato", FODSELSDATO)
                .medSystemProperty("ikketilgangtilbarn", "false")
                .medSystemProperty("folkeregistrertsammen", "true")
                .medSystemProperty("utvandret", "false")
                .medSystemProperty("deltbosted", "true");
    }

    private Faktum lagBarnMedDiskresjonskodeFaktum() {
        return new Faktum().medKey("system.familie.barn.true.barn")
                .medUnikProperty("fnr")
                .medSystemProperty("fnr", "Skal aldri vises")
                .medSystemProperty("fornavn", "Skal aldri vises")
                .medSystemProperty("mellomnavn", "Skal aldri vises")
                .medSystemProperty("etternavn", "Skal aldri vises")
                .medSystemProperty("fodselsdato", "Skal aldri vises")
                .medSystemProperty("ikketilgangtilbarn", "true")
                .medSystemProperty("folkeregistrertsammen", "false")
                .medSystemProperty("utvandret", "false");
    }

}