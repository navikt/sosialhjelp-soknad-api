package no.nav.sbl.dialogarena.soknadinnsending.consumer.arbeidsforhold;

import no.nav.sbl.dialogarena.sendsoknad.domain.Arbeidsforhold;
import no.nav.sbl.dialogarena.soknadinnsending.consumer.organisasjon.OrganisasjonService;
import no.nav.tjeneste.virksomhet.arbeidsforhold.v3.informasjon.arbeidsforhold.*;
import org.joda.time.DateTime;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import javax.xml.datatype.DatatypeFactory;
import java.math.BigDecimal;

import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class ArbeidsforholdTransformerTest {

    public static final String ORGNUMMER = "1234567";

    @Mock
    private OrganisasjonService organisasjonService;

    @InjectMocks
    private ArbeidsforholdTransformer arbeidsforholdTransformer;

    private DatatypeFactory datatypeFactory;

    private String orgnavn = "Testesen A/S, andre linje";

    @Before
    public void setup() throws Exception {
        datatypeFactory = DatatypeFactory.newInstance();
        when(organisasjonService.hentOrgNavn(anyString())).thenReturn(orgnavn);
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
}