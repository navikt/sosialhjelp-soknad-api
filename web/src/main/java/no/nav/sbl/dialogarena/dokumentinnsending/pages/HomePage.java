package no.nav.sbl.dialogarena.dokumentinnsending.pages;

import no.nav.modig.core.context.SubjectHandler;
import no.nav.sbl.dialogarena.dokumentinnsending.domain.Dokument;
import no.nav.sbl.dialogarena.dokumentinnsending.domain.DokumentSoknad;
import no.nav.sbl.dialogarena.dokumentinnsending.domain.SoknadStatus;
import no.nav.sbl.dialogarena.dokumentinnsending.repository.SoknadRepository;
import no.nav.sbl.dialogarena.dokumentinnsending.service.BrukerBehandlingServiceIntegration;
import no.nav.sbl.dialogarena.dokumentinnsending.service.SoknadService;
import no.nav.sbl.dialogarena.websoknad.pages.opensoknad.OpenSoknadPage;
import no.nav.sbl.dialogarena.websoknad.pages.startsoknad.StartSoknadPage;
import no.nav.sbl.dialogarena.websoknad.service.WebSoknadService;

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.form.AjaxButton;
import org.apache.wicket.markup.html.TransparentWebMarkupContainer;
import org.apache.wicket.markup.html.WebPage;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.DropDownChoice;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.link.ExternalLink;
import org.apache.wicket.markup.html.link.Link;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.list.ListView;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.LoadableDetachableModel;
import org.apache.wicket.model.PropertyModel;
import org.apache.wicket.request.Url;
import org.apache.wicket.request.http.handler.RedirectRequestHandler;
import org.apache.wicket.request.mapper.parameter.PageParameters;

import javax.inject.Inject;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static no.nav.sbl.dialogarena.dokumentinnsending.domain.Dokument.Type.EKSTERNT_VEDLEGG;
import static no.nav.sbl.dialogarena.dokumentinnsending.domain.Dokument.Type.EKSTRA_VEDLEGG;
import static no.nav.sbl.dialogarena.dokumentinnsending.domain.Dokument.Type.NAV_VEDLEGG;

public class HomePage extends WebPage {

    @Inject
    private BrukerBehandlingServiceIntegration brukerBehandlingService;

    @Inject
    private SoknadService soknadService;

    @Inject
    private SoknadRepository soknadRepository;

    private List<String> soknader = Arrays.asList("Krav om dagpenger", "Søknad om overgangsstønad, stønad til barnetilsyn, utdanningsstønad og flytteutgifter til enslig forsørger", "Sjøfartsbok/hyreavregning");
    private String valgtSoknad = soknader.get(0);

    private List<String> soknadTyper = Arrays.asList("Innsending", "Ettersending");
    private String soknadType = soknadTyper.get(0);

    
    @Inject
    private WebSoknadService websoknadService;
    
    private List<String> websoknader = Arrays.asList("Tullesøknad");
    private List<String> gosysId = Arrays.asList("1");
    private String valgtWebSoknad = soknader.get(0);
    
    public HomePage() {
        super(new PageParameters());

        soknadRepository.lagSoknader();

        TransparentWebMarkupContainer body = new TransparentWebMarkupContainer("body");
        body.setOutputMarkupId(true);
        add(body);

        LoadableDetachableModel<String> aktorId = new LoadableDetachableModel<String>() {
            @Override
            protected String load() {
                return SubjectHandler.getSubjectHandler().getUid();
            }
        };
        add(new Label("aktorId", aktorId));

        add(new ListView<String>("eksisterendeSoknadListe", hentBrukerBehandlingIder()) {
            @Override
            protected void populateItem(final ListItem<String> item) {
                LoadableDetachableModel<String> url = new LoadableDetachableModel<String>() {
                    @Override
                    protected String load() {
                        return getRequestCycle().getUrlRenderer().renderFullUrl(Url.parse(String.format("../oversikt/%s", item.getModelObject())));
                    }
                };
                ExternalLink oversiktLink = new ExternalLink("eksisterendeSoknadLink", url);
                oversiktLink.add(new Label("eksisterendeSoknadText", item.getModel()));
                item.add(oversiktLink);
            }
        });
       
        Form form = new Form("opprettSoknadForm");
        add(form);
        form.add(new Label("bruker", aktorId));

        DropDownChoice soknadList = new DropDownChoice("soknad", new PropertyModel(this, "valgtSoknad"), soknader);
        form.add(soknadList);

        DropDownChoice soknadTypeList = new DropDownChoice("soknadType", new PropertyModel(this, "soknadType"), soknadTyper);
        form.add(soknadTypeList);

        AjaxButton submit = new AjaxButton("submit") {
            @Override
            protected void onSubmit(AjaxRequestTarget target, Form<?> form) {
                boolean ettersending = soknadType.equals(soknadTyper.get(1));
                DokumentSoknad nySoknad = soknadRepository.hentSoknadMedHovedskjema(valgtSoknad);
                String behandlingsId = opprettSoknad(nySoknad, ettersending);
                String url = getRequestCycle().getUrlRenderer().renderFullUrl(Url.parse(String.format("../startBrukerbehandling/%s", behandlingsId)));
                getRequestCycle().scheduleRequestHandlerAfterCurrent(new RedirectRequestHandler(url));
            }
        };
        form.add(submit);
      
        //WEBSOKNAD
        add(new ListView<Long>("eksisterendeWebSoknadListe", hentSoknadIder("HARDCODED_VALUE_OVERRIDDEN_IN_HENVENDELSE")) {
            @Override
            protected void populateItem(final ListItem<Long> item) {
                Link link = new Link("eksisterendeWebSoknadLink") {
                    @Override
                    public void onClick() {
                        setResponsePage(OpenSoknadPage.class, new PageParameters().set("soknadId", item.getModelObject()));
                    }
                };
                link.add(new Label("eksisterendeWebSoknadText", item.getModelObject()));
                item.add(link);
            }
        });
                
        Form websoknadForm = new Form("websoknadForm") {
            @Override
            protected void onSubmit() {
                String id = gosysId.get(websoknader.indexOf(valgtWebSoknad));
                setResponsePage(StartSoknadPage.class, new PageParameters().set("navSoknadId", id));

            }
        };
        add(websoknadForm);
        websoknadForm.add(new Label("bruker", aktorId));
        DropDownChoice websoknadList = new DropDownChoice("websoknad", new PropertyModel(this, "valgtWebSoknad"), websoknader);
        websoknadForm.add(websoknadList);
    }
    
    private List<Long> hentSoknadIder(String aktorId) {
        return websoknadService.hentMineSoknader(aktorId);
    }

    private String opprettSoknad(DokumentSoknad soknad, boolean erEttersending) {
        String hovedskjema = soknad.hovedskjema.getKodeverkId();
        List<String> vedlegg = getKodeverksIder(soknad.finnVedleggAvType(NAV_VEDLEGG), soknad.finnVedleggAvType(EKSTERNT_VEDLEGG), soknad.finnVedleggAvType(EKSTRA_VEDLEGG));
        return brukerBehandlingService.opprettDokumentBehandling(hovedskjema, vedlegg, erEttersending);
    }

    private List<String> getKodeverksIder(List<? extends Dokument> navVedlegg, List<? extends Dokument> eksterntVedlegg, List<? extends Dokument> annetVedlegg) {
        List<String> list = new ArrayList<>();
        for (Dokument dok : navVedlegg) {
            list.add(dok.getKodeverkId());
        }

        for (Dokument dok : eksterntVedlegg) {
            list.add(dok.getKodeverkId());
        }

        for (Dokument dok : annetVedlegg) {
            list.add(dok.getKodeverkId());
        }
        return list;
    }

    private IModel<List<String>> hentBrukerBehandlingIder() {
        return new LoadableDetachableModel<List<String>>() {
            @Override
            protected List<String> load() {
                List<String> behandlingsIder = brukerBehandlingService.hentBrukerBehandlingIder(SubjectHandler.getSubjectHandler().getUid());
                List<String> aktiveBehandlinger = new ArrayList<>();
                for (String id : behandlingsIder) {
                    DokumentSoknad soknad = soknadService.hentSoknad(id);
                    if (soknad.status.er(SoknadStatus.UNDER_ARBEID)) {
                        aktiveBehandlinger.add(id);
                    }
                }
                return aktiveBehandlinger;
            }
        };
    }
}