package no.nav.sbl.dialogarena.dokumentinnsending.pages.oversikt;

import no.nav.modig.content.CmsContentRetriever;
import no.nav.modig.lang.collections.PredicateUtils;
import no.nav.modig.wicket.events.NamedEventPayload;
import no.nav.sbl.dialogarena.dokumentinnsending.common.DokumentinnsendingParameters;
import no.nav.sbl.dialogarena.dokumentinnsending.domain.Dokument;
import no.nav.sbl.dialogarena.dokumentinnsending.domain.InnsendingsValg;
import no.nav.sbl.dialogarena.dokumentinnsending.pages.felles.behaviors.ShowTextTooltipBehavior;
import no.nav.sbl.dialogarena.dokumentinnsending.pages.opplasting.OpplastingPage;
import no.nav.sbl.dialogarena.dokumentinnsending.service.SoknadService;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.form.AjaxCheckBox;
import org.apache.wicket.behavior.AttributeAppender;
import org.apache.wicket.event.Broadcast;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.link.Link;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.AbstractReadOnlyModel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.LoadableDetachableModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.PropertyModel;
import org.apache.wicket.model.StringResourceModel;
import org.apache.wicket.request.mapper.parameter.PageParameters;

import javax.inject.Inject;

import static no.nav.modig.wicket.conditional.ConditionalUtils.hasCssClassIf;
import static no.nav.modig.wicket.conditional.ConditionalUtils.visibleIf;
import static no.nav.modig.wicket.model.ModelUtils.not;
import static no.nav.modig.wicket.model.ModelUtils.when;
import static no.nav.sbl.dialogarena.dokumentinnsending.domain.Dokument.Type.EKSTRA_VEDLEGG;
import static no.nav.sbl.dialogarena.dokumentinnsending.domain.Dokument.Type.HOVEDSKJEMA;
import static no.nav.sbl.dialogarena.dokumentinnsending.domain.Dokument.Type.NAV_VEDLEGG;
import static no.nav.sbl.dialogarena.dokumentinnsending.domain.InnsendingsValg.IKKE_VALGT;
import static no.nav.sbl.dialogarena.dokumentinnsending.domain.InnsendingsValg.SENDES_IKKE;

public class IkkeOpplastetDokument extends Panel {

    @Inject
    protected CmsContentRetriever cmsContentRetriever;

    @Inject
    private SoknadService soknadService;

    public IkkeOpplastetDokument(String id, final IModel<Dokument> dokument) {
        super(id);
        setOutputMarkupId(true);


        IModel<Boolean> ikkeSendModel = new LoadableDetachableModel<Boolean>() {
            @Override
            protected Boolean load() {
                return dokument.getObject().er(SENDES_IKKE);
            }
        };

        add(hasCssClassIf("ikkeSend", ikkeSendModel));

        IModel<Boolean> valg = when(dokument, PredicateUtils.either(Dokument.avType(HOVEDSKJEMA)).or(Dokument.avType(NAV_VEDLEGG)));
        Link lastOpp = new Link<Void>("lastOppLink") {
            @Override
            public void onClick() {
                PageParameters parameters = new DokumentinnsendingParameters(dokument.getObject().getBehandlingsId())
                        .set("dokumentId", dokument.getObject().getDokumentForventningsId())
                        .set("type", dokument.getObject().getType().ordinal());
                setResponsePage(OpplastingPage.class, parameters);
            }
        };

        BooleanStringModel fyllUtHovedskjema = new BooleanStringModel(valg, "innsendingside.fyllUtHovedskjema", "innsendingside.fyllUtHovedskjema");
        StringResourceModel resourceModel = new StringResourceModel("${}", fyllUtHovedskjema);
        lastOpp.add(new Label("lastOppLinkTekst", resourceModel));
        lastOpp.add(visibleIf(not(ikkeSendModel)));
        add(lastOpp);

        WebMarkupContainer checkboxContainer = new WebMarkupContainer("checkboxContainer");
        checkboxContainer.add(visibleIf(when(dokument, Dokument.ikkeAvType(HOVEDSKJEMA))));
        AjaxCheckBox ajaxCheckBox = new AjaxCheckBox("ikkeSendCheckbox", ikkeSendModel) {
            @Override
            protected void onUpdate(AjaxRequestTarget target) {
                InnsendingsValg valg = getModelObject() ? SENDES_IKKE : IKKE_VALGT;
                dokument.getObject().setInnsendingsvalg(valg);
                soknadService.oppdaterInnsendingsvalg(dokument.getObject());
                send(getPage(), Broadcast.DEPTH, new NamedEventPayload(DokumentPanel.OPPDATER_DOKUMENTBOKS, dokument.getObject()));
            }
        };
        WebMarkupContainer label = new WebMarkupContainer("label");
        label.add(new AttributeAppender("for", Model.of(ajaxCheckBox.getMarkupId())));
        checkboxContainer.add(label);
        checkboxContainer.add(ajaxCheckBox);
        label.add(new Label("innsendingside.ikkeSend", cmsContentRetriever.hentTekst("innsendingside.ikkeSend")));
        Label dokumentNavn = new Label("dokumentNavn", new PropertyModel<>(dokument, "navn"));
        dokumentNavn.add(hasCssClassIf("lowercase", new PropertyModel<Boolean>(dokument, "erTittelOver30Tegn")));
        dokumentNavn.add(hasCssClassIf("strek-ikon-soknad", when(dokument, Dokument.avType(HOVEDSKJEMA))));
        dokumentNavn.add(hasCssClassIf("strek-ikon-dokument", when(dokument, Dokument.ikkeAvType(HOVEDSKJEMA))));
        add(dokumentNavn);

        DokumentNavn dokumentNavnEkstra = new DokumentNavn("dokumentNavnEkstra", dokument);
        dokumentNavnEkstra.add(hasCssClassIf("lowercase", new PropertyModel<Boolean>(dokument, "erTittelOver30Tegn")));
        dokumentNavnEkstra.add(hasCssClassIf("strek-ikon-dokument", when(dokument, Dokument.ikkeAvType(HOVEDSKJEMA))));
        add(dokumentNavnEkstra);

        dokumentNavn.add(visibleIf(when(dokument, Dokument.ikkeAvType(EKSTRA_VEDLEGG))));
        dokumentNavnEkstra.add(visibleIf(when(dokument, Dokument.avType(EKSTRA_VEDLEGG))));

        add(new Label("beskrivelse", lagBeskrivelseModel(dokument)).setEscapeModelStrings(true));

        add(checkboxContainer);

        checkboxContainer.add(new ShowTextTooltipBehavior(cmsContentRetriever.hentTekst("innsendingside.ikkeSend.tooltip"), "#" + checkboxContainer.getMarkupId() + " .infoikon"));
    }

    private LoadableDetachableModel<String> lagBeskrivelseModel(final IModel<Dokument> dokument) {
        return new LoadableDetachableModel<String>() {
            @Override
            protected String load() {
                if (dokument.getObject().er(SENDES_IKKE)) {
                    return cmsContentRetriever.hentTekst("innsendingside.sendesIkke");
                } else if (dokument.getObject().er(HOVEDSKJEMA)) {
                    return cmsContentRetriever.hentTekst("innsendingside.hovedskjema");
                } else if (dokument.getObject().er(NAV_VEDLEGG)) {
                    return cmsContentRetriever.hentTekst("innsendingside.navvedlegg");
                } else {
                    return cmsContentRetriever.hentTekst("innsendingside.egetvedlegg");
                }
            }
        };
    }

    private static class BooleanStringModel extends AbstractReadOnlyModel<String> {
        private IModel<Boolean> model;
        private String trueString;
        private String falseString;

        public BooleanStringModel(IModel<Boolean> model, String trueString, String falseString) {
            this.model = model;
            this.trueString = trueString;
            this.falseString = falseString;
        }

        @Override
        public String getObject() {
            return model.getObject() ? trueString : falseString;
        }
    }
}