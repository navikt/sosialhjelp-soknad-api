package no.nav.sbl.dialogarena.soknadinnsending.business.service;

import no.nav.sbl.dialogarena.soknadinnsending.business.util.*;
import org.joda.time.DateTime;
import org.joda.time.DateTimeUtils;
import org.junit.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;

public class StartDatoUtilTest {

    private StartDatoUtil startDatoUtil = new StartDatoUtil();

    private static final DateTime FORSTE_JANUAR = new DateTime(2014, 1, 1, 0, 1);
    private static final DateTime TJUEAATTENDE_FEBRUAR = new DateTime(2014, 2, 28, 23, 59);
    private static final DateTime TJUENIENDE_FEBRUAR = new DateTime(2012, 2, 29, 23, 59);
    private static final DateTime FORSTE_MARS = new DateTime(2014, 3, 1, 0, 1);
    private static final DateTime TRETTIFORSTE_DESEMBER = new DateTime(2014, 12, 31, 23, 59);

    @Test
    public void skalReturnereTrueDersomDetErForsteJanuar() {
        DateTimeUtils.setCurrentMillisFixed(FORSTE_JANUAR.getMillis());

        Boolean erJanuarEllerFebruar = startDatoUtil.erJanuarEllerFebruar();

        assertThat(erJanuarEllerFebruar, is(true));
    }

    @Test
    public void skalReturnereTrueDersomDetEr28Februar() {
        DateTimeUtils.setCurrentMillisFixed(TJUEAATTENDE_FEBRUAR.getMillis());

        Boolean erJanuarEllerFebruar = startDatoUtil.erJanuarEllerFebruar();

        assertThat(erJanuarEllerFebruar, is(true));
    }

    @Test
    public void skalReturnereTrueDersomDetEr29Februar() {
        DateTimeUtils.setCurrentMillisFixed(TJUENIENDE_FEBRUAR.getMillis());

        Boolean erJanuarEllerFebruar = startDatoUtil.erJanuarEllerFebruar();

        assertThat(erJanuarEllerFebruar, is(true));
    }

    @Test
    public void skalReturnereFalseDersomDetEr1Mars() {
        DateTimeUtils.setCurrentMillisFixed(FORSTE_MARS.getMillis());

        Boolean erJanuarEllerFebruar = startDatoUtil.erJanuarEllerFebruar();

        assertThat(erJanuarEllerFebruar, is(false));
    }

    @Test
    public void skalReturnereFalseDersomDet31DesemberMars() {
        DateTimeUtils.setCurrentMillisFixed(TRETTIFORSTE_DESEMBER.getMillis());

        Boolean erJanuarEllerFebruar = startDatoUtil.erJanuarEllerFebruar();

        assertThat(erJanuarEllerFebruar, is(false));
    }
}
