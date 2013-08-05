package no.nav.sbl.dialogarena.soknad.behaviors.util;

import org.apache.wicket.behavior.AttributeAppender;
import org.apache.wicket.model.AbstractReadOnlyModel;

public class InputType extends AttributeAppender {
    public InputType(final String value) {
        super("type", new AbstractReadOnlyModel<String>() {
            @Override
            public String getObject() {
                return value;
            }
        });
    }
}
