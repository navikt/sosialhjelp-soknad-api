package no.nav.sbl.dialogarena.soknadinnsending.business.domain.exception;

import no.nav.sbl.dialogarena.soknadinnsending.business.domain.Vedlegg;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

public class VedleggGenereringMismatchTest {

    @Test
    public void skalMangleEttVedlegg() throws Exception {

        List<Vedlegg> orginaleVedlegg = new ArrayList<>();
        Vedlegg vedlegg = new Vedlegg(1L, 1L, "skjemanr", Vedlegg.Status.SendesIkke);
        orginaleVedlegg.add(vedlegg);
        VedleggGenereringMismatch.Mismatch mismatch = new VedleggGenereringMismatch(orginaleVedlegg, new ArrayList<Vedlegg>()).getMismatch();

        assertThat(mismatch.getVedleggSomMangler()).contains(vedlegg);
        assertThat(mismatch.getVedleggGenerertEkstra()).isEmpty();
    }

    @Test
    public void skalHaEtVedleggForMye() throws Exception {

        List<Vedlegg> nyeVedlegg = new ArrayList<>();
        Vedlegg nyttVedlegg = new Vedlegg(1L, 1L, "skjemanr", Vedlegg.Status.SendesIkke);
        nyeVedlegg.add(nyttVedlegg);
        VedleggGenereringMismatch.Mismatch mismatch = new VedleggGenereringMismatch(new ArrayList<Vedlegg>(), nyeVedlegg).getMismatch();

        assertThat(mismatch.getVedleggSomMangler()).isEmpty();
        assertThat(mismatch.getVedleggGenerertEkstra()).contains(nyttVedlegg);

    }
}