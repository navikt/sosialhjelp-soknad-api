package no.nav.sbl.dialogarena.dokumentinnsending.pages.fortsettsenere;

import no.nav.sbl.dialogarena.dokumentinnsending.common.BrukerBehandlingId;
import no.nav.sbl.dialogarena.dokumentinnsending.domain.DokumentSoknad;
import no.nav.sbl.dialogarena.dokumentinnsending.pages.oversikt.OversiktPage;
import no.nav.sbl.dialogarena.dokumentinnsending.pages.base.mainbasepage.MainBasePage;
import no.nav.sbl.dialogarena.dokumentinnsending.pages.felles.DittNavLink;
import no.nav.sbl.dialogarena.dokumentinnsending.pages.felles.OpenPageLink;
import no.nav.sbl.dialogarena.dokumentinnsending.service.SoknadService;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.link.BookmarkablePageLink;
import org.apache.wicket.model.CompoundPropertyModel;
import org.apache.wicket.model.LoadableDetachableModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.StringResourceModel;
import org.apache.wicket.request.mapper.parameter.PageParameters;

import javax.inject.Inject;


/**
 * Klasse for å gi brukeren en kvittering på at ønsket om å fortsette søknaden senere gikk igjennom. Dette innebærer at brukeren fikk tilsendt en mail med link til søknaden.
 */
public class FortsettSenereKvitteringPage extends MainBasePage {
    @Inject
    private SoknadService soknadService;

    public FortsettSenereKvitteringPage(PageParameters parameters, String sendtTo) {
        super(parameters);

        final String behandlingsId = BrukerBehandlingId.get(parameters);
        OpenPageLink tilOversiktLink = new OpenPageLink("tilOversikt", OversiktPage.class, behandlingsId);
        add(tilOversiktLink);
        setDefaultModel(new CompoundPropertyModel<>(new LoadableDetachableModel<FortsettSenereViewModel>() {
            @Override
            protected FortsettSenereViewModel load() {
                DokumentSoknad soknad = soknadService.hentSoknad(behandlingsId);
                return new FortsettSenereViewModel(soknad, cmsContentRetriever);
            }
        }));
        add(new Label("sideTittel"));
        add(new Label("fortsettSenere.beskrivelse", cmsContentRetriever.hentTekst("fortsettSenere.beskrivelse")));
        tilOversiktLink.add(new Label("fortsettSenere.oversiktSide", cmsContentRetriever.hentTekst("fortsettSenere.oversiktSide")));
        Label sendPaaNyttLabel = (new Label("fortsettSenere.sendPaaNytt", cmsContentRetriever.hentTekst("fortsettSenere.sendPaaNytt")));
        add(new DittNavLink("dittnav", "fortsettSenere.dittnav"));

        add(new Label("epostSendtTil", new StringResourceModel("fortsettSenere.epostSendt", Model.of(new EpostInput.EmailModel().with(sendtTo, "")))));
        BookmarkablePageLink sendPaaNyttLink = new BookmarkablePageLink<>("sendPaaNytt", FortsettSenerePage.class, getPageParameters());
        add(sendPaaNyttLink);
        sendPaaNyttLink.add(sendPaaNyttLabel);

    }
}