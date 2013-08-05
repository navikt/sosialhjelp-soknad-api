package no.nav.sbl.dialogarena.soknad.behaviors.util;

import org.apache.wicket.behavior.AttributeAppender;
import org.apache.wicket.model.AbstractReadOnlyModel;

public class LabelFor extends AttributeAppender {
    public LabelFor(final String value) {
        super("for", new AbstractReadOnlyModel<String>() {
            @Override
            public String getObject() {
                return value;
            }
        });
    }
}
