package no.nav.sbl.dialogarena.dokumentinnsending.pages.oversikt;

import no.nav.modig.content.CmsContentRetriever;
import no.nav.sbl.dialogarena.dokumentinnsending.domain.BrukerBehandlingType;
import no.nav.sbl.dialogarena.dokumentinnsending.domain.Dokument;
import no.nav.sbl.dialogarena.dokumentinnsending.domain.DokumentSoknad;
import no.nav.sbl.dialogarena.dokumentinnsending.domain.SoknadStatus;
import no.nav.sbl.dialogarena.dokumentinnsending.pages.felles.BaseViewModel;
import org.joda.time.DateTime;

import java.util.ArrayList;
import java.util.List;

public class OversiktViewModel extends BaseViewModel {
    private List<Dokument> dokumenter = new ArrayList<>();
    private String getAvbrytInnsendingTekst;

    public SoknadStatus status;
    public DateTime sistEndret;

    public OversiktViewModel(DokumentSoknad soknad, CmsContentRetriever cmsContentRetriever) {
        super(soknad, cmsContentRetriever);
        dokumenter = soknad.getDokumenter();
        status = soknad.status;
        sistEndret = soknad.sistEndret;
        getAvbrytInnsendingTekst = soknad.er(BrukerBehandlingType.DOKUMENT_BEHANDLING) ? "innsendingside.slettSoknad" : "innsendingside.slettEttersending";
        setTabTittel("innsendingside.tittel");
    }

    public List<Dokument> getDokumenter() {
        return dokumenter;
    }

    public String getAvbrytInnsendingTekst() {
        return getApplicationProperty(getAvbrytInnsendingTekst);
    }
}
