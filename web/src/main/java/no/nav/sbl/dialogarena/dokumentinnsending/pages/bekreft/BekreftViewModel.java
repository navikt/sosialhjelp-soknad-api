package no.nav.sbl.dialogarena.dokumentinnsending.pages.bekreft;

import no.nav.modig.content.CmsContentRetriever;
import no.nav.sbl.dialogarena.dokumentinnsending.domain.Dokument;
import no.nav.sbl.dialogarena.dokumentinnsending.domain.Person;
import no.nav.sbl.dialogarena.dokumentinnsending.domain.DokumentSoknad;
import no.nav.sbl.dialogarena.dokumentinnsending.domain.SoknadStatus;
import no.nav.sbl.dialogarena.dokumentinnsending.pages.felles.BaseViewModel;

import java.util.ArrayList;
import java.util.List;

import static no.nav.modig.lang.collections.IterUtils.on;
import static no.nav.modig.lang.collections.PredicateUtils.both;
import static no.nav.modig.lang.collections.PredicateUtils.not;
import static no.nav.sbl.dialogarena.dokumentinnsending.domain.Dokument.harIkkeValg;
import static no.nav.sbl.dialogarena.dokumentinnsending.domain.Dokument.harKodeverkId;
import static no.nav.sbl.dialogarena.dokumentinnsending.domain.Dokument.harValg;
import static no.nav.sbl.dialogarena.dokumentinnsending.domain.InnsendingsValg.LASTET_OPP;
import static no.nav.sbl.dialogarena.dokumentinnsending.kodeverk.KodeverkClient.KVITTERING_KODEVERKSID;

public class BekreftViewModel extends BaseViewModel {

    private String sideTittel;
    private boolean samtykket;

    private List<Dokument> innsendteDokumenter = new ArrayList<>();
    private List<Dokument> ikkeSendteDokumenter = new ArrayList<>();
    private boolean harUtenlandsAdresse;
    public SoknadStatus status;

    public BekreftViewModel(DokumentSoknad soknad, Person person, CmsContentRetriever cmsContentRetriever) {
        super(soknad, cmsContentRetriever);

        sideTittel = getApplicationProperty("bekreftelsesside.sideTittel");

        harUtenlandsAdresse = person.harUtenlandsAdresse();

        innsendteDokumenter = on(soknad.getDokumenter())
                .filter(both(
                        harValg(LASTET_OPP))
                        .and(not(harKodeverkId(KVITTERING_KODEVERKSID))))
                .collect();
        ikkeSendteDokumenter = on(soknad.getDokumenter()).filter(harIkkeValg(LASTET_OPP)).collect();
        samtykket = Boolean.FALSE;
        aktivtSteg = 2;
        status = soknad.status;
        setTabTittel("bekreftelsesside.tittel");
    }

    public List<Dokument> getInnsendteDokumenter() {
        return innsendteDokumenter;
    }

    public List<Dokument> getIkkeSendteDokumenter() {
        return ikkeSendteDokumenter;
    }

    public String getSideTittel() {
        return sideTittel;
    }

    public boolean getSamtykket() {
        return samtykket;
    }

    public boolean getHarUtenlandskAdresse() {
        return harUtenlandsAdresse;
    }
}