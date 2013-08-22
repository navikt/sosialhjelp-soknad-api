package no.nav.sbl.dialogarena.dokumentinnsending.pages.fortsettsenere;

import no.nav.sbl.dialogarena.dokumentinnsending.common.BrukerBehandlingId;
import no.nav.sbl.dialogarena.dokumentinnsending.domain.DokumentSoknad;
import no.nav.sbl.dialogarena.dokumentinnsending.pages.base.mainbasepage.MainBasePage;
import no.nav.sbl.dialogarena.dokumentinnsending.pages.felles.DittNavLink;
import no.nav.sbl.dialogarena.dokumentinnsending.pages.felles.OpenPageLink;
import no.nav.sbl.dialogarena.dokumentinnsending.pages.oversikt.OversiktPage;
import no.nav.sbl.dialogarena.dokumentinnsending.service.SoknadService;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.model.CompoundPropertyModel;
import org.apache.wicket.model.LoadableDetachableModel;
import org.apache.wicket.request.mapper.parameter.PageParameters;

import javax.inject.Inject;

public class FortsettSenerePage extends MainBasePage {

    @Inject
    private SoknadService soknadService;

    public FortsettSenerePage(PageParameters parameters) {
        super(parameters);

        final String behandlingsId = BrukerBehandlingId.get(parameters);

        setDefaultModel(new CompoundPropertyModel<>(new LoadableDetachableModel<FortsettSenereViewModel>() {
            @Override
            protected FortsettSenereViewModel load() {
                DokumentSoknad soknad = soknadService.hentSoknad(behandlingsId);
                return new FortsettSenereViewModel(soknad, cmsContentRetriever);
            }
        }));
        OpenPageLink tilOversiktLink = (new OpenPageLink("tilOversikt", OversiktPage.class, behandlingsId));
        add(tilOversiktLink);
        add(new Label("sideTittel"));
        add(new Label("fortsettSenere.beskrivelse", cmsContentRetriever.hentTekst("fortsettSenere.beskrivelse")));
        tilOversiktLink.add(new Label("fortsettSenere.oversiktSide", cmsContentRetriever.hentTekst("fortsettSenere.oversiktSide")));
        add(new DittNavLink("dittnav", "fortsettSenere.dittnav"));
        add(new EpostInput("sendLink", behandlingsId, ((FortsettSenereViewModel) getDefaultModelObject()).getTittel()));

    }
}