package no.nav.sbl.dialogarena.dokumentinnsending.pages.opplasting;

import no.nav.modig.wicket.events.annotations.RunOnEvents;
import no.nav.sbl.dialogarena.dokumentinnsending.domain.Dokument;
import no.nav.sbl.dialogarena.dokumentinnsending.pages.base.modalbasepage.ModalBasePage;
import no.nav.sbl.dialogarena.dokumentinnsending.pages.opplasting.komponenter.UploadValidationUtil;
import org.apache.wicket.Component;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.request.mapper.parameter.PageParameters;

import java.util.ArrayList;
import java.util.List;

import static no.nav.modig.wicket.conditional.ConditionalUtils.visibleIf;
import static no.nav.modig.wicket.model.ModelUtils.not;

public class OpplastingPage extends ModalBasePage {

    private IModel<? extends List<OpplastetFil>> uploads;
    private IModel<Boolean> opplastingStartet;

    public OpplastingPage(PageParameters parameters) {
        super(parameters);

        opplastingStartet = Model.of(Boolean.FALSE);
        uploads = new Model<>(new ArrayList<OpplastetFil>());
        final long dokumentForventningId = parameters.get("dokumentId").toLong();
        IModel<Dokument> model = Model.of(soknadService.hentDokument(dokumentForventningId, behandlingsId));

        add(new Label("tabTittel", cmsContentRetriever.hentTekst("opplasting.sideTittel")));
        add(getSkjemaForType(model)
                .add(visibleIf(not(opplastingStartet))));
        add(new Opplasting("opplasting", uploads, model)
                .add(visibleIf(opplastingStartet)));
    }

    private Component getSkjemaForType(IModel<Dokument> model) {
        Dokument.Type type = model.getObject().getType();
        if (type == Dokument.Type.HOVEDSKJEMA) {
            return new StartOpplastingSkjema("skjema", model);
        } else if (type == Dokument.Type.NAV_VEDLEGG) {
            return new StartOpplastingNavVedlegg("skjema", model);
        } else {
            return new StartOpplastingEksterntVedlegg("skjema");
        }
    }

    @RunOnEvents(StartOpplasting.OPPLASTING_STARTET)
    private void opplastingStartet(AjaxRequestTarget target, List<OpplastetFil> uploads) {
        List<OpplastetFil> currentFiles = this.uploads.getObject();
        UploadValidationUtil.validateUploadedFiles(uploads, this, currentFiles);
        if (getFeedbackMessages() == null || getFeedbackMessages().isEmpty()) {
            currentFiles.addAll(uploads);
        }
        opplastingStartet.setObject(!opplastingStartet.getObject());
        target.add(this);
    }
}
