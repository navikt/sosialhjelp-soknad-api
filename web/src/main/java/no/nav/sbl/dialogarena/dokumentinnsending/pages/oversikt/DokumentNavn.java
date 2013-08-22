package no.nav.sbl.dialogarena.dokumentinnsending.pages.oversikt;

import no.nav.modig.wicket.events.NamedEventPayload;
import no.nav.sbl.dialogarena.dokumentinnsending.domain.Dokument;
import no.nav.sbl.dialogarena.dokumentinnsending.pages.felles.behaviors.TextValidationJSBehavior;
import no.nav.sbl.dialogarena.dokumentinnsending.service.SoknadService;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.event.Broadcast;
import org.apache.wicket.extensions.ajax.markup.html.AjaxEditableLabel;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.AbstractReadOnlyModel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.validation.validator.PatternValidator;
import org.apache.wicket.validation.validator.StringValidator;

import javax.inject.Inject;

import static org.apache.commons.lang3.StringUtils.substringAfter;
import static org.apache.commons.lang3.StringUtils.substringBefore;

public class DokumentNavn extends Panel {
    @Inject
    private SoknadService soknadService;

    public static final String OPPDATER_VEDLEGGLISTE = "OppdaterVedleggsListe";

    public DokumentNavn(String id, final IModel<Dokument> dokument) {
        super(id);

        final int maxLength = 25;
        final String pattern = "^[-a-zA-Z0-9æøåÆØÅ _+§%()]+$";

        this.setOutputMarkupId(true);

        AjaxEditableLabel dokumentNavn = new AjaxEditableLabel("dokumentNavn",
                new AbstractReadOnlyModel<String>() {
                    @Override
                    public String getObject() {
                        return dokument.getObject().getNavn();
                    }
                }) {
            @Override
            public void onEdit(AjaxRequestTarget target) {
                super.onEdit(target);
                setDefaultModel(Model.of(substringAfter(dokument.getObject().getNavn(), ":")));
                TextValidationJSBehavior validationJSBehavior = new TextValidationJSBehavior()
                        .setPattern(pattern, "beskrivelse.PatternValidator")
                        .setMaxLength(maxLength, "beskrivelse.StringValidator.maximum")
                        .setRequired(true, "beskrivelse.Required")
                        .setRemoveOnFocus(false);
                get("editor").add(validationJSBehavior);
                target.add(this.getParent());
            }

            @Override
            protected void onCancel(AjaxRequestTarget target) {
                super.onCancel(target);
                String prefix = substringBefore(dokument.getObject().getNavn(), ":");
                setDefaultModel(Model.of(prefix + ": " + getDefaultModelObject()));
                target.add(this.getParent());

                String js = String.format("$('#%s').closest('.form-linje').removeClass('feil');", getMarkupId());
                target.appendJavaScript(js);
            }

            @Override
            protected void onSubmit(AjaxRequestTarget target) {
                super.onSubmit(target);
                String nyBeskrivelse = (String) getDefaultModelObject();
                String nyttNavn = substringBefore(dokument.getObject().getNavn(), ":") + ": " + nyBeskrivelse;
                if (dokument.getObject().getNavn().equals(nyttNavn)) {
                    setDefaultModel(Model.of(nyttNavn));
                } else {
                    soknadService.oppdaterBeskrivelseAnnetVedlegg(dokument.getObject(), nyBeskrivelse);
                    send(getPage(), Broadcast.DEPTH, new NamedEventPayload(OPPDATER_VEDLEGGLISTE));
                }
                String js = String.format("$('#%s').closest('.form-linje').removeClass('feil');", getMarkupId());
                target.appendJavaScript(js);
            }

            @Override
            protected void onError(AjaxRequestTarget target) {
                super.onError(target);
                String js = String.format("$('#%s').closest('.form-linje').addClass('feil');", getMarkupId());
                target.appendJavaScript(js);
            }
        };
        dokumentNavn.add(new PatternValidator(pattern));
        dokumentNavn.add(StringValidator.maximumLength(maxLength));
        dokumentNavn.setRequired(true);
        add(dokumentNavn);
    }
}