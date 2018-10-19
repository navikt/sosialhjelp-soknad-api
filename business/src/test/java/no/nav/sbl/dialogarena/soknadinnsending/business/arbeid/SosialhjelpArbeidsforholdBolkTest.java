package no.nav.sbl.dialogarena.soknadinnsending.business.arbeid;

import static org.hamcrest.Matchers.equalTo;
import static org.joda.time.Months.monthsBetween;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.Arrays;
import java.util.List;

import org.joda.time.DateTime;
import org.junit.Assert;
import org.junit.Test;

import no.nav.sbl.dialogarena.sendsoknad.domain.Faktum;
import no.nav.sbl.dialogarena.soknadinnsending.business.service.FaktaService;
import no.nav.sbl.dialogarena.soknadinnsending.consumer.ArbeidsforholdService;

public class SosialhjelpArbeidsforholdBolkTest {

    @Test
    public void testGetSoekeperiode() {
        final SosialhjelpArbeidsforholdBolk sosialhjelpArbeidsforholdBolk = new SosialhjelpArbeidsforholdBolk(
                mock(FaktaService.class), mock(ArbeidsforholdService.class));

        ArbeidsforholdService.Sokeperiode sokeperiode;

        sokeperiode = sosialhjelpArbeidsforholdBolk.getSoekeperiode();

        DateTime fom = sokeperiode.getFom();
        DateTime tom = sokeperiode.getTom();

        Assert.assertEquals(-3, monthsBetween(tom, fom).getMonths());
    }

    @Test
    public void skalBeOmSluttOppgjorHvisSluttdatoInnenforEnManedFremITid() throws Exception {
        final no.nav.sbl.dialogarena.sendsoknad.domain.Arbeidsforhold arbeidsforhold = new no.nav.sbl.dialogarena.sendsoknad.domain.Arbeidsforhold();
        arbeidsforhold.tom = new DateTime().plusMonths(1).toString("yyyy-MM-dd");

        final ArbeidsforholdService arbeidsforholdService = mock(ArbeidsforholdService.class);
        final SosialhjelpArbeidsforholdBolk sosialhjelpArbeidsforholdBolk = new SosialhjelpArbeidsforholdBolk(
                mock(FaktaService.class), arbeidsforholdService);

        when(arbeidsforholdService.hentArbeidsforhold(any(String.class), any(ArbeidsforholdService.Sokeperiode.class)))
                .thenReturn(Arrays.asList(arbeidsforhold));

        List<Faktum> faktums = sosialhjelpArbeidsforholdBolk.genererArbeidsforhold("123", 11L);
        Faktum faktum = faktums.get(1);

        assertThat(faktum.finnEgenskap("skalbeomsluttoppgjor").getValue(), equalTo("true"));
    }

    @Test
    public void skalIkkeBeOmSluttOppgjorHvisSluttdatoEtterEnManedFremITid() throws Exception {
        final no.nav.sbl.dialogarena.sendsoknad.domain.Arbeidsforhold arbeidsforhold = new no.nav.sbl.dialogarena.sendsoknad.domain.Arbeidsforhold();
        arbeidsforhold.tom = new DateTime().plusMonths(1).plusDays(1).toString("yyyy-MM-dd");

        final ArbeidsforholdService arbeidsforholdService = mock(ArbeidsforholdService.class);
        final SosialhjelpArbeidsforholdBolk sosialhjelpArbeidsforholdBolk = new SosialhjelpArbeidsforholdBolk(
                mock(FaktaService.class), arbeidsforholdService);

        when(arbeidsforholdService.hentArbeidsforhold(any(String.class), any(ArbeidsforholdService.Sokeperiode.class)))
                .thenReturn(Arrays.asList(arbeidsforhold));

        List<Faktum> faktums = sosialhjelpArbeidsforholdBolk.genererArbeidsforhold("123", 11L);
        Faktum faktum = faktums.get(1);

        assertThat(faktum.finnEgenskap("skalbeomsluttoppgjor").getValue(), equalTo("false"));
    }
}