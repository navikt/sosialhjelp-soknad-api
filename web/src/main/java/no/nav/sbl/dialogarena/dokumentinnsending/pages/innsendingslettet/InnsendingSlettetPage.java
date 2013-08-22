package no.nav.sbl.dialogarena.dokumentinnsending.pages.innsendingslettet;

import no.nav.modig.content.CmsContentRetriever;
import no.nav.sbl.dialogarena.dokumentinnsending.pages.base.mainbasepage.MainBasePage;
import no.nav.sbl.dialogarena.dokumentinnsending.pages.felles.BaseViewModel;
import no.nav.sbl.dialogarena.dokumentinnsending.pages.felles.DittNavLink;
import no.nav.sbl.dialogarena.dokumentinnsending.pages.felles.SkjemaveilederLink;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.model.CompoundPropertyModel;
import org.apache.wicket.request.mapper.parameter.PageParameters;

public class InnsendingSlettetPage extends MainBasePage {

    public InnsendingSlettetPage(String tittel) {
        super(new PageParameters());
        setDefaultModel(new CompoundPropertyModel<>(new ViewModel(tittel, cmsContentRetriever)));
        add(new SkjemaveilederLink("skjemaveileder", cmsContentRetriever.hentTekst("skjemaveileder.url.path") + cmsContentRetriever.hentTekst("skjemaveileder")));
        add(new Label("kvittering.slettet.sideTittel", cmsContentRetriever.hentTekst("kvittering.slettet.sideTittel")));
        add(new Label("kvittering.slettet.beskrivelse", cmsContentRetriever.hentTekst("kvittering.slettet.beskrivelse")));
        add(new DittNavLink("dittnav"));
    }

    private static class ViewModel extends BaseViewModel {
        public ViewModel(String tittel, CmsContentRetriever cmsContentRetriever) {
            super(tittel, cmsContentRetriever);
            setTabTittel("kvittering.slettet.tittel");
        }
    }
}
