package no.nav.sbl.dialogarena.soknad.convert.xml;

import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import java.io.Serializable;
import java.util.AbstractMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class XmlUtil implements Serializable {

    public static Map<String, String> toMap(final NodeList list) {
        return new AbstractMap<String, String>() {

            @Override
            public Set<Entry<String, String>> entrySet() {
                Set<Entry<String, String>> set = new HashSet<>();

                for (int i = 0; i < list.getLength(); i++) {
                    Element element = (Element) list.item(i);
                    XmlInputElement xmlInputElement = new XmlInputElement(element);
                    Entry<String, String> entry = new SimpleEntry<String, String>(xmlInputElement.getKey(), xmlInputElement.getValue());
                    set.add(entry);
                }

                return set;
            }
        };
    }
}
