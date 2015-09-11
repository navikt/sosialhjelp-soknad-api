package no.nav.sbl.dialogarena.soknadinnsending.business.arbeid;

import no.nav.tjeneste.virksomhet.arbeidsforhold.v3.informasjon.arbeidsforhold.Aktoer;
import no.nav.tjeneste.virksomhet.arbeidsforhold.v3.informasjon.arbeidsforhold.AnsettelsesPeriode;
import no.nav.tjeneste.virksomhet.arbeidsforhold.v3.informasjon.arbeidsforhold.Arbeidsavtale;
import no.nav.tjeneste.virksomhet.arbeidsforhold.v3.informasjon.arbeidsforhold.Avloenningstyper;
import no.nav.tjeneste.virksomhet.arbeidsforhold.v3.informasjon.arbeidsforhold.Gyldighetsperiode;
import no.nav.tjeneste.virksomhet.arbeidsforhold.v3.informasjon.arbeidsforhold.Organisasjon;
import no.nav.tjeneste.virksomhet.organisasjon.v4.binding.OrganisasjonV4;
import no.nav.tjeneste.virksomhet.organisasjon.v4.informasjon.UstrukturertNavn;
import no.nav.tjeneste.virksomhet.organisasjon.v4.meldinger.HentOrganisasjonRequest;
import no.nav.tjeneste.virksomhet.organisasjon.v4.meldinger.HentOrganisasjonResponse;
import org.joda.time.DateTime;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import javax.xml.datatype.DatatypeFactory;
import java.math.BigDecimal;

import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class ArbeidsforholdTransformerTest {

    public static final String ORGNUMMER = "1234567";
    @Mock
    private OrganisasjonV4 organisasjon;
    private ArbeidsforholdTransformer transformer;
    private DatatypeFactory datatypeFactory;

    @Before
    public void setup() throws Exception {
        transformer = new ArbeidsforholdTransformer(organisasjon);
        datatypeFactory = DatatypeFactory.newInstance();
        when(organisasjon.hentOrganisasjon(any(HentOrganisasjonRequest.class))).thenReturn(createOrgResponse());
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

    @Test
    public void skalTransformereFastArbeidsforhold() {
        Arbeidsforhold result = transformer.transform(lagArbeidsforhold("fast"));
        assertThat(result.harFastStilling, equalTo(true));
        assertThat(result.fastStillingsprosent, equalTo(100L));
        assertThat(result.variabelStillingsprosent, equalTo(false));
        assertThat(result.fom, equalTo("2015-01-01"));
        assertThat(result.tom, equalTo(null));
        assertThat(result.arbridsgiverNavn, equalTo("Testesen A/S, andre linje"));
    }

    @Test
    public void skalTransformereVariabeltArbeidsforhold() {
        Arbeidsforhold result = transformer.transform(lagArbeidsforhold("time"));
        assertThat(result.harFastStilling, equalTo(false));
        assertThat(result.fastStillingsprosent, equalTo(0L));
        assertThat(result.variabelStillingsprosent, equalTo(true));
    }

    @Test
    public void skalTransformereMixedArbeidsforhold() {
        Arbeidsforhold result = transformer.transform(lagArbeidsforhold("time", "fast"));
        assertThat(result.harFastStilling, equalTo(true));
        assertThat(result.fastStillingsprosent, equalTo(100L));
        assertThat(result.variabelStillingsprosent, equalTo(true));
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

}