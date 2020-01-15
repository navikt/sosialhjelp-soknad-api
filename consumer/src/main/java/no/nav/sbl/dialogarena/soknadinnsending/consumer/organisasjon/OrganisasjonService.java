package no.nav.sbl.dialogarena.soknadinnsending.consumer.organisasjon;

import no.nav.sbl.dialogarena.soknadinnsending.consumer.organisasjon.dto.NavnDto;
import no.nav.sbl.dialogarena.soknadinnsending.consumer.organisasjon.dto.OrganisasjonNoekkelinfoDto;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import java.util.ArrayList;
import java.util.List;

import static java.util.Arrays.asList;

public class OrganisasjonService {

    private static final Logger log = LoggerFactory.getLogger(OrganisasjonService.class);

    @Inject
    private OrganisasjonConsumer organisasjonConsumer;

    public String hentOrgNavn(String orgnr) {
        if (orgnr != null) {
            try {
                OrganisasjonNoekkelinfoDto noekkelinfo = organisasjonConsumer.hentOrganisasjonNoekkelinfo(orgnr);
                if (noekkelinfo == null) {
                    log.warn("Kunne ikke hente orgnr fra Ereg: " + orgnr);
                    return orgnr;
                }
                NavnDto navn = noekkelinfo.getNavn();
                List<String> list = new ArrayList<>(asList(navn.getNavnelinje1(), navn.getNavnelinje2(), navn.getNavnelinje3(), navn.getNavnelinje4(), navn.getNavnelinje5()));
                list.removeAll(asList("", null)); // fjern tomme strenger og null (håndteres som "null")
                return String.join(", ", list);
            } catch (Exception e) {
                log.warn("Kunne ikke hente orgnr fra Ereg: " + orgnr, e);
                return orgnr;
            }
        } else {
            return "";
        }
    }
}
