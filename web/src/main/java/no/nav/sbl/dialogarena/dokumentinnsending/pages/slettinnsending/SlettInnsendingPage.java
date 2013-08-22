package no.nav.sbl.dialogarena.dokumentinnsending.pages.slettinnsending;

import no.nav.sbl.dialogarena.dokumentinnsending.domain.BrukerBehandlingType;
import no.nav.sbl.dialogarena.dokumentinnsending.domain.DokumentSoknad;
import no.nav.sbl.dialogarena.dokumentinnsending.pages.base.modalbasepage.ModalBasePage;
import no.nav.sbl.dialogarena.dokumentinnsending.pages.innsendingslettet.InnsendingSlettetPage;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.link.Link;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.LoadableDetachableModel;
import org.apache.wicket.request.mapper.parameter.PageParameters;

public class SlettInnsendingPage extends ModalBasePage {

    public SlettInnsendingPage(final PageParameters parameters) {
        super(parameters);

        final DokumentSoknad soknad = soknadService.hentSoknad(behandlingsId);

        String resourceKeyPrefix = "slett.innsending";

        if (soknad.er(BrukerBehandlingType.DOKUMENT_ETTERSENDING)) {
            resourceKeyPrefix = "slett.ettersending";
        }

        IModel<String> tittel = new LoadableDetachableModel<String>() {
            @Override
            protected String load() {
                return soknad.soknadTittel;
            }
        };

        add(new Label("tittel", cmsContentRetriever.hentTekst(resourceKeyPrefix + ".tittel")));
        add(new Label("tekst", cmsContentRetriever.hentTekst(resourceKeyPrefix + ".beskrivelse")));
        add(new Label("tabTittel", cmsContentRetriever.hentTekst(resourceKeyPrefix + ".sideTittel")));

        Link<String> button = new Link<String>("bekreftSletting", tittel) {
            @Override
            public void onClick() {
                setResponsePage(new InnsendingSlettetPage(getModelObject().toString()));
                soknadService.slettSoknad(behandlingsId);
            }
        };
        button.add(new Label("knapp-tekst", cmsContentRetriever.hentTekst(resourceKeyPrefix + ".bekreft")));
        add(button);
    }
}
