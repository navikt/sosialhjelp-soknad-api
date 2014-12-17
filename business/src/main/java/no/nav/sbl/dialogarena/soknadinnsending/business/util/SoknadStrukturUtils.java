package no.nav.sbl.dialogarena.soknadinnsending.business.util;

import no.nav.modig.core.exception.ApplicationException;
import no.nav.sbl.dialogarena.soknadinnsending.business.domain.oppsett.SoknadStruktur;

import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import java.util.HashMap;
import java.util.Map;

import static java.lang.String.format;
import static javax.xml.bind.JAXBContext.newInstance;

public class SoknadStrukturUtils {
    public static SoknadStruktur hentStruktur(String skjema) {
        //TODO: Få flyttet dette ut på et vis? Ta i bruk.
        Map<String, String> strukturDokumenter = new HashMap<>();
        strukturDokumenter.put("NAV 04-01.04", "NAV 04-01.03.xml");
        strukturDokumenter.put("NAV 04-01.03", "NAV 04-01.03.xml");
        strukturDokumenter.put("NAV 04-16.03", "NAV 04-16.03.xml");

        String type = strukturDokumenter.get(skjema);

        if (type == null || type.isEmpty()) {
            throw new ApplicationException("Fant ikke strukturdokument for nav-skjemanummer: " + skjema);
        }

        try {
            Unmarshaller unmarshaller = newInstance(SoknadStruktur.class)
                    .createUnmarshaller();
            return (SoknadStruktur) unmarshaller.unmarshal(SoknadStruktur.class
                    .getResourceAsStream(format("/soknader/%s", type)));
        } catch (JAXBException e) {
            throw new RuntimeException("Kunne ikke laste definisjoner. ", e);
        }
    }
}
