package no.nav.sbl.dialogarena.soknadinnsending.business.arbeid;

import no.nav.sbl.dialogarena.sendsoknad.domain.Arbeidsforhold;
import no.nav.sbl.dialogarena.sendsoknad.domain.Faktum;
import no.nav.sbl.dialogarena.soknadinnsending.business.service.FaktaService;
import no.nav.sbl.dialogarena.soknadinnsending.consumer.ArbeidsforholdService;
import org.junit.Assert;
import org.junit.Test;

import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.Arrays;
import java.util.List;

import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class SosialhjelpArbeidsforholdBolkTest {

    @Test
    public void testGetSoekeperiode() {
        SosialhjelpArbeidsforholdBolk sosialhjelpArbeidsforholdBolk = new SosialhjelpArbeidsforholdBolk(
                mock(FaktaService.class), mock(ArbeidsforholdService.class));

        ArbeidsforholdService.Sokeperiode sokeperiode;

        sokeperiode = sosialhjelpArbeidsforholdBolk.getSoekeperiode();

        OffsetDateTime fom = sokeperiode.getFom();
        OffsetDateTime tom = sokeperiode.getTom();

        Assert.assertEquals(-3, ChronoUnit.MONTHS.between(tom, fom));
    }

    @Test
    public void skalBeOmSluttOppgjorHvisSluttdatoInnenforEnManedFremITid() {
        Arbeidsforhold arbeidsforhold = new Arbeidsforhold();
        arbeidsforhold.tom = OffsetDateTime.now().plusMonths(1).format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));

        ArbeidsforholdService arbeidsforholdService = mock(ArbeidsforholdService.class);
        SosialhjelpArbeidsforholdBolk sosialhjelpArbeidsforholdBolk = new SosialhjelpArbeidsforholdBolk(
                mock(FaktaService.class), arbeidsforholdService);

        when(arbeidsforholdService.hentArbeidsforhold(any(String.class), any(ArbeidsforholdService.Sokeperiode.class)))
                .thenReturn(Arrays.asList(arbeidsforhold));

        List<Faktum> faktums = sosialhjelpArbeidsforholdBolk.genererArbeidsforhold("123", 11L);
        Faktum faktum = faktums.get(1);

        assertThat(faktum.finnEgenskap("skalbeomsluttoppgjor").getValue(), equalTo("true"));
    }

    @Test
    public void skalIkkeBeOmSluttOppgjorHvisSluttdatoEtterEnManedFremITid() {
        Arbeidsforhold arbeidsforhold = new Arbeidsforhold();
        arbeidsforhold.tom = OffsetDateTime.now().plusMonths(1).plusDays(1).format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));

        ArbeidsforholdService arbeidsforholdService = mock(ArbeidsforholdService.class);
        SosialhjelpArbeidsforholdBolk sosialhjelpArbeidsforholdBolk = new SosialhjelpArbeidsforholdBolk(
                mock(FaktaService.class), arbeidsforholdService);

        when(arbeidsforholdService.hentArbeidsforhold(any(String.class), any(ArbeidsforholdService.Sokeperiode.class)))
                .thenReturn(Arrays.asList(arbeidsforhold));

        List<Faktum> faktums = sosialhjelpArbeidsforholdBolk.genererArbeidsforhold("123", 11L);
        Faktum faktum = faktums.get(1);

        assertThat(faktum.finnEgenskap("skalbeomsluttoppgjor").getValue(), equalTo("false"));
    }
}