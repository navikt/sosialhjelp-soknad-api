package no.nav.sbl.dialogarena.rest.ressurser;

import no.nav.sbl.dialogarena.utils.NedetidUtils;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.runners.MockitoJUnitRunner;

import java.time.LocalDateTime;

import static org.junit.Assert.*;

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
        assertFalse(nedetidFrontend.isNedetid);
        assertFalse(nedetidFrontend.isPlanlagtNedetid);
        assertNull(nedetidFrontend.nedetidSlutt);
        assertNull(nedetidFrontend.nedetidStart);

        System.setProperty(NedetidUtils.NEDETID_START, LocalDateTime.now().format(NedetidUtils.dateTimeFormatter));
        nedetidFrontend = nedetidRessurs.hentNedetidInformasjon();
        assertFalse(nedetidFrontend.isNedetid);
        assertFalse(nedetidFrontend.isPlanlagtNedetid);
        assertNull(nedetidFrontend.nedetidSlutt);

        System.clearProperty(NedetidUtils.NEDETID_START);
        System.setProperty(NedetidUtils.NEDETID_SLUTT, LocalDateTime.now().format(NedetidUtils.dateTimeFormatter));
        nedetidFrontend = nedetidRessurs.hentNedetidInformasjon();
        assertFalse(nedetidFrontend.isNedetid);
        assertFalse(nedetidFrontend.isPlanlagtNedetid);
        assertNull(nedetidFrontend.nedetidStart);
    }

    @Test
    public void whenNedetidStarterOm15dager_ShouldReturnFalseAndNull() {
        LocalDateTime nedetidStart = LocalDateTime.now().plusDays(15);
        LocalDateTime nedetidSlutt = LocalDateTime.now().plusDays(20);
        System.setProperty(NedetidUtils.NEDETID_START, nedetidStart.format(NedetidUtils.dateTimeFormatter));
        System.setProperty(NedetidUtils.NEDETID_SLUTT, nedetidSlutt.format(NedetidUtils.dateTimeFormatter));

        NedetidRessurs.NedetidFrontend nedetidFrontend = nedetidRessurs.hentNedetidInformasjon();
        assertFalse(nedetidFrontend.isNedetid);
        assertFalse(nedetidFrontend.isPlanlagtNedetid);
        assertNotNull(nedetidFrontend.nedetidSlutt);
        assertNotNull(nedetidFrontend.nedetidStart);
    }

    @Test
    public void whenNedetidStarterOm14dagerAnd1min_ShouldReturnFalseAndNull() {
        LocalDateTime nedetidStart = LocalDateTime.now().plusDays(14).plusMinutes(1);
        LocalDateTime nedetidSlutt = LocalDateTime.now().plusDays(20);
        System.setProperty(NedetidUtils.NEDETID_START, nedetidStart.format(NedetidUtils.dateTimeFormatter));
        System.setProperty(NedetidUtils.NEDETID_SLUTT, nedetidSlutt.format(NedetidUtils.dateTimeFormatter));

        NedetidRessurs.NedetidFrontend nedetidFrontend = nedetidRessurs.hentNedetidInformasjon();
        assertFalse(nedetidFrontend.isNedetid);
        assertFalse(nedetidFrontend.isPlanlagtNedetid);
        assertNotNull(nedetidFrontend.nedetidSlutt);
        assertNotNull(nedetidFrontend.nedetidStart);
    }


    // Innenfor planlagt nedetid:

    @Test
    public void whenNedetidStarterOm12dager_ShouldReturnPlanlagtNedetid() {
        LocalDateTime nedetidStart = LocalDateTime.now().plusDays(12);
        LocalDateTime nedetidSlutt = LocalDateTime.now().plusDays(20);
        System.setProperty(NedetidUtils.NEDETID_START, nedetidStart.format(NedetidUtils.dateTimeFormatter));
        System.setProperty(NedetidUtils.NEDETID_SLUTT, nedetidSlutt.format(NedetidUtils.dateTimeFormatter));

        NedetidRessurs.NedetidFrontend nedetidFrontend = nedetidRessurs.hentNedetidInformasjon();
        assertFalse(nedetidFrontend.isNedetid);
        assertTrue(nedetidFrontend.isPlanlagtNedetid);
        assertNotNull(nedetidFrontend.nedetidSlutt);
        assertNotNull(nedetidFrontend.nedetidStart);
    }

    @Test
    public void whenNedetidStarterOm14dagerMinus1min_ShouldReturnPlanlagtNedetid() {
        LocalDateTime nedetidStart = LocalDateTime.now().plusDays(14).minusMinutes(1);
        LocalDateTime nedetidSlutt = LocalDateTime.now().plusDays(20);
        System.setProperty(NedetidUtils.NEDETID_START, nedetidStart.format(NedetidUtils.dateTimeFormatter));
        System.setProperty(NedetidUtils.NEDETID_SLUTT, nedetidSlutt.format(NedetidUtils.dateTimeFormatter));

        NedetidRessurs.NedetidFrontend nedetidFrontend = nedetidRessurs.hentNedetidInformasjon();
        assertFalse(nedetidFrontend.isNedetid);
        assertTrue(nedetidFrontend.isPlanlagtNedetid);
        assertNotNull(nedetidFrontend.nedetidSlutt);
        assertNotNull(nedetidFrontend.nedetidStart);
    }

    // Innenfor nedetid:

    @Test
    public void whenNedetidStartetfor1sekSiden_ShouldReturnNedetid() {
        LocalDateTime nedetidStart = LocalDateTime.now().minusSeconds(1);
        LocalDateTime nedetidSlutt = LocalDateTime.now().plusDays(20);
        System.setProperty(NedetidUtils.NEDETID_START, nedetidStart.format(NedetidUtils.dateTimeFormatter));
        System.setProperty(NedetidUtils.NEDETID_SLUTT, nedetidSlutt.format(NedetidUtils.dateTimeFormatter));

        NedetidRessurs.NedetidFrontend nedetidFrontend = nedetidRessurs.hentNedetidInformasjon();
        assertTrue(nedetidFrontend.isNedetid);
        assertFalse(nedetidFrontend.isPlanlagtNedetid);
        assertNotNull(nedetidFrontend.nedetidSlutt);
        assertNotNull(nedetidFrontend.nedetidStart);
    }

    @Test
    public void whenMidtINedetid_ShouldReturnNedetid() {
        LocalDateTime nedetidStart = LocalDateTime.now().minusDays(5);
        LocalDateTime nedetidSlutt = LocalDateTime.now().plusDays(5);
        System.setProperty(NedetidUtils.NEDETID_START, nedetidStart.format(NedetidUtils.dateTimeFormatter));
        System.setProperty(NedetidUtils.NEDETID_SLUTT, nedetidSlutt.format(NedetidUtils.dateTimeFormatter));

        NedetidRessurs.NedetidFrontend nedetidFrontend = nedetidRessurs.hentNedetidInformasjon();
        assertTrue(nedetidFrontend.isNedetid);
        assertFalse(nedetidFrontend.isPlanlagtNedetid);
        assertNotNull(nedetidFrontend.nedetidSlutt);
        assertNotNull(nedetidFrontend.nedetidStart);
    }

    @Test
    public void whenNedetidSlutterOm1min_ShouldReturnNedetid() {
        LocalDateTime nedetidStart = LocalDateTime.now().minusDays(2);
        LocalDateTime nedetidSlutt = LocalDateTime.now().plusMinutes(1);
        System.setProperty(NedetidUtils.NEDETID_START, nedetidStart.format(NedetidUtils.dateTimeFormatter));
        System.setProperty(NedetidUtils.NEDETID_SLUTT, nedetidSlutt.format(NedetidUtils.dateTimeFormatter));

        NedetidRessurs.NedetidFrontend nedetidFrontend = nedetidRessurs.hentNedetidInformasjon();
        assertTrue(nedetidFrontend.isNedetid);
        assertFalse(nedetidFrontend.isPlanlagtNedetid);
        assertNotNull(nedetidFrontend.nedetidSlutt);
        assertNotNull(nedetidFrontend.nedetidStart);
        assertEquals(nedetidStart.format(NedetidUtils.dateTimeFormatter), nedetidFrontend.nedetidStart);
        assertEquals(nedetidSlutt.format(NedetidUtils.dateTimeFormatter), nedetidFrontend.nedetidSlutt);
    }

    // Etter nedetid
    @Test
    public void whenNedetidSluttetFor1sekSiden_ShouldReturnFalse() {
        LocalDateTime nedetidStart = LocalDateTime.now().minusDays(5);
        LocalDateTime nedetidSlutt = LocalDateTime.now().minusSeconds(1);
        System.setProperty(NedetidUtils.NEDETID_START, nedetidStart.format(NedetidUtils.dateTimeFormatter));
        System.setProperty(NedetidUtils.NEDETID_SLUTT, nedetidSlutt.format(NedetidUtils.dateTimeFormatter));

        NedetidRessurs.NedetidFrontend nedetidFrontend = nedetidRessurs.hentNedetidInformasjon();
        assertFalse(nedetidFrontend.isNedetid);
        assertFalse(nedetidFrontend.isPlanlagtNedetid);
        assertNotNull(nedetidFrontend.nedetidSlutt);
        assertNotNull(nedetidFrontend.nedetidStart);
    }

    @Test
    public void whenNedetidSluttetFor1ArSiden_ShouldReturnFalse() {
        LocalDateTime nedetidStart = LocalDateTime.now().minusDays(5);
        LocalDateTime nedetidSlutt = LocalDateTime.now().minusYears(1).plusDays(1);
        System.setProperty(NedetidUtils.NEDETID_START, nedetidStart.format(NedetidUtils.dateTimeFormatter));
        System.setProperty(NedetidUtils.NEDETID_SLUTT, nedetidSlutt.format(NedetidUtils.dateTimeFormatter));

        NedetidRessurs.NedetidFrontend nedetidFrontend = nedetidRessurs.hentNedetidInformasjon();
        assertFalse(nedetidFrontend.isNedetid);
        assertFalse(nedetidFrontend.isPlanlagtNedetid);
        assertNotNull(nedetidFrontend.nedetidSlutt);
        assertNotNull(nedetidFrontend.nedetidStart);
    }

    // Rare caser

    @Test
    public void whenSluttIsBeforeStart_ShouldReturnFalse() {
        LocalDateTime nedetidStart = LocalDateTime.now().plusDays(2);
        LocalDateTime nedetidSlutt = LocalDateTime.now().minusDays(1);
        System.setProperty(NedetidUtils.NEDETID_START, nedetidStart.format(NedetidUtils.dateTimeFormatter));
        System.setProperty(NedetidUtils.NEDETID_SLUTT, nedetidSlutt.format(NedetidUtils.dateTimeFormatter));

        NedetidRessurs.NedetidFrontend nedetidFrontend = nedetidRessurs.hentNedetidInformasjon();
        assertFalse(nedetidFrontend.isNedetid);
        assertFalse(nedetidFrontend.isPlanlagtNedetid);
        assertNotNull(nedetidFrontend.nedetidSlutt);
        assertNotNull(nedetidFrontend.nedetidStart);
    }

    @Test
    public void whenPropertyIsNotInDateformat_ShouldReturnNullAndFalse() {
        System.setProperty(NedetidUtils.NEDETID_START, "noe");
        NedetidRessurs.NedetidFrontend nedetidFrontend = nedetidRessurs.hentNedetidInformasjon();
        assertFalse(nedetidFrontend.isNedetid);
        assertFalse(nedetidFrontend.isPlanlagtNedetid);
        assertNull(nedetidFrontend.nedetidSlutt);
        assertNull(nedetidFrontend.nedetidStart);

        System.clearProperty(NedetidUtils.NEDETID_START);
        System.setProperty(NedetidUtils.NEDETID_SLUTT, "null");
        nedetidFrontend = nedetidRessurs.hentNedetidInformasjon();
        assertFalse(nedetidFrontend.isNedetid);
        assertFalse(nedetidFrontend.isPlanlagtNedetid);
        assertNull(nedetidFrontend.nedetidSlutt);
        assertNull(nedetidFrontend.nedetidStart);

        System.setProperty(NedetidUtils.NEDETID_START, "");
        System.setProperty(NedetidUtils.NEDETID_SLUTT, "noe");
        nedetidFrontend = nedetidRessurs.hentNedetidInformasjon();
        assertFalse(nedetidFrontend.isNedetid);
        assertFalse(nedetidFrontend.isPlanlagtNedetid);
        assertNull(nedetidFrontend.nedetidSlutt);
        assertNull(nedetidFrontend.nedetidStart);
    }
}