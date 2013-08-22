package no.nav.sbl.dialogarena.dokumentinnsending.pages.oversikt;

import no.nav.modig.core.exception.ApplicationException;
import no.nav.modig.wicket.events.annotations.RunOnEvents;
import no.nav.sbl.dialogarena.dokumentinnsending.common.BrukerBehandlingId;
import no.nav.sbl.dialogarena.dokumentinnsending.common.DokumentinnsendingParameters;
import no.nav.sbl.dialogarena.dokumentinnsending.common.SpraakKode;
import no.nav.sbl.dialogarena.dokumentinnsending.domain.Dokument;
import no.nav.sbl.dialogarena.dokumentinnsending.domain.DokumentSoknad;
import no.nav.sbl.dialogarena.dokumentinnsending.domain.SoknadStatus;
import no.nav.sbl.dialogarena.dokumentinnsending.pages.base.mainbasepage.MainBasePage;
import no.nav.sbl.dialogarena.dokumentinnsending.pages.bekreft.BekreftelsesPage;
import no.nav.sbl.dialogarena.dokumentinnsending.pages.felles.Feilmelding;
import no.nav.sbl.dialogarena.dokumentinnsending.pages.felles.OpenPageLink;
import no.nav.sbl.dialogarena.dokumentinnsending.pages.felles.StegIndikator;
import no.nav.sbl.dialogarena.dokumentinnsending.pages.felles.behaviors.BaseLoadingBehavior;
import no.nav.sbl.dialogarena.dokumentinnsending.pages.felles.behaviors.DefaultLoadingBehavior;
import no.nav.sbl.dialogarena.dokumentinnsending.pages.fortsettsenere.FortsettSenerePage;
import no.nav.sbl.dialogarena.dokumentinnsending.pages.hjelp.HjelpPage;
import no.nav.sbl.dialogarena.dokumentinnsending.pages.leggtilvedlegg.LeggTilVedleggPage;
import no.nav.sbl.dialogarena.dokumentinnsending.pages.slettinnsending.SlettInnsendingPage;
import no.nav.sbl.dialogarena.dokumentinnsending.service.SoknadService;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.list.ListView;
import org.apache.wicket.model.CompoundPropertyModel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.LoadableDetachableModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.request.mapper.parameter.PageParameters;
import org.apache.wicket.util.string.StringValue;

import javax.inject.Inject;

import static no.nav.modig.lang.collections.PredicateUtils.equalTo;
import static no.nav.modig.lang.collections.PredicateUtils.where;
import static no.nav.sbl.dialogarena.dokumentinnsending.domain.InnsendingsValg.IKKE_VALGT;
import static no.nav.sbl.dialogarena.dokumentinnsending.domain.InnsendingsValg.LASTET_OPP;
import static org.apache.commons.collections15.CollectionUtils.exists;

public class OversiktPage extends MainBasePage {

    @Inject
    private SoknadService soknadService;

    private WebMarkupContainer oppdaterDokumenter;
    private Feilmelding feedbackPanel;

    public OversiktPage() {
        this(new PageParameters());
    }

    public OversiktPage(PageParameters parameters) {
        super(parameters);

        feedbackPanel = new Feilmelding("tilbakemelding");
        add(feedbackPanel);

        final String behandlingsId = BrukerBehandlingId.get(parameters);

        setDefaultModel(new CompoundPropertyModel<>(new LoadableDetachableModel<OversiktViewModel>() {
            @Override
            protected OversiktViewModel load() {
                DokumentSoknad soknad = soknadService.hentSoknad(behandlingsId);
                return new OversiktViewModel(soknad, cmsContentRetriever);
            }
        }));
        OpenPageLink opprettVedleggLink = new OpenPageLink("opprettVedlegg", LeggTilVedleggPage.class, behandlingsId);
        add(opprettVedleggLink);
        opprettVedleggLink.add((new Label("innsendingside.leggTilVedlegg", cmsContentRetriever.hentTekst("innsendingside.leggTilVedlegg"))));

        OpenPageLink hjelpLink = new OpenPageLink("hjelp", HjelpPage.class, behandlingsId);
        add(hjelpLink);
        hjelpLink.add(new Label("innsendingside.hjelp", cmsContentRetriever.hentTekst("innsendingside.hjelp")));
        add(new OpenPageLink("avbrytInnsending", SlettInnsendingPage.class, behandlingsId)
                .add(new Label("avbrytInnsendingTekst")));

        add(new SoknadTips("soknad-tips").setVisible(!parameters.get("visInfo").isNull()));
        add(new StegIndikator("stegIndikator"));
        OpenPageLink fortsettSenereLink =  new OpenPageLink("fortsettSenere", FortsettSenerePage.class, behandlingsId);
        add(fortsettSenereLink);




        fortsettSenereLink.add(new Label("fortsettSenereTekst", cmsContentRetriever.hentTekst("fortsettSenere")));

        oppdaterDokumenter = new WebMarkupContainer("oppdaterDokumenter");
        oppdaterDokumenter.setOutputMarkupId(true);
        oppdaterDokumenter.add(new DokumentlisteOversikt("dokumenter", parameters.get("scrollTo")));
        add(oppdaterDokumenter);



        AjaxLink fortsettInnsending = new AjaxLink("fortsettInnsending") {
            @Override
            public void onClick(AjaxRequestTarget target) {
                validerAtTilstandTillaterAFortsette(target, this, behandlingsId);
            }
        };


        fortsettInnsending.add(new DefaultLoadingBehavior(BaseLoadingBehavior.Colour.GRAA));
        fortsettInnsending.add(new Label("innsendingside.fortsettInnsending", cmsContentRetriever.hentTekst("innsendingside.fortsettInnsending")));
        add(fortsettInnsending);


    }

    public static class DokumentlisteOversikt extends ListView<Dokument> {
        private StringValue scrollTo;

        public DokumentlisteOversikt(String id, StringValue scrollTo) {
            super(id);
            this.setOutputMarkupId(true);
            this.scrollTo = scrollTo;
        }

        @Override
        protected void populateItem(ListItem<Dokument> item) {
            IModel<Boolean> loading = Model.of(!scrollTo.isEmpty() && item.getModelObject().getDokumentForventningsId().equals(scrollTo.toLong()));
            item.setOutputMarkupId(true);
            item.add(new DokumentPanel("dokument", item.getModel(), loading));
        }
    }

    @Override
    protected void onConfigure() {
        super.onConfigure();
        validerAtTilstandTillaterAApneSiden();

        if (!getPageParameters().get("languagecode").isEmpty()) {
            SpraakKode.setGjeldendeSpraak(getPageParameters().get("languagecode").toString());
        }
    }

    @RunOnEvents(DokumentNavn.OPPDATER_VEDLEGGLISTE)
    private void oppdaterVedleggListe(AjaxRequestTarget target) {
        this.detach();
        target.add(oppdaterDokumenter);
    }

    // Validerer at søknaden har tilstand som tillater å gå til denne siden
    private void validerAtTilstandTillaterAApneSiden() {
        OversiktViewModel viewModel = (OversiktViewModel) getDefaultModelObject();

        if (viewModel.status.er(SoknadStatus.UNDER_ARBEID)) {
            return;
        }
        throw new ApplicationException("Henvendelsen er avsluttet");
    }

    // Validerer at søknaden har tilstand som tillater å fortsette til neste side
    private void validerAtTilstandTillaterAFortsette(AjaxRequestTarget target, AjaxLink button, String behandlingsId) {
        OversiktViewModel viewModel = (OversiktViewModel) getDefaultModelObject();
        if (!exists(viewModel.getDokumenter(), where(Dokument.INNSENDINGSVALG, equalTo(IKKE_VALGT))) && exists(viewModel.getDokumenter(), where(Dokument.INNSENDINGSVALG, equalTo(LASTET_OPP)))) {
            setResponsePage(BekreftelsesPage.class, new DokumentinnsendingParameters(behandlingsId));
        } else {
            String errorModel;
            if (exists(viewModel.getDokumenter(), Dokument.avType(Dokument.Type.HOVEDSKJEMA))) {
                errorModel = cmsContentRetriever.hentTekst("innsendingside.fortsett.feil");
            } else {
                errorModel = cmsContentRetriever.hentTekst("innsendingside.fortsett.ingenOpplastetDokument");
            }
            feedbackPanel.error(errorModel);

            String js = String.format("hideLoadingIndicator('%s');", button.getMarkupId());
            target.appendJavaScript(js);
            target.add(feedbackPanel);
        }
    }
}
