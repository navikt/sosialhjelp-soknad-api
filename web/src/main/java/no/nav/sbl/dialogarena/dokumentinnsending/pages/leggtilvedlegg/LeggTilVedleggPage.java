package no.nav.sbl.dialogarena.dokumentinnsending.pages.leggtilvedlegg;

import no.nav.sbl.dialogarena.dokumentinnsending.common.DokumentinnsendingParameters;
import no.nav.sbl.dialogarena.dokumentinnsending.pages.oversikt.OversiktPage;
import no.nav.sbl.dialogarena.dokumentinnsending.pages.base.modalbasepage.ModalBasePage;
import no.nav.sbl.dialogarena.dokumentinnsending.pages.felles.behaviors.DefaultLoadingBehavior;
import no.nav.sbl.dialogarena.dokumentinnsending.pages.felles.behaviors.TextValidationJSBehavior;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.form.AjaxButton;
import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.head.OnLoadHeaderItem;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.RequiredTextField;
import org.apache.wicket.model.Model;
import org.apache.wicket.request.mapper.parameter.PageParameters;
import org.apache.wicket.validation.validator.PatternValidator;
import org.apache.wicket.validation.validator.StringValidator;

public class LeggTilVedleggPage extends ModalBasePage {

    RequiredTextField<String> beskrivelse;

    public LeggTilVedleggPage(PageParameters parameters) {
        super(parameters);

        add(new Label("tabTittel", new Model(cmsContentRetriever.hentTekst("leggtilvedlegg.sideTittel"))));
        add(new Label("tittel", new Model(cmsContentRetriever.hentTekst("leggtilvedlegg.tittel"))));
        add(new Label("beskrivelse", new Model(cmsContentRetriever.hentTekst("leggtilvedlegg.beskrivelse"))));

        int maxLength = 25;
        String pattern = "^[-a-zA-Z0-9æøåÆØÅ _+§%()]+$";

        Form form = new Form("vedleggForm");
        form.add(new Label("beskrivelseLabel", new Model(cmsContentRetriever.hentTekst("leggtilvedlegg.beskrivelseLabel"))));

        beskrivelse = new RequiredTextField<>("beskrivelse", Model.of(""));
        beskrivelse.add(new PatternValidator(pattern));
        beskrivelse.add(StringValidator.maximumLength(maxLength));
        TextValidationJSBehavior validationJSBehavior = new TextValidationJSBehavior()
                .setPattern(pattern, "beskrivelse.PatternValidator")
                .setMaxLength(maxLength, "beskrivelse.StringValidator.maximum")
                .setRequired(true, "beskrivelse.Required");
        beskrivelse.add(validationJSBehavior);
        form.add(beskrivelse);

       Model bekreftModel = new Model(cmsContentRetriever.hentTekst("leggtilvedlegg.bekreftOpprettelse"));

        AjaxButton bekreft = new AjaxButton("bekreft", bekreftModel, form) {
            @Override
            protected void onSubmit(AjaxRequestTarget target, Form<?> form) {
                Long dokumentForventingsId = soknadService.leggTilVedlegg(behandlingsId, beskrivelse.getModelObject());
                setResponsePage(OversiktPage.class, new DokumentinnsendingParameters(behandlingsId).scrollTo(dokumentForventingsId));
            }

            @Override
            protected void onError(AjaxRequestTarget target, Form<?> form) {
                super.onError(target, form);
                target.appendJavaScript(String.format("hideLoadingIndicator('%s');", this.getMarkupId()));
            }
        };
        bekreft.add(new DefaultLoadingBehavior());
        form.add(bekreft);

        add(form);
    }

    @Override
    public void renderHead(IHeaderResponse response) {
        response.render(OnLoadHeaderItem.forScript(String.format("$('#%s').focus();", beskrivelse.getMarkupId())));
        super.renderHead(response);
    }
}
