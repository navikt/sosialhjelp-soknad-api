package no.nav.sbl.dialogarena.dokumentinnsending.pages.felles.utils;

import org.apache.wicket.behavior.AttributeAppender;
import org.apache.wicket.model.Model;

class CssClassAppender extends AttributeAppender {

    public CssClassAppender(String value) {
        super("class", Model.of(value), " ");
    }

}
