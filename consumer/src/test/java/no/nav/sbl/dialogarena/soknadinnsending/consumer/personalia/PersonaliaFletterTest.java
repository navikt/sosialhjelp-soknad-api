package no.nav.sbl.dialogarena.soknadinnsending.consumer.personalia;

import no.nav.modig.core.exception.ApplicationException;
import no.nav.sbl.dialogarena.sendsoknad.domain.*;
import no.nav.sbl.dialogarena.sendsoknad.domain.personalia.Personalia;
import no.nav.sbl.dialogarena.soknadinnsending.consumer.exceptions.*;
import no.nav.sbl.dialogarena.soknadinnsending.consumer.kontaktinfo.BrukerprofilService;
import no.nav.sbl.dialogarena.soknadinnsending.consumer.kontaktinfo.EpostService;
import no.nav.sbl.dialogarena.soknadinnsending.consumer.person.PersonService;
import org.joda.time.LocalDate;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.nullValue;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.when;

@RunWith(value = MockitoJUnitRunner.class)
public class PersonaliaFletterTest {

    private static final String IDENT = "56128349974";
    private static final String EKTEFELLE_IDENT = "01010091736";

    private static final String ET_FORNAVN = "Ola";
    private static final String ET_MELLOMNAVN = "Johan";
    private static final String ET_ETTERNAVN = "Normann";
    private static final String ET_SAMMENSATT_NAVN = "Ola Johan Normann";

    private static final String ET_LAND = "Finland";
    private static final String EN_EPOST = "test@epost.com";

    private static final String ALDER = "33";
    private static final String KJONN = "m";
    private static final String SIVILSTATUS = "gift";
    private static final String STATSBORGERSKAP = "NOR";

    private static final String GJELDENDE_ADRESSE = "Grepalida 44A, 0101 Oslo";
    private static final String FOLKEREGISTRERT_ADRESSE = "Vegvegen 52, 3113 Sandefjord";
    private static final String SEKUNDAR_ADRESSE = "C/O Per Conradi, Veggaten 22C, 0565 Førde";
    private static final String KONTONUMMER = "0123456";
    private static final String UTENLANDSK_KONTO_BANKNAVN = "Banken";
    private static final String MOBILNUMMER = "98765432";

    @InjectMocks
    private PersonaliaFletter personaliaFletter;

    @Mock
    private PersonService personServiceMock;

    @Mock
    private BrukerprofilService brukerprofilServiceMock;

    @Mock
    private EpostService epostServiceMock;

    @Before
    public void setup() {
        when(personServiceMock.hentPerson(anyString())).thenReturn(lagPerson());
        when(brukerprofilServiceMock.hentKontaktinformasjonOgPreferanser(anyString())).thenReturn(lagAdresserOgKontonummer());
        when(epostServiceMock.hentInfoFraDKIF(anyString())).thenReturn(new DigitalKontaktinfo().withEpostadresse(EN_EPOST).withMobilnummer(MOBILNUMMER));
    }

    @Test
    public void mapTilPersonaliaAggregererInfoFraTjenesterForVanligBruker() {
        Personalia personalia = personaliaFletter.mapTilPersonalia(IDENT);

        assertThat(personalia.getFnr(), is(IDENT));
        assertThat(personalia.getFodselsdato(), notNullValue());
        assertThat(personalia.getAlder(), is(ALDER));
        assertThat(personalia.getDiskresjonskode(), nullValue());
        assertThat(personalia.getNavn(), is(ET_SAMMENSATT_NAVN));
        assertThat(personalia.getFornavn(), is(ET_FORNAVN));
        assertThat(personalia.getMellomnavn(), is(ET_MELLOMNAVN));
        assertThat(personalia.getEtternavn(), is(ET_ETTERNAVN));
        assertThat(personalia.getStatsborgerskap(), is(STATSBORGERSKAP));
        assertThat(personalia.getKjonn(), is(KJONN));
        assertThat(personalia.getSivilstatus(), is(SIVILSTATUS));
        assertThat(personalia.getEktefelle().getFnr(), is(EKTEFELLE_IDENT));
        assertThat(personalia.getEpost(), is(EN_EPOST));
        assertThat(personalia.getMobiltelefonnummer(), is(MOBILNUMMER));
        assertThat(personalia.getGjeldendeAdresse().getAdresse(), is(GJELDENDE_ADRESSE));
        assertThat(personalia.getSekundarAdresse().getAdresse(), is(SEKUNDAR_ADRESSE));
        assertThat(personalia.getFolkeregistrertAdresse().getAdresse(), is(FOLKEREGISTRERT_ADRESSE));
        assertThat(personalia.getKontonummer(), is(KONTONUMMER));
        assertThat(personalia.getErUtenlandskBankkonto(), is(true));
        assertThat(personalia.getUtenlandskKontoBanknavn(), is(UTENLANDSK_KONTO_BANKNAVN));
        assertThat(personalia.getUtenlandskKontoLand(), is(ET_LAND));
    }

    @Test(expected = ApplicationException.class)
    public void mapTilPersonaliaKasterApplicationExceptionHvisBrukerIkkeFinnesITPS() {
        when(personServiceMock.hentPerson(any(String.class))).thenThrow(new IkkeFunnetException("", new Exception()));

        personaliaFletter.mapTilPersonalia(IDENT);
    }

    @Test(expected = SikkerhetsBegrensningException.class)
    public void mapTilPersonaliaKasterSikkerhetsBegrensningExceptionHvisBrukerHarDiskresjonskode() {
        when(personServiceMock.hentPerson(any(String.class))).thenThrow(new SikkerhetsBegrensningException("", new Exception()));

        personaliaFletter.mapTilPersonalia(IDENT);
    }

    @Test(expected = TjenesteUtilgjengeligException.class)
    public void mapTilPersonaliaKasterTjenesteUtilgjengeligExceptionVedWebserviceFeilIPersonTjeneste() {
        when(personServiceMock.hentPerson(any(String.class))).thenThrow(new TjenesteUtilgjengeligException("", new Exception()));

        personaliaFletter.mapTilPersonalia(IDENT);
    }

    private Person lagPerson() {
        return new Person()
                .withFornavn(ET_FORNAVN)
                .withMellomnavn(ET_MELLOMNAVN)
                .withEtternavn(ET_ETTERNAVN)
                .withSammensattNavn(ET_SAMMENSATT_NAVN)
                .withFodselsdato(new LocalDate())
                .withFnr(IDENT)
                .withAlder(ALDER)
                .withStatsborgerskap(STATSBORGERSKAP)
                .withKjonn(KJONN)
                .withSivilstatus(SIVILSTATUS)
                .withEktefelle(new Ektefelle().withFnr(EKTEFELLE_IDENT));
    }

    private AdresserOgKontonummer lagAdresserOgKontonummer() {
        Adresse folkeregistrertAdresse = new Adresse();
        folkeregistrertAdresse.setAdresse(FOLKEREGISTRERT_ADRESSE);
        Adresse gjeldendeAdresse = new Adresse();
        gjeldendeAdresse.setAdresse(GJELDENDE_ADRESSE);
        Adresse sekundarAdresse = new Adresse();
        sekundarAdresse.setAdresse(SEKUNDAR_ADRESSE);
        return new AdresserOgKontonummer()
                .withFolkeregistrertAdresse(folkeregistrertAdresse)
                .withGjeldendeAdresse(gjeldendeAdresse)
                .withSekundarAdresse(sekundarAdresse)
                .withKontonummer(KONTONUMMER)
                .withUtenlandskBankkonto(true)
                .withUtenlandskKontoLand(ET_LAND)
                .withUtenlandskKontoBanknavn(UTENLANDSK_KONTO_BANKNAVN);
    }
}
