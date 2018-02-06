package no.nav.sbl.dialogarena.soknadinnsending.business.arbeid;

import no.nav.sbl.dialogarena.soknadinnsending.consumer.ArbeidsforholdService;
import org.joda.time.DateTime;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import static org.joda.time.Months.monthsBetween;

public class SosialhjelpArbeidsforholdBolkTest {

    private SosialhjelpArbeidsforholdBolk sosialhjelpArbeidsforholdBolk;

    @Before
    public void setOpp() {

        sosialhjelpArbeidsforholdBolk = new SosialhjelpArbeidsforholdBolk();
    }

    @Test
    public void testGetSoekeperiode() {
        ArbeidsforholdService.Sokeperiode sokeperiode;

        sokeperiode = sosialhjelpArbeidsforholdBolk.getSoekeperiode();

        DateTime fom = sokeperiode.getFom();
        DateTime tom = sokeperiode.getTom();

        Assert.assertEquals(-3, monthsBetween(tom, fom).getMonths());
    }
}