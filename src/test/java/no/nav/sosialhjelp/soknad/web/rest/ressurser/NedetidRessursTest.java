package no.nav.sosialhjelp.soknad.web.rest.ressurser;

import no.nav.sosialhjelp.soknad.web.utils.NedetidUtils;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.junit.MockitoJUnitRunner;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

@RunWith(MockitoJUnitRunner.class)
public class NedetidRessursTest {

    @InjectMocks
    NedetidRessurs nedetidRessurs;

    @Before
    public void setUp() {
        System.clearProperty(NedetidUtils.NEDETID_START);
        System.clearProperty(NedetidUtils.NEDETID_SLUTT);
    }

    // Utenfor planlagt nedetid eller nedetid:

    @Test
    public void whenNedetidIsNull_ShouldReturnNullAndFalse() {
        NedetidRessurs.NedetidFrontend nedetidFrontend = nedetidRessurs.hentNedetidInformasjon();
        assertThat(nedetidFrontend.isNedetid).isFalse();
        assertThat(nedetidFrontend.isPlanlagtNedetid).isFalse();
        assertThat(nedetidFrontend.nedetidSlutt).isNull();
        assertThat(nedetidFrontend.nedetidStart).isNull();

        System.setProperty(NedetidUtils.NEDETID_START, LocalDateTime.now().format(NedetidUtils.dateTimeFormatter));
        nedetidFrontend = nedetidRessurs.hentNedetidInformasjon();
        assertThat(nedetidFrontend.isNedetid).isFalse();
        assertThat(nedetidFrontend.isPlanlagtNedetid).isFalse();
        assertThat(nedetidFrontend.nedetidSlutt).isNull();

        System.clearProperty(NedetidUtils.NEDETID_START);
        System.setProperty(NedetidUtils.NEDETID_SLUTT, LocalDateTime.now().format(NedetidUtils.dateTimeFormatter));
        nedetidFrontend = nedetidRessurs.hentNedetidInformasjon();
        assertThat(nedetidFrontend.isNedetid).isFalse();
        assertThat(nedetidFrontend.isPlanlagtNedetid).isFalse();
        assertThat(nedetidFrontend.nedetidStart).isNull();
    }

    @Test
    public void whenNedetidStarterOm15dager_ShouldReturnFalseAndNull() {
        LocalDateTime nedetidStart = LocalDateTime.now().plusDays(15);
        LocalDateTime nedetidSlutt = LocalDateTime.now().plusDays(20);
        System.setProperty(NedetidUtils.NEDETID_START, nedetidStart.format(NedetidUtils.dateTimeFormatter));
        System.setProperty(NedetidUtils.NEDETID_SLUTT, nedetidSlutt.format(NedetidUtils.dateTimeFormatter));

        NedetidRessurs.NedetidFrontend nedetidFrontend = nedetidRessurs.hentNedetidInformasjon();
        assertThat(nedetidFrontend.isNedetid).isFalse();
        assertThat(nedetidFrontend.isPlanlagtNedetid).isFalse();
        assertThat(nedetidFrontend.nedetidSlutt).isNotNull();
        assertThat(nedetidFrontend.nedetidStart).isNotNull();
    }

    @Test
    public void whenNedetidStarterOm14dagerAnd1min_ShouldReturnFalseAndNull() {
        LocalDateTime nedetidStart = LocalDateTime.now().plusDays(14).plusMinutes(1);
        LocalDateTime nedetidSlutt = LocalDateTime.now().plusDays(20);
        System.setProperty(NedetidUtils.NEDETID_START, nedetidStart.format(NedetidUtils.dateTimeFormatter));
        System.setProperty(NedetidUtils.NEDETID_SLUTT, nedetidSlutt.format(NedetidUtils.dateTimeFormatter));

        NedetidRessurs.NedetidFrontend nedetidFrontend = nedetidRessurs.hentNedetidInformasjon();
        assertThat(nedetidFrontend.isNedetid).isFalse();
        assertThat(nedetidFrontend.isPlanlagtNedetid).isFalse();
        assertThat(nedetidFrontend.nedetidSlutt).isNotNull();
        assertThat(nedetidFrontend.nedetidStart).isNotNull();
    }


    // Innenfor planlagt nedetid:

    @Test
    public void whenNedetidStarterOm12dager_ShouldReturnPlanlagtNedetid() {
        LocalDateTime nedetidStart = LocalDateTime.now().plusDays(12);
        LocalDateTime nedetidSlutt = LocalDateTime.now().plusDays(20);
        System.setProperty(NedetidUtils.NEDETID_START, nedetidStart.format(NedetidUtils.dateTimeFormatter));
        System.setProperty(NedetidUtils.NEDETID_SLUTT, nedetidSlutt.format(NedetidUtils.dateTimeFormatter));

        NedetidRessurs.NedetidFrontend nedetidFrontend = nedetidRessurs.hentNedetidInformasjon();
        assertThat(nedetidFrontend.isNedetid).isFalse();
        assertThat(nedetidFrontend.isPlanlagtNedetid).isTrue();
        assertThat(nedetidFrontend.nedetidSlutt).isNotNull();
        assertThat(nedetidFrontend.nedetidStart).isNotNull();
    }

    @Test
    public void whenNedetidStarterOm14dagerMinus1min_ShouldReturnPlanlagtNedetid() {
        LocalDateTime nedetidStart = LocalDateTime.now().plusDays(14).minusMinutes(1);
        LocalDateTime nedetidSlutt = LocalDateTime.now().plusDays(20);
        System.setProperty(NedetidUtils.NEDETID_START, nedetidStart.format(NedetidUtils.dateTimeFormatter));
        System.setProperty(NedetidUtils.NEDETID_SLUTT, nedetidSlutt.format(NedetidUtils.dateTimeFormatter));

        NedetidRessurs.NedetidFrontend nedetidFrontend = nedetidRessurs.hentNedetidInformasjon();
        assertThat(nedetidFrontend.isNedetid).isFalse();
        assertThat(nedetidFrontend.isPlanlagtNedetid).isTrue();
        assertThat(nedetidFrontend.nedetidSlutt).isNotNull();
        assertThat(nedetidFrontend.nedetidStart).isNotNull();
    }

    // Innenfor nedetid:

    @Test
    public void whenNedetidStartetfor1sekSiden_ShouldReturnNedetid() {
        LocalDateTime nedetidStart = LocalDateTime.now().minusSeconds(1);
        LocalDateTime nedetidSlutt = LocalDateTime.now().plusDays(20);
        System.setProperty(NedetidUtils.NEDETID_START, nedetidStart.format(NedetidUtils.dateTimeFormatter));
        System.setProperty(NedetidUtils.NEDETID_SLUTT, nedetidSlutt.format(NedetidUtils.dateTimeFormatter));

        NedetidRessurs.NedetidFrontend nedetidFrontend = nedetidRessurs.hentNedetidInformasjon();
        assertThat(nedetidFrontend.isNedetid).isTrue();
        assertThat(nedetidFrontend.isPlanlagtNedetid).isFalse();
        assertThat(nedetidFrontend.nedetidSlutt).isNotNull();
        assertThat(nedetidFrontend.nedetidStart).isNotNull();
    }

    @Test
    public void whenMidtINedetid_ShouldReturnNedetid() {
        LocalDateTime nedetidStart = LocalDateTime.now().minusDays(5);
        LocalDateTime nedetidSlutt = LocalDateTime.now().plusDays(5);
        System.setProperty(NedetidUtils.NEDETID_START, nedetidStart.format(NedetidUtils.dateTimeFormatter));
        System.setProperty(NedetidUtils.NEDETID_SLUTT, nedetidSlutt.format(NedetidUtils.dateTimeFormatter));

        NedetidRessurs.NedetidFrontend nedetidFrontend = nedetidRessurs.hentNedetidInformasjon();
        assertThat(nedetidFrontend.isNedetid).isTrue();
        assertThat(nedetidFrontend.isPlanlagtNedetid).isFalse();
        assertThat(nedetidFrontend.nedetidSlutt).isNotNull();
        assertThat(nedetidFrontend.nedetidStart).isNotNull();
    }

    @Test
    public void whenNedetidSlutterOm1min_ShouldReturnNedetid() {
        LocalDateTime nedetidStart = LocalDateTime.now().minusDays(2);
        LocalDateTime nedetidSlutt = LocalDateTime.now().plusMinutes(1);
        System.setProperty(NedetidUtils.NEDETID_START, nedetidStart.format(NedetidUtils.dateTimeFormatter));
        System.setProperty(NedetidUtils.NEDETID_SLUTT, nedetidSlutt.format(NedetidUtils.dateTimeFormatter));

        NedetidRessurs.NedetidFrontend nedetidFrontend = nedetidRessurs.hentNedetidInformasjon();
        assertThat(nedetidFrontend.isNedetid).isTrue();
        assertThat(nedetidFrontend.isPlanlagtNedetid).isFalse();
        assertThat(nedetidFrontend.nedetidSlutt).isNotNull();
        assertThat(nedetidFrontend.nedetidStart).isNotNull();
        assertThat(nedetidFrontend.nedetidStart).isEqualTo(nedetidStart.format(NedetidUtils.dateTimeFormatter));
        assertThat(nedetidFrontend.nedetidSlutt).isEqualTo(nedetidSlutt.format(NedetidUtils.dateTimeFormatter));
    }

    // Etter nedetid
    @Test
    public void whenNedetidSluttetFor1sekSiden_ShouldReturnFalse() {
        LocalDateTime nedetidStart = LocalDateTime.now().minusDays(5);
        LocalDateTime nedetidSlutt = LocalDateTime.now().minusSeconds(1);
        System.setProperty(NedetidUtils.NEDETID_START, nedetidStart.format(NedetidUtils.dateTimeFormatter));
        System.setProperty(NedetidUtils.NEDETID_SLUTT, nedetidSlutt.format(NedetidUtils.dateTimeFormatter));

        NedetidRessurs.NedetidFrontend nedetidFrontend = nedetidRessurs.hentNedetidInformasjon();
        assertThat(nedetidFrontend.isNedetid).isFalse();
        assertThat(nedetidFrontend.isPlanlagtNedetid).isFalse();
        assertThat(nedetidFrontend.nedetidSlutt).isNotNull();
        assertThat(nedetidFrontend.nedetidStart).isNotNull();
    }

    @Test
    public void whenNedetidSluttetFor1ArSiden_ShouldReturnFalse() {
        LocalDateTime nedetidStart = LocalDateTime.now().minusDays(5);
        LocalDateTime nedetidSlutt = LocalDateTime.now().minusYears(1).plusDays(1);
        System.setProperty(NedetidUtils.NEDETID_START, nedetidStart.format(NedetidUtils.dateTimeFormatter));
        System.setProperty(NedetidUtils.NEDETID_SLUTT, nedetidSlutt.format(NedetidUtils.dateTimeFormatter));

        NedetidRessurs.NedetidFrontend nedetidFrontend = nedetidRessurs.hentNedetidInformasjon();
        assertThat(nedetidFrontend.isNedetid).isFalse();
        assertThat(nedetidFrontend.isPlanlagtNedetid).isFalse();
        assertThat(nedetidFrontend.nedetidSlutt).isNotNull();
        assertThat(nedetidFrontend.nedetidStart).isNotNull();
    }

    // Rare caser

    @Test
    public void whenSluttIsBeforeStart_ShouldReturnFalse() {
        LocalDateTime nedetidStart = LocalDateTime.now().plusDays(2);
        LocalDateTime nedetidSlutt = LocalDateTime.now().minusDays(1);
        System.setProperty(NedetidUtils.NEDETID_START, nedetidStart.format(NedetidUtils.dateTimeFormatter));
        System.setProperty(NedetidUtils.NEDETID_SLUTT, nedetidSlutt.format(NedetidUtils.dateTimeFormatter));

        NedetidRessurs.NedetidFrontend nedetidFrontend = nedetidRessurs.hentNedetidInformasjon();
        assertThat(nedetidFrontend.isNedetid).isFalse();
        assertThat(nedetidFrontend.isPlanlagtNedetid).isFalse();
        assertThat(nedetidFrontend.nedetidSlutt).isNotNull();
        assertThat(nedetidFrontend.nedetidStart).isNotNull();
    }

    @Test
    public void whenPropertyIsNotInDateformat_ShouldReturnNullAndFalse() {
        System.setProperty(NedetidUtils.NEDETID_START, "noe");
        NedetidRessurs.NedetidFrontend nedetidFrontend = nedetidRessurs.hentNedetidInformasjon();
        assertThat(nedetidFrontend.isNedetid).isFalse();
        assertThat(nedetidFrontend.isPlanlagtNedetid).isFalse();
        assertThat(nedetidFrontend.nedetidSlutt).isNull();
        assertThat(nedetidFrontend.nedetidStart).isNull();

        System.clearProperty(NedetidUtils.NEDETID_START);
        System.setProperty(NedetidUtils.NEDETID_SLUTT, "null");
        nedetidFrontend = nedetidRessurs.hentNedetidInformasjon();
        assertThat(nedetidFrontend.isNedetid).isFalse();
        assertThat(nedetidFrontend.isPlanlagtNedetid).isFalse();
        assertThat(nedetidFrontend.nedetidSlutt).isNull();
        assertThat(nedetidFrontend.nedetidStart).isNull();

        System.setProperty(NedetidUtils.NEDETID_START, "");
        System.setProperty(NedetidUtils.NEDETID_SLUTT, "noe");
        nedetidFrontend = nedetidRessurs.hentNedetidInformasjon();
        assertThat(nedetidFrontend.isNedetid).isFalse();
        assertThat(nedetidFrontend.isPlanlagtNedetid).isFalse();
        assertThat(nedetidFrontend.nedetidSlutt).isNull();
        assertThat(nedetidFrontend.nedetidStart).isNull();
    }
}