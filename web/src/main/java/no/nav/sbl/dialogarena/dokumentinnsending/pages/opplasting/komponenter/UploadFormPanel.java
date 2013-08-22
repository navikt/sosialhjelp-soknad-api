package no.nav.sbl.dialogarena.dokumentinnsending.pages.opplasting.komponenter;

import no.nav.modig.wicket.events.NamedEventPayload;
import no.nav.sbl.dialogarena.dokumentinnsending.pages.felles.behaviors.OnSubmitFileLoadingBehavior;
import no.nav.sbl.dialogarena.dokumentinnsending.pages.opplasting.OpplastetFil;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.form.AjaxButton;
import org.apache.wicket.event.Broadcast;
import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.head.OnDomReadyHeaderItem;
import org.apache.wicket.markup.html.form.Button;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.upload.FileUpload;
import org.apache.wicket.markup.html.form.upload.FileUploadField;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.ResourceModel;

import java.util.ArrayList;
import java.util.List;

import static no.nav.sbl.dialogarena.dokumentinnsending.pages.felles.utils.LocalConditionalUtils.hasCssClass;
import static no.nav.sbl.dialogarena.dokumentinnsending.pages.opplasting.OpplastetFil.OPPLASTET_FIL_TRANSFORMER;

public class UploadFormPanel extends Panel {

    private final String opplastingEvent;

    public UploadFormPanel(String id, ResourceModel buttonModel, String fakeUploadClass, String opplastingEvent) {
        super(id);
        this.opplastingEvent = opplastingEvent;
        setOutputMarkupId(true);
        UploadForm form = new UploadForm("form", buttonModel, fakeUploadClass);
        add(form);
    }

    protected void performActionOnSubmit(AjaxRequestTarget target) {
    }

    private class UploadForm extends Form<Void> {
        private final FileUploadField fileUploadField;

        public UploadForm(String id, ResourceModel buttonModel, String fakeUploadClass) {
            super(id);

            setMultiPart(true);

            fileUploadField = new FileUploadField("fileInput");
            add(fileUploadField.setOutputMarkupId(true));

            Button fakeUpload = new Button("fakeUpload", buttonModel) {
                @Override
                public void renderHead(IHeaderResponse response) {
                    String js = "$('.fakeUpload').click(function () {"
                            + "     $('#fileInput').click();"
                            + "});"
                            + "$('#fileInput').change(function() {"
                            + "     $('INPUT.submit').click();"
                            + "});";
                    response.render(OnDomReadyHeaderItem.forScript(js));
                }
            };
            fakeUpload.add(hasCssClass(fakeUploadClass));
            add(fakeUpload);

            AjaxButton submit = new AjaxButton("submit") {
                @Override
                public void onSubmit(AjaxRequestTarget target, Form<?> form) {
                    performActionOnSubmit(target);
                }
            };
            submit.add(new OnSubmitFileLoadingBehavior(fakeUpload));
            submit.setOutputMarkupId(true);
            add(submit);
        }

        @Override
        protected void onSubmit() {
            super.onSubmit();
            //IKKE bruk modig iterUtils. Den gjør kun en dekorering slik at det opprinnelige objektet vil være igjen.
            List<OpplastetFil> filer = new ArrayList<>();
            if (fileUploadField.getFileUploads() != null) {
                for (FileUpload fileUpload : fileUploadField.getFileUploads()) {
                    filer.add(OPPLASTET_FIL_TRANSFORMER.transform(fileUpload));
                }
                fileUploadField.clearInput();
            }
            send(getPage(), Broadcast.DEPTH, new NamedEventPayload(opplastingEvent, filer));
        }
    }
}