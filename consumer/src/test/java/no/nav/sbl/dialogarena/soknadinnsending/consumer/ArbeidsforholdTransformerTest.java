package no.nav.sbl.dialogarena.soknadinnsending.consumer;

import no.nav.sbl.dialogarena.sendsoknad.domain.Arbeidsforhold;
import no.nav.sbl.dialogarena.soknadinnsending.consumer.organisasjon.OrganisasjonService;
import no.nav.sbl.dialogarena.soknadinnsending.consumer.organisasjon.dto.NavnDto;
import no.nav.sbl.dialogarena.soknadinnsending.consumer.organisasjon.dto.OrganisasjonNoekkelinfoDto;
import no.nav.tjeneste.virksomhet.arbeidsforhold.v3.informasjon.arbeidsforhold.*;
import no.nav.tjeneste.virksomhet.organisasjon.v4.binding.OrganisasjonV4;
import no.nav.tjeneste.virksomhet.organisasjon.v4.informasjon.UstrukturertNavn;
import no.nav.tjeneste.virksomhet.organisasjon.v4.meldinger.HentOrganisasjonRequest;
import no.nav.tjeneste.virksomhet.organisasjon.v4.meldinger.HentOrganisasjonResponse;
import org.joda.time.DateTime;
import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import javax.xml.datatype.DatatypeFactory;
import java.math.BigDecimal;

import static java.lang.System.getProperties;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class ArbeidsforholdTransformerTest {

    //todo: fjern webservice-avhengigheter på sikt

    public static final String ORGNUMMER = "1234567";

    @Mock
    private OrganisasjonV4 organisasjon;

    @Mock
    private OrganisasjonService organisasjonService;

    @InjectMocks
    private ArbeidsforholdTransformer arbeidsforholdTransformer;

    private DatatypeFactory datatypeFactory;

    @Before
    public void setup() throws Exception {
        datatypeFactory = DatatypeFactory.newInstance();
        when(organisasjon.hentOrganisasjon(any(HentOrganisasjonRequest.class))).thenReturn(createOrgResponse());
        when(organisasjonService.hentOrgNavn(anyString())).thenReturn(createOrgnoekkelinfoResponse());
    }

    @After
    public void tearDown() {
        getProperties().setProperty("EREG_API_ENABLED", "true");
    }

    private HentOrganisasjonResponse createOrgResponse() {
        HentOrganisasjonResponse response = new HentOrganisasjonResponse();
        no.nav.tjeneste.virksomhet.organisasjon.v4.informasjon.Organisasjon org = new no.nav.tjeneste.virksomhet.organisasjon.v4.informasjon.Organisasjon();
        UstrukturertNavn value = new UstrukturertNavn();
        value.getNavnelinje().add("Testesen A/S");
        value.getNavnelinje().add("andre linje");
        org.setNavn(value);
        response.setOrganisasjon(org);
        return response;
    }

    private String createOrgnoekkelinfoResponse() {
        return "Testesen A/S, andre linje";
    }

    @Test
    public void skalTransformereFastArbeidsforhold() {
        Arbeidsforhold arbeidsforhold = arbeidsforholdTransformer.transform(lagArbeidsforhold("fast"));
        assertThat(arbeidsforhold.harFastStilling, equalTo(true));
        assertThat(arbeidsforhold.fastStillingsprosent, equalTo(100L));
        assertThat(arbeidsforhold.fom, equalTo("2015-01-01"));
        assertThat(arbeidsforhold.tom, equalTo(null));
        assertThat(arbeidsforhold.arbeidsgivernavn, equalTo("Testesen A/S, andre linje"));
    }

    @Test
    public void skalIgnorereNullVerdierIOrgNavnWS() throws Exception {
        getProperties().setProperty("EREG_API_ENABLED", "false");
        when(organisasjon.hentOrganisasjon(any(HentOrganisasjonRequest.class))).thenReturn(createOrgResponseWithNulls());
        Arbeidsforhold arbeidsforhold = arbeidsforholdTransformer.transform(lagArbeidsforhold());
        assertThat(arbeidsforhold.arbeidsgivernavn, equalTo("Testesen A/S, andre linje"));
    }

    @Test
    public void skalIgnorereTommeStrengerIOrgNavnWS() throws Exception {
        getProperties().setProperty("EREG_API_ENABLED", "false");
        when(organisasjon.hentOrganisasjon(any(HentOrganisasjonRequest.class))).thenReturn(createOrgResponseWithEmptyStrings());
        Arbeidsforhold arbeidsforhold = arbeidsforholdTransformer.transform(lagArbeidsforhold());
        assertThat(arbeidsforhold.arbeidsgivernavn, equalTo("Testesen A/S, andre linje"));
    }

    @Ignore
    @Test
    public void skalTransformereVariabeltArbeidsforhold() {
        Arbeidsforhold result = arbeidsforholdTransformer.transform(lagArbeidsforhold("time"));
        assertThat(result.harFastStilling, equalTo(false));
        assertThat(result.fastStillingsprosent, equalTo(0L));
    }

    @Ignore
    @Test
    public void skalTransformereMixedArbeidsforhold() {
        Arbeidsforhold result = arbeidsforholdTransformer.transform(lagArbeidsforhold("time", "fast"));
        assertThat(result.harFastStilling, equalTo(true));
        assertThat(result.fastStillingsprosent, equalTo(100L));
    }

    private no.nav.tjeneste.virksomhet.arbeidsforhold.v3.informasjon.arbeidsforhold.Arbeidsforhold lagArbeidsforhold(String... stillingstyper) {
        no.nav.tjeneste.virksomhet.arbeidsforhold.v3.informasjon.arbeidsforhold.Arbeidsforhold arbeidsforhold = new no.nav.tjeneste.virksomhet.arbeidsforhold.v3.informasjon.arbeidsforhold.Arbeidsforhold();
        arbeidsforhold.setArbeidsgiver(lagOrganisasjon());
        arbeidsforhold.setAnsettelsesPeriode(lagAapenPeriode(new DateTime(2015, 1, 1, 0, 0)));
        arbeidsforhold.setArbeidsforholdIDnav(12345L);
        for (String stillingstype : stillingstyper) {
            arbeidsforhold.getArbeidsavtale().add(lagArbeidsavtale(stillingstype));
        }
        return arbeidsforhold;
    }

    private Arbeidsavtale lagArbeidsavtale(String stillingstype) {
        Arbeidsavtale arbeidsavtale = new Arbeidsavtale();
        Avloenningstyper value = new Avloenningstyper();
        value.setKodeRef(stillingstype);
        arbeidsavtale.setAvloenningstype(value);
        if ("fast".equals(stillingstype)) {
            arbeidsavtale.setStillingsprosent(new BigDecimal(100d));
        }
        return arbeidsavtale;
    }

    private AnsettelsesPeriode lagAapenPeriode(DateTime dateTime) {
        AnsettelsesPeriode ansettelsesPeriode = new AnsettelsesPeriode();
        ansettelsesPeriode.setPeriode(lagPeriode(dateTime, null));
        return ansettelsesPeriode;
    }

    private Gyldighetsperiode lagPeriode(DateTime fom, DateTime tom) {
        Gyldighetsperiode gyldighetsperiode = new Gyldighetsperiode();
        gyldighetsperiode.setFom(datatypeFactory.newXMLGregorianCalendar(fom.toGregorianCalendar()));
        if (tom != null) {
            gyldighetsperiode.setTom(datatypeFactory.newXMLGregorianCalendar(tom.toGregorianCalendar()));
        }
        return gyldighetsperiode;
    }

    private Aktoer lagOrganisasjon() {
        Organisasjon org = new Organisasjon();
        org.setOrgnummer(ORGNUMMER);
        return org;
    }

    private HentOrganisasjonResponse createOrgResponseWithNulls() {
        HentOrganisasjonResponse response = new HentOrganisasjonResponse();
        no.nav.tjeneste.virksomhet.organisasjon.v4.informasjon.Organisasjon org = new no.nav.tjeneste.virksomhet.organisasjon.v4.informasjon.Organisasjon();
        UstrukturertNavn value = new UstrukturertNavn();
        value.getNavnelinje().add("Testesen A/S");
        value.getNavnelinje().add("andre linje");
        value.getNavnelinje().add(null);
        value.getNavnelinje().add(null);
        org.setNavn(value);
        response.setOrganisasjon(org);
        return response;
    }

    private HentOrganisasjonResponse createOrgResponseWithEmptyStrings() {
        HentOrganisasjonResponse response = new HentOrganisasjonResponse();
        no.nav.tjeneste.virksomhet.organisasjon.v4.informasjon.Organisasjon org = new no.nav.tjeneste.virksomhet.organisasjon.v4.informasjon.Organisasjon();
        UstrukturertNavn value = new UstrukturertNavn();
        value.getNavnelinje().add("Testesen A/S");
        value.getNavnelinje().add("andre linje");
        value.getNavnelinje().add("");
        value.getNavnelinje().add("");
        org.setNavn(value);
        response.setOrganisasjon(org);
        return response;
    }

    private OrganisasjonNoekkelinfoDto createOrgNoekkelinfoResponseWithNulls() {
        NavnDto navn = new NavnDto("Testesen A/S", "andre linje", null, null, null);
        return new OrganisasjonNoekkelinfoDto(navn, ORGNUMMER);
    }

    private OrganisasjonNoekkelinfoDto createOrgNoekkelinfoResponseWithEmptyStrings() {
        NavnDto navn = new NavnDto("Testesen A/S", "andre linje", "", "", "");
        return new OrganisasjonNoekkelinfoDto(navn, ORGNUMMER);
    }
}