package no.nav.sbl.dialogarena.soknad.pages.felles.xml;

import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import java.util.AbstractList;
import java.util.List;

public class XmlUtil {

    public static List<XmlElement> list(final NodeList list) {
        return new AbstractList<XmlElement>() {
            @Override
            public XmlElement get(int index) {
                Element element = (Element) list.item(index);
                if (element == null) {
                    throw new IndexOutOfBoundsException();
                }
                return new XmlElement(element);
            }

            @Override
            public int size() {
                return list.getLength();
            }
        };
    }
}
