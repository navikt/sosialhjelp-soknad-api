package no.nav.sbl.dialogarena.dokumentinnsending.pages.fortsettsenere;

import no.nav.modig.content.CmsContentRetriever;
import no.nav.sbl.dialogarena.dokumentinnsending.domain.DokumentSoknad;
import no.nav.sbl.dialogarena.dokumentinnsending.pages.felles.BaseViewModel;

public class FortsettSenereViewModel extends BaseViewModel {

    private String sideTittel;

    public FortsettSenereViewModel(DokumentSoknad soknad, CmsContentRetriever cmsContentRetriever) {
        super(soknad, cmsContentRetriever);

        sideTittel = getApplicationProperty("fortsettSenere.sideTittel");
        setTabTittel("fortsettSenere.tittel");
    }

    public String getSideTittel() {
        return sideTittel;
    }
}
