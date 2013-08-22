package no.nav.sbl.dialogarena.dokumentinnsending.pages.fortsettsenere;


import no.nav.modig.content.CmsContentRetriever;
import no.nav.modig.core.context.SubjectHandler;
import no.nav.modig.lang.option.Optional;
import no.nav.modig.wicket.events.NamedEventPayload;
import no.nav.modig.wicket.events.annotations.RunOnEvents;
import no.nav.modig.wicket.events.components.AjaxEventSubmitButton;
import no.nav.sbl.dialogarena.dokumentinnsending.domain.Person;
import no.nav.sbl.dialogarena.dokumentinnsending.pages.felles.behaviors.TextValidationJSBehavior;
import no.nav.sbl.dialogarena.dokumentinnsending.pages.felles.validators.EmailValidator;
import no.nav.sbl.dialogarena.dokumentinnsending.service.EmailService;
import no.nav.sbl.dialogarena.dokumentinnsending.service.PersonService;
import org.apache.wicket.Component;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.IAjaxIndicatorAware;
import org.apache.wicket.event.Broadcast;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.EmailTextField;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.panel.GenericPanel;
import org.apache.wicket.model.CompoundPropertyModel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.PropertyModel;
import org.apache.wicket.request.Url;

import javax.inject.Inject;
import javax.inject.Named;
import java.io.Serializable;
import java.util.LinkedHashMap;
import java.util.Map;

import static no.nav.modig.lang.option.Optional.optional;

public class EpostInput extends GenericPanel<EpostInput.EmailModel> implements IAjaxIndicatorAware {
    public static final String EPOST_KLIKKET = "fortsettSenere.epost.klikket";
    public static final String FORTSETT_SENERE_BYTT = "fortsettSenere.epost.send";

    @Inject
    protected CmsContentRetriever cmsContentRetriever;
    @Inject
    private EmailService epostUtsender;
    @Inject
    private PersonService personService;
    @Inject
    @Named("fortsettSenereUtmKey")
    private String utmConfig;

    private final String behandlingsId;
    private final String jsonValidationString = new TextValidationJSBehavior()
            .setRequired(true, "epost.Required")
            .setPattern("${input}" + EmailValidator.getInstance().getPattern().pattern(), "EmailValidator")
            .getJsonString();

    @Override
    public String getAjaxIndicatorMarkupId() {
        return "sendtEpostFortsettSenere";
    }

    public static class EmailModel implements Serializable {
        public String epost;
        private String skjemaNavn;

        public EmailModel with(String epost, String skjemaNavn) {
            this.epost = epost;
            this.skjemaNavn = skjemaNavn;
            return this;
        }

        public String getSkjemaNavn() {
            return skjemaNavn;
        }
    }

    public EpostInput(String id, String behandlingsId, String skjemaNavn) {
        super(id);
        this.behandlingsId = behandlingsId;

        Label epostLabel = new Label("fortsettSenere.epostLabel", cmsContentRetriever.hentTekst("fortsettSenere.epostLabel"));

        IModel<EmailModel> epostModel = new CompoundPropertyModel<>(new EmailModel().with(hentEpostForBruker(), skjemaNavn));
        setModel(epostModel);
        Form<EmailModel> form = new Form<>("epostForm", getModel());
        form.add(epostLabel);

        final Component epost = new EmailTextField("epost", new PropertyModel<String>(getModel(), "epost"), EmailValidator.getInstance())
                .setRequired(true);

        Component sendEpost = new AjaxEventSubmitButton("sendEpost", EPOST_KLIKKET) {
            @Override
            protected void onError(AjaxRequestTarget target, Form<?> form) {
                super.onError(target, form);
                String js = String.format("validate($('#%s'), %s, %b); deactiveButton('%s');", epost.getMarkupId(), jsonValidationString, true, getMarkupId());
                target.appendJavaScript(js);
            }
        };
        sendEpost.setOutputMarkupId(true);

        form.add(epost, sendEpost)
                .setOutputMarkupPlaceholderTag(true);

        add(form);
    }

    private String hentEpostForBruker() {
        Optional<Person> person = optional(personService.hentPerson(SubjectHandler.getSubjectHandler().getUid()));
        return person.isSome() ? person.get().getEpost() : "";
    }

    @RunOnEvents(EPOST_KLIKKET)
    public void sendEpost(AjaxRequestTarget target, EmailModel epost) {

        String url = getRequestCycle().getUrlRenderer().renderFullUrl(
                Url.parse(String.format("../oversikt/%s?%s", EpostInput.this.behandlingsId, utmConfig))
        );
        Map<String, String> resourceMap = new LinkedHashMap<>();
        resourceMap.put("url", url);
        resourceMap.put("skjemanavn", epost.getSkjemaNavn());

        String epostSubject = getLocalizer().getString("fortsettSenere.sendEpost.epostSubject", this, Model.ofMap(resourceMap)) + "${skjemanavn}";
        String epostTekst = getLocalizer().getString("fortsettSenere.sendEpost.epostInnhold", this, Model.ofMap(resourceMap))  + "${url}";
        epostUtsender.sendFortsettSenereEPost(epost.epost, epostSubject, epostTekst);
        send(this, Broadcast.EXACT, new NamedEventPayload(FORTSETT_SENERE_BYTT));
        setResponsePage(new FortsettSenereKvitteringPage(getPage().getPageParameters(), epost.epost));
    }
}