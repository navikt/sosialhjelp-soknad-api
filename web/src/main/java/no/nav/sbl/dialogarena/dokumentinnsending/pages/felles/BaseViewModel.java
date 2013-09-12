package no.nav.sbl.dialogarena.dokumentinnsending.pages.felles;

import no.nav.modig.content.CmsContentRetriever;
import no.nav.sbl.dialogarena.dokumentinnsending.domain.BrukerBehandlingType;
import no.nav.sbl.dialogarena.dokumentinnsending.domain.Soknad;

import javax.inject.Inject;
import java.io.Serializable;

public class BaseViewModel implements Serializable {
    private String tittel;
    private String tabTittel;

    public int aktivtSteg = 1;

    @Inject
    protected CmsContentRetriever cmsContentRetriever;

    public BaseViewModel(String tittel, CmsContentRetriever cmsContentRetriever) {
        this.cmsContentRetriever = cmsContentRetriever;
        this.tittel = tittel;
    }

    public BaseViewModel(Soknad soknad, CmsContentRetriever cmsContentRetriever) {
        this.cmsContentRetriever = cmsContentRetriever;
        if (soknad.er(BrukerBehandlingType.DOKUMENT_ETTERSENDING)) {
            tittel = String.format("%s %s", cmsContentRetriever.hentTekst("ettersending.tittelPrefix"), soknad.soknadTittel.toLowerCase());
        } else {
            tittel = soknad.soknadTittel;
        }
    }

    public String getTittel() {
        return tittel;
    }

    public String getTabTittel() {
        return tabTittel;
    }

    public void setTabTittel(String resourceKey) {
        tabTittel = getApplicationProperty(resourceKey);
    }

    protected String getApplicationProperty(String key) {
        return cmsContentRetriever.hentTekst(key);
     }
}