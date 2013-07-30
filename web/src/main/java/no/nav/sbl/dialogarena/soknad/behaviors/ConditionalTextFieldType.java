package no.nav.sbl.dialogarena.soknad.behaviors;

import org.apache.wicket.behavior.AttributeAppender;
import org.apache.wicket.model.AbstractReadOnlyModel;

public class ConditionalTextFieldType extends AttributeAppender {
    public ConditionalTextFieldType(final String value) {
        super("type", new AbstractReadOnlyModel<String>() {
            @Override
            public String getObject() {
                return value;
            }
        });
    }
}
