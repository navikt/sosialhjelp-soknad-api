package no.nav.sbl.sosialhjelp.midlertidig;

import no.nav.sbl.dialogarena.sendsoknad.domain.Vedlegg;
import no.nav.sbl.sosialhjelp.domain.Vedleggstatus;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

import static no.nav.sbl.sosialhjelp.midlertidig.VedleggsforventningConverter.mapVedleggTilVedleggstatus;
import static no.nav.sbl.sosialhjelp.midlertidig.VedleggsforventningConverter.mapVedleggsforventningerTilVedleggstatusListe;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

public class VedleggsforventningConverterTest {
    private static final String EIER = "12345678910";
    private static final String TYPE = "bostotte";
    private static final String TILLEGGSINFO = "annetboutgift";

    @Test
    public void mapVedleggsforventningerTilVedleggstatusListeFjernerVedleggSomErNullEllerIkkeVedleggEllerAlleredeLastetOpp() {
        List<Vedlegg> vedlegg = new ArrayList<>();
        vedlegg.add(lagVedleggMedStatus(Vedlegg.Status.VedleggAlleredeSendt));
        vedlegg.add(lagVedleggMedStatus(Vedlegg.Status.IkkeVedlegg));
        vedlegg.add(lagVedleggMedStatus(Vedlegg.Status.LastetOpp));
        vedlegg.add(null);

        List<Vedleggstatus> vedleggstatuser = mapVedleggsforventningerTilVedleggstatusListe(vedlegg, EIER);

        assertThat(vedleggstatuser.size(), is(1));
        assertThat(vedleggstatuser.get(0).getStatus(), is(Vedleggstatus.Status.VedleggAlleredeSendt));
    }

    @Test
    public void mapVedleggsforventningerTilVedleggstatusListeReturnererTomListeHvisVedleggsforventningerErTom() {
        List<Vedleggstatus> vedleggstatuser = mapVedleggsforventningerTilVedleggstatusListe(new ArrayList<>(), EIER);

        assertThat(vedleggstatuser.size(), is(0));
    }

    @Test
    public void mapVedleggTilVedleggstatusMapperInfoRiktig() {
        Vedleggstatus vedleggstatus = mapVedleggTilVedleggstatus(lagVedleggMedStatus(Vedlegg.Status.VedleggKreves), EIER);

        assertThat(vedleggstatus.getEier(), is(EIER));
        assertThat(vedleggstatus.getVedleggType().getType(), is(TYPE));
        assertThat(vedleggstatus.getVedleggType().getTilleggsinfo(), is(TILLEGGSINFO));
        assertThat(vedleggstatus.getStatus(), is(Vedleggstatus.Status.VedleggKreves));
    }

    private Vedlegg lagVedleggMedStatus(Vedlegg.Status status) {
        return new Vedlegg()
                .medSkjemaNummer(TYPE)
                .medSkjemanummerTillegg(TILLEGGSINFO)
                .medInnsendingsvalg(status);
    }
}