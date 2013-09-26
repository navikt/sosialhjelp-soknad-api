package no.nav.sbl.dialogarena.person.consumer.transform;

import no.nav.sbl.dialogarena.telefonnummer.Telefonnummertype;
import no.nav.tjeneste.virksomhet.behandlebrukerprofil.v1.informasjon.XMLTelefontyper;
import org.apache.commons.collections15.Transformer;

import java.util.EnumMap;
import java.util.Map;
import java.util.NoSuchElementException;

import static org.apache.commons.lang3.StringUtils.join;

class TelefonnummertypeToXMLTelefontype implements Transformer<Telefonnummertype, XMLTelefontyper> {

    public static final Map<Telefonnummertype, XMLTelefontyper> XML_TYPER = new EnumMap<>(Telefonnummertype.class);

    static {
        XML_TYPER.put(Telefonnummertype.MOBIL, new XMLTelefontyper().withValue("MOBI"));
        XML_TYPER.put(Telefonnummertype.HJEMMETELEFON, new XMLTelefontyper().withValue("HJET"));
        XML_TYPER.put(Telefonnummertype.JOBBTELEFON, new XMLTelefontyper().withValue("ARBT"));
    }

    @Override
    public XMLTelefontyper transform(Telefonnummertype type) {
        if (XML_TYPER.containsKey(type)) {
            return XML_TYPER.get(type);
        } else {
            throw new NoSuchElementException("Vet ikke hvordan jeg skal oversette "
                + Telefonnummertype.class.getSimpleName() + "." + type.name() + " til XML-objekt. Disse håndteres: "
                + join(XML_TYPER.keySet(), ", "));
        }
    }

}
