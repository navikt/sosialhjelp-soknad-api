package no.nav.sbl.dialogarena.soknadinnsending.business.arbeid;

import no.nav.sbl.dialogarena.soknadinnsending.business.domain.Faktum;
import no.nav.sbl.dialogarena.soknadinnsending.business.domain.dto.Land;
import no.nav.sbl.dialogarena.soknadinnsending.business.service.FaktaService;
import no.nav.tjeneste.virksomhet.arbeidsforhold.v3.binding.ArbeidsforholdV3;
import no.nav.tjeneste.virksomhet.arbeidsforhold.v3.binding.FinnArbeidsforholdPrArbeidstakerSikkerhetsbegrensning;
import no.nav.tjeneste.virksomhet.arbeidsforhold.v3.binding.FinnArbeidsforholdPrArbeidstakerUgyldigInput;
import no.nav.tjeneste.virksomhet.arbeidsforhold.v3.informasjon.arbeidsforhold.Arbeidsforhold;
import no.nav.tjeneste.virksomhet.arbeidsforhold.v3.meldinger.FinnArbeidsforholdPrArbeidstakerRequest;
import no.nav.tjeneste.virksomhet.arbeidsforhold.v3.meldinger.FinnArbeidsforholdPrArbeidstakerResponse;
import no.nav.tjeneste.virksomhet.organisasjon.v4.binding.OrganisasjonV4;
import org.joda.time.DateTime;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;

import java.util.List;

import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyLong;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class ArbeidsforholdServiceTest {


    @Mock
    ArbeidsforholdV3 arbeidsforholdWebService;
    @Mock
    private OrganisasjonV4 organisasjonWebService;
    @Mock
    private FaktaService faktaService;
    @Mock
    private ArbeidsforholdTransformer transformer;

    private String tom = new DateTime().toString("yyyy-MM-dd");
    private String fom = new DateTime().minusYears(1).toString("yyyy-MM-dd");
    private long soknadId = 11L;
    private long yrkesAktivFaktumId = 345L;



    @InjectMocks
    private ArbeidsforholdService service = new ArbeidsforholdService();

    @Test
    public void skalLagreSystemfakta() throws Exception {
        Arbeidsforhold arbeidsforhold = setup(lagArbeidsforhold());
        List<Faktum> faktums = service.genererArbeidsforhold("123", soknadId);
        Mockito.verify(arbeidsforholdWebService).finnArbeidsforholdPrArbeidstaker(any(FinnArbeidsforholdPrArbeidstakerRequest.class));
        verify(transformer).apply(arbeidsforhold);
        assertThat(faktums.size(), equalTo(1));
    }

    @Test
    public void skalSetteAlleFaktumFelter() throws Exception {
        no.nav.sbl.dialogarena.soknadinnsending.business.arbeid.Arbeidsforhold result = lagArbeidsforhold();
        setup(result);
        List<Faktum> faktums = service.genererArbeidsforhold("123", soknadId);
        Faktum faktum = faktums.get(0);

        assertThat(faktum.finnEgenskap("orgnr").getValue(), equalTo("12345"));
        assertThat(faktum.finnEgenskap("arbeidsgivernavn").getValue(), equalTo("test"));
        assertThat(faktum.finnEgenskap("fom").getValue(), equalTo(fom + ""));
        assertThat(faktum.finnEgenskap("tom").getValue(), equalTo(tom + ""));
        assertThat(faktum.finnEgenskap("land").getValue(), equalTo("NO"));
        assertThat(faktum.finnEgenskap("stillingstype").getValue(), equalTo("fast"));
        assertThat(faktum.finnEgenskap("stillingsprosent").getValue(), equalTo("50"));
        assertThat(faktum.finnEgenskap("kilde").getValue(), equalTo("EDAG"));
        assertThat(faktum.finnEgenskap("edagref").getValue(), equalTo("123"));

    }

    @Test
    public void skalSetteVariabel() throws Exception {
        no.nav.sbl.dialogarena.soknadinnsending.business.arbeid.Arbeidsforhold result = lagArbeidsforhold();
        result.harFastStilling = false;
        result.fastStillingsprosent = 0L;
        result.variabelStillingsprosent = true;
        setup(result);
        List<Faktum> faktums = service.genererArbeidsforhold("123", soknadId);
        Faktum faktum = faktums.get(0);

        assertThat(faktum.finnEgenskap("stillingstype").getValue(), equalTo("variabel"));
        assertThat(faktum.finnEgenskap("stillingsprosent").getValue(), equalTo("0"));
    }

    @Test
    public void skalSetteMixed() throws Exception {
        no.nav.sbl.dialogarena.soknadinnsending.business.arbeid.Arbeidsforhold result = lagArbeidsforhold();
        result.variabelStillingsprosent = true;
        setup(result);
        List<Faktum> faktums = service.genererArbeidsforhold("123", soknadId);
        Faktum faktum = faktums.get(0);

        assertThat(faktum.finnEgenskap("stillingstype").getValue(), equalTo("fastOgVariabel"));
        assertThat(faktum.finnEgenskap("stillingsprosent").getValue(), equalTo("50"));
    }

    @Test
    public void skalSettePagaende() throws Exception {
        no.nav.sbl.dialogarena.soknadinnsending.business.arbeid.Arbeidsforhold result = lagArbeidsforhold();
        result.tom = null;
        setup(result);
        List<Faktum> faktums = service.genererArbeidsforhold("123", soknadId);
        Faktum faktum = faktums.get(0);

        assertThat(faktum.finnEgenskap("ansatt").getValue(), equalTo("true"));
    }

    @Test
    public void arbeidsforholdSkalHaRiktigParrentFaktum() throws FinnArbeidsforholdPrArbeidstakerSikkerhetsbegrensning, FinnArbeidsforholdPrArbeidstakerUgyldigInput {
        no.nav.sbl.dialogarena.soknadinnsending.business.arbeid.Arbeidsforhold result = lagArbeidsforhold();
        setup(result);
        List<Faktum> faktums = service.genererArbeidsforhold("123", soknadId);
        Faktum faktum = faktums.get(0);

        assertThat(faktum.getParrentFaktum(), equalTo(yrkesAktivFaktumId));

    }
    private Arbeidsforhold setup(no.nav.sbl.dialogarena.soknadinnsending.business.arbeid.Arbeidsforhold result) throws FinnArbeidsforholdPrArbeidstakerSikkerhetsbegrensning, FinnArbeidsforholdPrArbeidstakerUgyldigInput {
        FinnArbeidsforholdPrArbeidstakerResponse t = new FinnArbeidsforholdPrArbeidstakerResponse();
        Arbeidsforhold arbeidsforhold = new Arbeidsforhold();
        t.getArbeidsforhold().add(arbeidsforhold);
        when(arbeidsforholdWebService.finnArbeidsforholdPrArbeidstaker(any(FinnArbeidsforholdPrArbeidstakerRequest.class))).thenReturn(t);
        when(transformer.apply(any(Arbeidsforhold.class))).thenReturn(result);
        when(faktaService.hentFaktumMedKey(anyLong(), eq("arbeidsforhold.yrkesaktiv"))).thenReturn(new Faktum().medKey("arbeidsforhold.yrkesaktiv").medValue("false").medFaktumId(yrkesAktivFaktumId));
        return arbeidsforhold;
    }

    private no.nav.sbl.dialogarena.soknadinnsending.business.arbeid.Arbeidsforhold lagArbeidsforhold() {
        no.nav.sbl.dialogarena.soknadinnsending.business.arbeid.Arbeidsforhold arbeidsforhold = new no.nav.sbl.dialogarena.soknadinnsending.business.arbeid.Arbeidsforhold();
        arbeidsforhold.orgnr = "12345";
        arbeidsforhold.arbridsgiverNavn = "test";
        arbeidsforhold.harFastStilling = true;
        arbeidsforhold.variabelStillingsprosent = false;
        arbeidsforhold.land = new Land("norge", "NO");
        arbeidsforhold.fastStillingsprosent = 50L;
        arbeidsforhold.edagId = 123L;
        arbeidsforhold.fom = fom;
        arbeidsforhold.tom = tom;


        return arbeidsforhold;
    }

}