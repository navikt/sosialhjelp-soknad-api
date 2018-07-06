package no.nav.sbl.dialogarena.soknadinnsending.business.person;

import no.nav.sbl.dialogarena.sendsoknad.domain.*;
import no.nav.sbl.dialogarena.sendsoknad.domain.personalia.Personalia;
import no.nav.sbl.dialogarena.sendsoknad.domain.personalia.PersonaliaBuilder;
import no.nav.sbl.dialogarena.soknadinnsending.consumer.personalia.PersonaliaFletter;
import org.joda.time.LocalDate;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.util.List;
import java.util.Map;

import static no.nav.sbl.dialogarena.sendsoknad.domain.Faktum.FaktumType.SYSTEMREGISTRERT;
import static no.nav.sbl.dialogarena.sendsoknad.domain.personalia.Personalia.*;
import static org.hamcrest.Matchers.nullValue;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;

@RunWith(value = MockitoJUnitRunner.class)
public class PersonaliaBolkTest {

    private static final Long SOKNADID = 12345L;

    private static final String FNR = "56128349974";
    private static final LocalDate FODSELSDATO = new LocalDate(1988, 5, 23);
    private static final String ALDER = "28";
    private static final String NAVN = "Ola Johan Normann";
    private static final String FORNAVN = "Ola";
    private static final String MELLOMNAVN = "Johan";
    private static final String ETTERNAVN = "Normann";
    private static final String EPOST = "test@epost.com";
    private static final String STATSBORGERSKAP = "DNK";
    private static final String KJONN = "m";
    private static final String KONTONUMMER = "123456789123";
    private static final String UTENLANDSK_BANK = "Utenlandsbanken";
    private static final String UTENLANDSKKONTO_LAND = "SWE";
    private static final String MOBIL = "98765432";

    private static final String FOLKEREGISTRERT_ADRESSE = "Folkeregistrert 1, 0560 Oslo";
    private static final String ADRESSETYPE_FOLKEREG = "Folkeregistrert";
    private static final String GJELDENDE_ADRESSE = "Annen Adresse 41B, 0560 Oslo";
    private static final String LANDKODE = "NOR";
    private static final String ADRESSETYPE_GJELDENDE = "Gjeldende";
    private static final String ADRESSE_GYLDIG_FRA = "2010-01-01";
    private static final String ADRESSE_GYLDIG_TIL = "2017-01-01";

    private static final String SIVILSTATUS_UGIFT = "ugift";
    private static final String SIVILSTATUS_GIFT = "gift";
    private static final String PARTNER_FNR = "01010091740";
    private static final String PARTNER_NAVN = "Kristin Partner";
    private static final LocalDate PARTNER_FODSELSDATO = new LocalDate(1987, 6, 23);
    private static final String PARTNER_FODSELSDATO_TEKST = "1987-06-23";

    private Adresse gjeldendeAdresse;
    private Adresse folkeregistrertAdresse;

    private Personalia personalia;

    @Mock
    PersonaliaFletter personaliaFletter;
    @InjectMocks
    PersonaliaBolk personaliaBolk;

    @Before
    public void setUp() {
        gjeldendeAdresse = new Adresse();
        gjeldendeAdresse.setAdresse(GJELDENDE_ADRESSE);
        gjeldendeAdresse.setLandkode(LANDKODE);
        gjeldendeAdresse.setGyldigFra(ADRESSE_GYLDIG_FRA);
        gjeldendeAdresse.setGyldigTil(ADRESSE_GYLDIG_TIL);
        gjeldendeAdresse.setAdressetype(ADRESSETYPE_GJELDENDE);

        folkeregistrertAdresse = new Adresse();
        folkeregistrertAdresse.setAdresse(FOLKEREGISTRERT_ADRESSE);
        folkeregistrertAdresse.setLandkode(LANDKODE);
        folkeregistrertAdresse.setAdressetype(ADRESSETYPE_FOLKEREG);

        personalia = PersonaliaBuilder.with()
                .fodselsnummer(FNR)
                .fodselsdato(FODSELSDATO)
                .alder(ALDER)
                .navn(NAVN)
                .withFornavn(FORNAVN)
                .withMellomnavn(MELLOMNAVN)
                .withEtternavn(ETTERNAVN)
                .epost(EPOST)
                .statsborgerskap(STATSBORGERSKAP)
                .kjonn(KJONN)
                .gjeldendeAdresse(gjeldendeAdresse)
                .sekundarAdresse(new Adresse())
                .folkeregistrertAdresse(folkeregistrertAdresse)
                .kontonummer(KONTONUMMER)
                .erUtenlandskBankkonto(true)
                .utenlandskKontoBanknavn(UTENLANDSK_BANK)
                .utenlandskKontoLand(UTENLANDSKKONTO_LAND)
                .mobiltelefon(MOBIL)
                .sivilstatus(SIVILSTATUS_UGIFT).build();
    }

    @Test
    public void genererPersonaliaFaktumLagerFaktumMedRiktigeVerdierForSokerUtenEktefelle() {
        List<Faktum> fakta = personaliaBolk.genererPersonaliaFaktum(SOKNADID, personalia);
        Faktum faktum = fakta.get(0);
        Map<String, String> faktumProperties = faktum.getProperties();

        assertThat(fakta.size(), is(2));
        assertThat(faktum.getKey(), is("personalia"));
        assertThat(faktum.getSoknadId(), is(SOKNADID));
        assertThat(faktumProperties.get(FNR_KEY), is(FNR));
        assertThat(faktumProperties.get(KONTONUMMER_KEY), is(KONTONUMMER));
        assertThat(faktumProperties.get(ER_UTENLANDSK_BANKKONTO), is("true"));
        assertThat(faktumProperties.get(UTENLANDSK_KONTO_BANKNAVN), is(UTENLANDSK_BANK));
        assertThat(faktumProperties.get(UTENLANDSK_KONTO_LAND), is(UTENLANDSKKONTO_LAND));
        assertThat(faktumProperties.get(ALDER_KEY), is(ALDER));
        assertThat(faktumProperties.get(NAVN_KEY), is(NAVN));
        assertThat(faktumProperties.get(FORNAVN_KEY), is(FORNAVN));
        assertThat(faktumProperties.get(MELLOMNAVN_KEY), is(MELLOMNAVN));
        assertThat(faktumProperties.get(ETTERNAVN_KEY), is(ETTERNAVN));
        assertThat(faktumProperties.get(EPOST_KEY), is(EPOST));
        assertThat(faktumProperties.get(STATSBORGERSKAP_KEY), is(STATSBORGERSKAP));
        assertThat(faktumProperties.get(STATSBORGERSKAPTYPE_KEY), is("eos"));
        assertThat(faktumProperties.get(KJONN_KEY), is(KJONN));
        assertThat(faktumProperties.get(FOLKEREGISTRERTADRESSE_KEY), is(FOLKEREGISTRERT_ADRESSE));
        assertThat(faktumProperties.get(GJELDENDEADRESSE_KEY), is(GJELDENDE_ADRESSE));
        assertThat(faktumProperties.get(DISKRESJONSKODE), nullValue());
        assertThat(faktumProperties.get(GJELDENDEADRESSE_TYPE_KEY), is(ADRESSETYPE_GJELDENDE));
        assertThat(faktumProperties.get(GJELDENDEADRESSE_GYLDIGFRA_KEY), is(ADRESSE_GYLDIG_FRA));
        assertThat(faktumProperties.get(GJELDENDEADRESSE_GYLDIGTIL_KEY), is(ADRESSE_GYLDIG_TIL));
        assertThat(faktumProperties.get(GJELDENDEADRESSE_LANDKODE), is(LANDKODE));
        assertThat(faktumProperties.get(SEKUNDARADRESSE_KEY), nullValue());
        assertThat(faktumProperties.get(SEKUNDARADRESSE_TYPE_KEY), nullValue());
        assertThat(faktumProperties.get(SEKUNDARADRESSE_GYLDIGFRA_KEY), nullValue());
        assertThat(faktumProperties.get(SEKUNDARADRESSE_GYLDIGTIL_KEY), nullValue());
        assertThat(fakta.get(1).getValue(), is(SIVILSTATUS_UGIFT));
    }

    @Test
    public void genererPersonaliaFaktumLagerFaktumForSivilstandForSokerMedEktefelle() {
        personalia.setSivilstatus(SIVILSTATUS_GIFT);
        personalia.setEktefelle(lagEktefelle());

        List<Faktum> fakta = personaliaBolk.genererPersonaliaFaktum(SOKNADID, personalia);

        assertThat(fakta.size(), is(3));
        assertThat(fakta.get(0).getKey(), is("personalia"));
        assertThat(fakta.get(1).getKey(), is("system.familie.sivilstatus"));
        assertThat(fakta.get(1).getValue(), is("gift"));
        assertThat(fakta.get(2).getKey(), is("system.familie.sivilstatus.gift.ektefelle"));
    }

    @Test
    public void genererSystemregistrertSivilstandFaktumLagerFaktumMedRiktigeVerdierForEktefelle() {
        personalia.setSivilstatus(SIVILSTATUS_GIFT);
        personalia.setEktefelle(lagEktefelle());

        Faktum faktum = personaliaBolk.genererSystemregistrertEktefelleFaktum(SOKNADID, personalia);
        Map<String, String> faktumProperties = faktum.getProperties();

        assertThat(faktum.getKey(), is("system.familie.sivilstatus.gift.ektefelle"));
        assertThat(faktum.getType(), is(SYSTEMREGISTRERT));
        assertThat(faktum.getSoknadId(), is(SOKNADID));
        assertThat(faktumProperties.get("navn"), is(PARTNER_NAVN));
        assertThat(faktumProperties.get("fodselsdato"), is(PARTNER_FODSELSDATO_TEKST));
        assertThat(faktumProperties.get("fnr"), is(PARTNER_FNR));
        assertThat(faktumProperties.get("folkeregistrertsammen"), is("true"));
        assertThat(faktumProperties.get("ikketilgangtilektefelle"), is("false"));
    }

    private Ektefelle lagEktefelle() {
        return new Ektefelle()
                .withFnr(PARTNER_FNR)
                .withNavn(PARTNER_NAVN)
                .withFodselsdato(PARTNER_FODSELSDATO)
                .withFolkeregistrertsammen(true)
                .withIkketilgangtilektefelle(false);
    }
}