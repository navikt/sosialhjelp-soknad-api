package no.nav.sbl.dialogarena.dokumentinnsending.pages.oversikt;

import no.nav.modig.wicket.events.annotations.RunOnEvents;
import no.nav.sbl.dialogarena.dokumentinnsending.domain.Dokument;
import no.nav.sbl.dialogarena.dokumentinnsending.service.SoknadService;
import org.apache.wicket.ajax.AbstractAjaxTimerBehavior;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.head.OnLoadHeaderItem;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.panel.GenericPanel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.util.time.Duration;

import javax.inject.Inject;

import static no.nav.modig.wicket.conditional.ConditionalUtils.visibleIf;
import static no.nav.modig.wicket.model.ModelUtils.both;
import static no.nav.modig.wicket.model.ModelUtils.not;
import static no.nav.modig.wicket.model.ModelUtils.when;
import static no.nav.sbl.dialogarena.dokumentinnsending.domain.Dokument.harDokumentInnhold;
import static no.nav.sbl.dialogarena.dokumentinnsending.domain.Dokument.harIkkeDokumentInnhold;

public class DokumentPanel extends GenericPanel<Dokument> {

    public static final String OPPDATER_DOKUMENTBOKS = "OppdaterDokument";

    private IModel<Boolean> loadingModel;

    @Inject
    private SoknadService soknadService;


    public DokumentPanel(String id, IModel<Dokument> dokument, IModel<Boolean> loading) {
        super(id, dokument);
        loadingModel = loading;

        add(new OpplastetDokument("opplastetInnhold", getModel())
                .add(visibleIf(when(dokument, harDokumentInnhold()))));

        add(new IkkeOpplastetDokument("ikkeOpplastetInnhold", getModel()).
                add(visibleIf(both(when(dokument, harIkkeDokumentInnhold()))
                        .and(not(loadingModel)))));

        add(new WebMarkupContainer("loadingIcon")
                .add(visibleIf(both(loadingModel)
                        .and(not(when(dokument, harDokumentInnhold()))))));

        add(new AbstractAjaxTimerBehavior(Duration.seconds(1)) {
            @Override
            protected void onTimer(AjaxRequestTarget target) {
                if (loadingModel.getObject()) {
                    loadingModel.setObject(false);
                    target.add(this.getComponent().getParent());
                }
                stop(target);
            }
        });
    }

    @RunOnEvents(OPPDATER_DOKUMENTBOKS)
    private void oppdaterDokument(AjaxRequestTarget target, Dokument dokument) {
        Dokument detteDokumentet = getModel().getObject();
        if (detteDokumentet.er(dokument)) {
            getModel().setObject(soknadService.hentOppdatertDokument(detteDokumentet));
            target.add(this.getParent());
        }
    }

    @Override
    public void renderHead(IHeaderResponse response) {
        super.renderHead(response);

        if (loadingModel.getObject()) {
            response.render(OnLoadHeaderItem.forScript(String.format("scrollToElement('%s');", getMarkupId())));
        }
    }
}
