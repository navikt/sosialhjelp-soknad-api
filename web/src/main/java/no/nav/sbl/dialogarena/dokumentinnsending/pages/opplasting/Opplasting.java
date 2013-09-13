package no.nav.sbl.dialogarena.dokumentinnsending.pages.opplasting;

import no.nav.modig.content.CmsContentRetriever;
import no.nav.modig.wicket.events.annotations.RunOnEvents;
import no.nav.sbl.dialogarena.dokumentinnsending.common.DokumentinnsendingParameters;
import no.nav.sbl.dialogarena.dokumentinnsending.domain.Dokument;
import no.nav.sbl.dialogarena.dokumentinnsending.domain.DokumentInnhold;
import no.nav.sbl.dialogarena.dokumentinnsending.domain.VirusException;
import no.nav.sbl.dialogarena.dokumentinnsending.pages.felles.Feilmelding;
import no.nav.sbl.dialogarena.dokumentinnsending.pages.felles.behaviors.DefaultLoadingBehavior;
import no.nav.sbl.dialogarena.dokumentinnsending.pages.felles.forhandsvisning.ImagePreview;
import no.nav.sbl.dialogarena.dokumentinnsending.pages.opplasting.komponenter.UploadFormPanel;
import no.nav.sbl.dialogarena.dokumentinnsending.pages.opplasting.komponenter.UploadValidationUtil;
import no.nav.sbl.dialogarena.dokumentinnsending.service.SoknadService;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.list.ListView;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.LoadableDetachableModel;
import org.apache.wicket.model.ResourceModel;

import javax.inject.Inject;
import java.util.List;

import static no.nav.modig.wicket.conditional.ConditionalUtils.visibleIf;

public class Opplasting extends Panel {


    @Inject
    private SoknadService soknadService;

    @Inject
    protected CmsContentRetriever cmsContentRetriever;

    private static final long serialVersionUID = 1L;
    private IModel<? extends List<OpplastetFil>> uploads;
    private static final String OPPDATER_FILER = "oppdaterFiler";
    private WebMarkupContainer fileListContainer;
    private AjaxLink lastOpp;
    private Feilmelding feedback;

    public Opplasting(String id, final IModel<? extends List<OpplastetFil>> uploads, final IModel<Dokument> dokument) {
        super(id);
        this.uploads = uploads;
        LoadableDetachableModel<Boolean> emptyUploadsModel = emptyUploadsModel();
        add(new Label("tittel", cmsContentRetriever.hentTekst("opplasting.tittel")));
        tilbakemeldingPanel();
        fillisteContainer(emptyUploadsModel);
        UploadFormPanel uploadForm = uploadForm();
        add(uploadForm);
        AjaxLink lastOppLink = lastOppLink(uploads, dokument, emptyUploadsModel);
        add(new Label("opplasting.beskrivelse", cmsContentRetriever.hentTekst("opplasting.beskrivelse")));
        add(lastOppLink);
        lastOppLink.add(new Label("opplasting.lastopp", cmsContentRetriever.hentTekst("opplasting.lastopp")));

    }

    public final List<OpplastetFil> getUploads() {
        return uploads.getObject();
    }

    private LoadableDetachableModel<Boolean> emptyUploadsModel() {
        return new LoadableDetachableModel<Boolean>() {
            @Override
            protected Boolean load() {
                return getUploads() != null && !getUploads().isEmpty();
            }
        };
    }

    private void fillisteContainer(LoadableDetachableModel<Boolean> emptyUploadsModel) {
        fileListContainer = new WebMarkupContainer("fileListContainer");
        fileListContainer.add(visibleIf(emptyUploadsModel));
        fileListContainer.add(new FileListView("fileList", uploads));
        fileListContainer.setOutputMarkupPlaceholderTag(true);
        add(fileListContainer);
    }

    private void tilbakemeldingPanel() {
        feedback = new Feilmelding("tilbakemelding");
        add(feedback);
    }

    private UploadFormPanel uploadForm() {
        UploadFormPanel uploadFormPanel = (new UploadFormPanel("uploadForm", new ResourceModel("opplasting.leggTilFlereFiler"), "knapp-liten", OPPDATER_FILER) {
            @Override
            protected void performActionOnSubmit(AjaxRequestTarget target) {
                target.add(this);
            }
        });
        return uploadFormPanel;

    }

    private AjaxLink lastOppLink(final IModel<? extends List<OpplastetFil>> uploads, final IModel<Dokument> dokument, LoadableDetachableModel<Boolean> emptyUploadsModel) {
        lastOpp = new AjaxLink<Void>("lastOpp") {
            @Override
            public void onClick(AjaxRequestTarget target) {
                DokumentInnhold innhold = OpplastetFil.OPPLASTING_TIL_DOKUMENT.transform(uploads.getObject());
                innhold.setNavn(dokument.getObject().lagFilNavn());
                lastOppFil(dokument.getObject(), innhold, target);
            }
        };
        lastOpp.setOutputMarkupPlaceholderTag(true);
        lastOpp.add(new DefaultLoadingBehavior());
        lastOpp.add(visibleIf(emptyUploadsModel));
        return lastOpp;
    }

    private void lastOppFil(Dokument dokument, DokumentInnhold innhold, AjaxRequestTarget target) {
        try {
            target.appendJavaScript("trackEventGA('Opplasting', 'Lastet opp');");
            soknadService.oppdaterInnhold(dokument, innhold);
            setResponsePage(null, new DokumentinnsendingParameters()
                    .behandlingsId(dokument.getBehandlingsId())
                    .scrollTo(dokument.getDokumentForventningsId()));
        } catch (VirusException e) {
            feedback.error(cmsContentRetriever.hentTekst("validate.opplasting.virus"));
            target.appendJavaScript(String.format("hideLoadingIndicator('%s');", lastOpp.getMarkupId()));
            target.add(feedback);
        }
    }


    @RunOnEvents(OPPDATER_FILER)
    private void oppdaterFiler(AjaxRequestTarget target, List<OpplastetFil> uploads) {
        target.appendJavaScript("trackEventGA('Opplasting', 'Lagt til ny fil');");
        UploadValidationUtil.validateUploadedFiles(uploads, this, getUploads());
        if (uploads != null && getFeedbackMessages().isEmpty()) {
            this.uploads.getObject().addAll(uploads);
        }
        target.add(fileListContainer, lastOpp, feedback);
    }

    @RunOnEvents(ImagePreview.SLETT_OPPLASTET_FIL)
    private void slettOpplastetFil(AjaxRequestTarget target, OpplastetFil upload) {
        target.appendJavaScript("trackEventGA('Opplasting', 'Slettet fil');");
        uploads.getObject().remove(upload);
        target.add(fileListContainer, lastOpp);
    }

    private static class FileListView extends ListView<OpplastetFil> {

        public FileListView(String name, final IModel<? extends List<OpplastetFil>> files) {
            super(name, files);
        }

        @Override
        protected void populateItem(ListItem<OpplastetFil> item) {
            item.add(new ImagePreview("preview", item.getModelObject()));
        }
    }

}
