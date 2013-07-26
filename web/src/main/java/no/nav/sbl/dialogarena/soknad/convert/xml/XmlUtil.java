package no.nav.sbl.dialogarena.soknad.convert.xml;

import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import java.util.AbstractList;
import java.util.List;

public class XmlUtil {

    public static List<XmlInputElement> toList(final NodeList list) {
        return new AbstractList<XmlInputElement>() {
            @Override
            public XmlInputElement get(int index) {
                Element element = (Element) list.item(index);
                if (element == null) {
                    throw new IndexOutOfBoundsException();
                }
                return new XmlInputElement(element);
            }

            @Override
            public int size() {
                return list.getLength();
            }
        };
    }
}
