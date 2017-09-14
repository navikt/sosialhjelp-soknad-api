package no.nav.sbl.dialogarena.soknadinnsending.business.service.soknadservice;

import junit.framework.TestCase;
import no.nav.tjeneste.domene.brukerdialog.sendsoknad.v1.meldinger.WSBehandlingskjedeElement;
import org.joda.time.DateTime;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;

import java.util.ArrayList;
import java.util.List;

import static no.nav.modig.lang.collections.IterUtils.on;
import static no.nav.sbl.dialogarena.soknadinnsending.business.service.soknadservice.StaticMetoder.NYESTE_FORST;
import static no.nav.sbl.dialogarena.soknadinnsending.business.service.soknadservice.StaticMetoder.ELDSTE_FORST;
import static org.assertj.core.api.Assertions.assertThat;

@RunWith(MockitoJUnitRunner.class)
public class StaticMetoderTest extends TestCase {

    @Test
    public void nyesteForstSkalSortereDatoerRiktig() {
        DateTime dato1 = new DateTime(1000000000000L);
        DateTime dato2 = new DateTime(2000000000000L);
        DateTime dato3 = new DateTime(1500000000000L);

        List<WSBehandlingskjedeElement> usortert = new ArrayList<>();
        usortert.add(new WSBehandlingskjedeElement().withInnsendtDato(dato1));
        usortert.add(new WSBehandlingskjedeElement().withInnsendtDato(dato2));
        usortert.add(new WSBehandlingskjedeElement().withInnsendtDato(dato3));

        List<WSBehandlingskjedeElement> sortert = on(usortert).collect(NYESTE_FORST);
        assertThat(sortert.get(0).getInnsendtDato()).isEqualTo(dato2);
        assertThat(sortert.get(1).getInnsendtDato()).isEqualTo(dato3);
        assertThat(sortert.get(2).getInnsendtDato()).isEqualTo(dato1);
    }

    @Test
    public void eldsteForstSkalSortereDatoerRiktig() {
        DateTime dato1 = new DateTime(1000000000000L);
        DateTime dato2 = new DateTime(2000000000000L);
        DateTime dato3 = new DateTime(1500000000000L);

        List<WSBehandlingskjedeElement> usortert = new ArrayList<>();
        usortert.add(new WSBehandlingskjedeElement().withInnsendtDato(dato1));
        usortert.add(new WSBehandlingskjedeElement().withInnsendtDato(dato2));
        usortert.add(new WSBehandlingskjedeElement().withInnsendtDato(dato3));

        List<WSBehandlingskjedeElement> sortert = on(usortert).collect(ELDSTE_FORST);

        assertThat(sortert.get(0).getInnsendtDato()).isEqualTo(dato1);
        assertThat(sortert.get(1).getInnsendtDato()).isEqualTo(dato3);
        assertThat(sortert.get(2).getInnsendtDato()).isEqualTo(dato2);
    }
}