package no.nav.sbl.dialogarena.dokumentinnsending.pages.oversikt;

import no.nav.modig.content.CmsContentRetriever;
import no.nav.modig.wicket.events.NamedEventPayload;
import no.nav.sbl.dialogarena.dokumentinnsending.domain.Dokument;
import no.nav.sbl.dialogarena.dokumentinnsending.pages.felles.AjaxLabelLink;
import no.nav.sbl.dialogarena.dokumentinnsending.pages.felles.behaviors.JSPreviewBehavior;
import no.nav.sbl.dialogarena.dokumentinnsending.service.SoknadService;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.event.Broadcast;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.LoadableDetachableModel;
import org.apache.wicket.model.PropertyModel;

import javax.inject.Inject;

import static no.nav.modig.wicket.conditional.ConditionalUtils.hasCssClassIf;

public class OpplastetDokumentInfo extends Panel {

    @Inject
    private SoknadService soknadService;

    @Inject
    protected CmsContentRetriever cmsContentRetriever;

    public OpplastetDokumentInfo(String id, final IModel<Dokument> dokument) {
        super(id);

        add(new Label("dokumentnavn", dokument.getObject().getNavn())
                .add(hasCssClassIf("lowercase", new PropertyModel<Boolean>(dokument, "erTittelOver18Tegn"))));

        LoadableDetachableModel<String> visModel = new LoadableDetachableModel<String>() {
            @Override
            protected String load() {
                if (dokument.getObject().er(Dokument.Type.HOVEDSKJEMA)) {
                    return cmsContentRetriever.hentTekst("innsendingside.visSkjema");
                } else {
                    return cmsContentRetriever.hentTekst("innsendingside.visVedlegg");
                }
            }
        };

        LoadableDetachableModel<String> slettModel = new LoadableDetachableModel<String>() {
            @Override
            protected String load() {
                if (dokument.getObject().er(Dokument.Type.HOVEDSKJEMA)) {
                    return cmsContentRetriever.hentTekst("innsendingside.slettSkjema");
                } else {
                    return cmsContentRetriever.hentTekst("innsendingside.slettVedlegg");
                }
            }
        };

        AjaxLabelLink fjernDokument = new AjaxLabelLink("fjernDokument", slettModel) {
            @Override
            public void onClick(AjaxRequestTarget target) {
                dokument.getObject().slettInnhold();
                soknadService.slettInnhold(dokument.getObject());
                send(getPage(), Broadcast.DEPTH, new NamedEventPayload(DokumentPanel.OPPDATER_DOKUMENTBOKS, dokument.getObject()));
            }
        };
        add(fjernDokument);

        add(new Label("visVedlegg", visModel)
                .add(new JSPreviewBehavior(dokument)));
    }
}