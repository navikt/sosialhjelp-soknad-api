package no.nav.sbl.dialogarena.dokumentinnsending.pages.bekreft;

import no.nav.modig.core.exception.ApplicationException;
import no.nav.sbl.dialogarena.dokumentinnsending.common.BrukerBehandlingId;
import no.nav.sbl.dialogarena.dokumentinnsending.domain.Dokument;
import no.nav.sbl.dialogarena.dokumentinnsending.domain.Person;
import no.nav.sbl.dialogarena.dokumentinnsending.domain.DokumentSoknad;
import no.nav.sbl.dialogarena.dokumentinnsending.domain.SoknadStatus;
import no.nav.sbl.dialogarena.dokumentinnsending.pages.base.mainbasepage.MainBasePage;
import no.nav.sbl.dialogarena.dokumentinnsending.pages.felles.DokumentListeInnsendingsvalg;
import no.nav.sbl.dialogarena.dokumentinnsending.pages.felles.Feilmelding;
import no.nav.sbl.dialogarena.dokumentinnsending.pages.felles.OpenPageLink;
import no.nav.sbl.dialogarena.dokumentinnsending.pages.felles.StegIndikator;
import no.nav.sbl.dialogarena.dokumentinnsending.pages.felles.behaviors.BaseLoadingBehavior;
import no.nav.sbl.dialogarena.dokumentinnsending.pages.felles.behaviors.DefaultLoadingBehavior;
import no.nav.sbl.dialogarena.dokumentinnsending.pages.felles.validators.CheckboxValidator;
import no.nav.sbl.dialogarena.dokumentinnsending.pages.fortsettsenere.FortsettSenerePage;
import no.nav.sbl.dialogarena.dokumentinnsending.pages.innsendingkvittering.InnsendingKvitteringPage;
import no.nav.sbl.dialogarena.dokumentinnsending.pages.oversikt.OversiktPage;
import no.nav.sbl.dialogarena.dokumentinnsending.service.BrukerBehandlingServiceIntegration;
import no.nav.sbl.dialogarena.dokumentinnsending.service.PersonService;
import no.nav.sbl.dialogarena.dokumentinnsending.service.SoknadService;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.form.AjaxButton;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.CheckBox;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.RadioGroup;
import org.apache.wicket.markup.html.link.ExternalLink;
import org.apache.wicket.model.CompoundPropertyModel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.LoadableDetachableModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.PropertyModel;
import org.apache.wicket.request.mapper.parameter.PageParameters;

import javax.inject.Inject;
import java.util.Arrays;
import java.util.List;

import static no.nav.modig.wicket.conditional.ConditionalUtils.visibleIf;
import static no.nav.sbl.dialogarena.dokumentinnsending.pages.bekreft.AdresseValg.VALG_1;
import static no.nav.sbl.dialogarena.dokumentinnsending.pages.bekreft.AdresseValg.VALG_2;
import static no.nav.sbl.dialogarena.dokumentinnsending.pages.bekreft.AdresseValg.VALG_3;

/**
 * Viser en oversikt over hvilke dokumenter som skal sendes inn eller ikke.
 * Før brukeren får sende inn søknad må bruker bekrefte at gitte opplysninger er
 * korrekte. Dersom bruker står registrert med utenlandsk adresse må brukeren velge
 * ett av tre alternativer.
 */
public class BekreftelsesPage extends MainBasePage {

    @Inject
    private SoknadService soknadService;

    @Inject
    private BrukerBehandlingServiceIntegration brukerBehandlingServiceIntegration;

    @Inject
    private PersonService personService;


    public BekreftelsesPage() {
        this(new PageParameters());
    }

    public BekreftelsesPage(PageParameters parameters) {
        super(parameters);

        final String behandlingsId = BrukerBehandlingId.get(parameters);

        setDefaultModel(new CompoundPropertyModel<>(new LoadableDetachableModel<BekreftViewModel>() {
            @Override
            protected BekreftViewModel load() {

                DokumentSoknad soknad = soknadService.hentSoknad(behandlingsId);
                Person person = personService.hentPerson(soknad.ident);
                return new BekreftViewModel(soknad, person, cmsContentRetriever);
            }
        }));

        add(new StegIndikator("stegIndikator"));
        Form form = new Form("bekreftelsesForm");
        form.add(new Label("bekreftelsesside.beskrivelse", cmsContentRetriever.hentTekst("bekreftelsesside.beskrivelse")));
        form.add(new Label("beskrivelse.ettersending.gaaTilLenke", cmsContentRetriever.hentTekst("bekreftelsesside.beskrivelse")));
        form.add(new Label("beskrivelse.ettersending.tekst", cmsContentRetriever.hentTekst("beskrivelse.ettersending.tekst")));
        form.add(new Label("bekreftelsesside.bekreftSamtykke", cmsContentRetriever.hentTekst("bekreftelsesside.bekreftSamtykke")));
        form.add(new Label("bekreftelsesside.samtykke", cmsContentRetriever.hentTekst("bekreftelsesside.samtykke")));

        IModel<List<Dokument>> innsendteDokumenter = new LoadableDetachableModel<List<Dokument>>() {
            @Override
            protected List<Dokument> load() {
                BekreftViewModel defaultModelObject = (BekreftViewModel) BekreftelsesPage.this.getDefaultModelObject();
                return defaultModelObject.getInnsendteDokumenter();
            }
        };
        IModel<List<Dokument>> ikkeSendteDokumenter = new LoadableDetachableModel<List<Dokument>>() {
            @Override
            protected List<Dokument> load() {
                BekreftViewModel defaultModelObject = (BekreftViewModel) BekreftelsesPage.this.getDefaultModelObject();
                return defaultModelObject.getIkkeSendteDokumenter();
            }
        };

        form.add(new Label("sideTittel"));
        form.add(new DokumentListeInnsendingsvalg("innsendteDokumenter", innsendteDokumenter,  "dokumentliste.sendteDokumenter", true));
        form.add(new DokumentListeInnsendingsvalg("ikkeSendteDokumenter", ikkeSendteDokumenter, "dokumentliste.manglendeDokumenter", false));
        form.add(new ExternalLink("skjema", cmsContentRetriever.hentTekst("beskrivelse.ettersending.lenke"), cmsContentRetriever.hentTekst("beskrivelse.ettersending.lenkeTekst")));
        OpenPageLink tilbakeLink = new OpenPageLink("tilbake", OversiktPage.class, behandlingsId);
        form.add(tilbakeLink);
        tilbakeLink.add(new Label("tilbakeTekst", cmsContentRetriever.hentTekst("tilbake")));
        OpenPageLink fortsettsenereLink = new OpenPageLink("fortsettSenere", FortsettSenerePage.class, behandlingsId);
        form.add(fortsettsenereLink);
        fortsettsenereLink.add(new Label("fortsettSenereTekst", cmsContentRetriever.hentTekst("fortsettSenere")));

        final Feilmelding feedbackPanel = new Feilmelding("tilbakemelding", cmsContentRetriever.hentTekst("validate.informasjon.mangler"));
        form.add(feedbackPanel);

        WebMarkupContainer utenlandskAdresse = new WebMarkupContainer("utenlandskAdresse");
        utenlandskAdresse.add(new Label("bekreftelsesside.adresse.beskrivelse", cmsContentRetriever.hentTekst("bekreftelsesside.adresse.beskrivelse")));
        utenlandskAdresse.add(new Label("bekreftelsesside.adresse", cmsContentRetriever.hentTekst("bekreftelsesside.adresse")));
        utenlandskAdresse.add(visibleIf(new PropertyModel<Boolean>(getDefaultModel(), "harUtenlandskAdresse")));
        form.add(utenlandskAdresse);

        final RadioGroup adresseGruppe = adresseInformasjon();
        adresseGruppe.setRequired(true);
        utenlandskAdresse.add(adresseGruppe);

        CheckBox samtykket = new CheckBox("samtykket");
        samtykket.add(new CheckboxValidator());
        form.add(samtykket);

        AjaxButton submit = new AjaxButton("submit") {
            @Override
            protected void onSubmit(AjaxRequestTarget target, Form<?> form) {
                String journalFoerendeEnhet = AdresseValg.journalFoerendeEnhet((String) adresseGruppe.getModelObject());
                brukerBehandlingServiceIntegration.sendBrukerBehandling(behandlingsId, journalFoerendeEnhet);
                setResponsePage(InnsendingKvitteringPage.class, new PageParameters().set("brukerBehandlingId", behandlingsId));
            }

            @Override
            protected void onError(AjaxRequestTarget target, Form<?> form) {
                target.add(feedbackPanel);
                target.appendJavaScript(String.format("hideLoadingIndicator('%s');", this.getMarkupId()));
            }
        };
        submit.add(new DefaultLoadingBehavior(BaseLoadingBehavior.Colour.GRAA));
        form.add(submit);

        add(form);
    }

    private RadioGroup adresseInformasjon() {
        RadioGroup adresseGruppe = new RadioGroup("adresseGruppe", new Model<String>());

        adresseGruppe.add(new AdresseValg(VALG_1, adresseGruppe, Arrays.asList("bekreftelsesside.feilAdresse")));
        adresseGruppe.add(new AdresseValg(VALG_2, adresseGruppe, Arrays.asList("bekreftelsesside.ikkeNorge", "bekreftelsesside.ikkeNorge.seksMaaneder", "bekreftelsesside.ikkeNorge.trygd")));
        adresseGruppe.add(new AdresseValg(VALG_3, adresseGruppe, Arrays.asList("bekreftelsesside.ikkeNorge", "bekreftelsesside.ikkeNorge.arbeider", "bekreftelsesside.ikkeNorge.studerer", "bekreftelsesside.ikkeNorge.vedtak", "bekreftelsesside.ikkeNorge.mottar")));

        return adresseGruppe;
    }

    @Override
    protected void onConfigure() {
        validerAtTilstandTillaterAApneSiden();
        super.onConfigure();
    }

    // Validerer at søknaden har tilstand som tillater å gå til denne siden
    private void validerAtTilstandTillaterAApneSiden() {
        BekreftViewModel viewModel = (BekreftViewModel) getDefaultModelObject();

        if (viewModel.status.erIkke(SoknadStatus.UNDER_ARBEID)) {
            throw new ApplicationException("Henvendelsen er avsluttet");
        } else if (viewModel.getInnsendteDokumenter().isEmpty()) {
            throw new ApplicationException("Henvendelsen har ingen opplastede dokumenter");
        }
    }
}
