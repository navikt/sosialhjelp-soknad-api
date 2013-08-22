package no.nav.sbl.dialogarena.dokumentinnsending.pages.innsendingkvittering;

import no.nav.modig.content.CmsContentRetriever;
import no.nav.sbl.dialogarena.dokumentinnsending.domain.BrukerBehandlingType;
import no.nav.sbl.dialogarena.dokumentinnsending.domain.Dokument;
import no.nav.sbl.dialogarena.dokumentinnsending.domain.DokumentSoknad;
import no.nav.sbl.dialogarena.dokumentinnsending.domain.SoknadStatus;
import no.nav.sbl.dialogarena.dokumentinnsending.pages.felles.BaseViewModel;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;

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

public class InnsendingKvitteringViewModel extends BaseViewModel {

    private String sideTittel;
    private String kvitteringTekst;
    public List<Dokument> innsendteDokumenter = new ArrayList<>();
    public List<Dokument> ikkeSendteDokumenter = new ArrayList<>();
    public SoknadStatus status;
    public DateTime innsendtDato;

    public InnsendingKvitteringViewModel(DokumentSoknad soknad, CmsContentRetriever cmsContentRetriever) {
        super(soknad, cmsContentRetriever);

        sideTittel = getApplicationProperty("kvittering.innsendt.sideTittel");
        innsendteDokumenter = on(soknad.getDokumenter())
                .filter(both(
                        harValg(LASTET_OPP))
                        .and(not(harKodeverkId(KVITTERING_KODEVERKSID))))
                .collect();
        ikkeSendteDokumenter = on(soknad.getDokumenter()).filter(harIkkeValg(LASTET_OPP)).collect();
        aktivtSteg = 3;
        status = soknad.status;
        innsendtDato = soknad.innsendtDato;
        kvitteringTekst = soknad.er(BrukerBehandlingType.DOKUMENT_BEHANDLING) ? "kvittering.innsendt.innsending.beskrivelse" : "kvittering.innsendt.ettersendelse.beskrivelse";
        setTabTittel("kvittering.innsendt.tittel");
    }

    public String getSideTittel() {
        return sideTittel;
    }

    public String getInnsendtDato() {
        return innsendtDato.toString(DateTimeFormat.forPattern("dd.MM.yyy"));
    }

    public String getKvitteringTekst() {
        return getApplicationProperty(kvitteringTekst);
    }
}
