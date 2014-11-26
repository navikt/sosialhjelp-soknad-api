package no.nav.sbl.dialogarena.websoknad.config;

import org.springframework.util.DefaultPropertiesPersister;

import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.util.Properties;

/**
 * Property persister som fjerner <p></p> rundt tekst for hver nøkkel ved lasting av propertyfiler
 */
public class StripPTagsPropertyPersister extends DefaultPropertiesPersister {

    private static final String P_START = "<p>";
    private static final String P_END = "</p>";

    @Override
    public void load(Properties props, InputStream is) throws IOException {
        super.load(props, is);
        stripPTags(props);
    }

    @Override
    public void load(Properties props, Reader reader) throws IOException {
        super.load(props, reader);
        stripPTags(props);
    }

    public static String stripPTag(String value) {
        String res = value;
        if (value != null) {
            if (res.startsWith(P_START)) {
                res = res.substring(3);
            }
            if (res.endsWith(P_END)) {
                res = res.substring(0, res.length() - 4);
            }
        }
        return res;
    }

    private static void stripPTags(Properties props) {
        for (String name : props.stringPropertyNames()) {
            String property = props.getProperty(name);
            if (property.startsWith(P_START) || property.endsWith(P_END)) {
                props.setProperty(name, stripPTag(property));
            }
        }
    }
}
