package no.nav.sosialhjelp.soknad.consumer.organisasjon;

import no.nav.sosialhjelp.soknad.client.organisasjon.OrganisasjonClient;
import no.nav.sosialhjelp.soknad.client.organisasjon.dto.NavnDto;
import no.nav.sosialhjelp.soknad.client.organisasjon.dto.OrganisasjonNoekkelinfoDto;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

import static java.util.Arrays.asList;

@Component
public class OrganisasjonService {

    private static final Logger log = LoggerFactory.getLogger(OrganisasjonService.class);

    private final OrganisasjonClient organisasjonClient;

    public OrganisasjonService(OrganisasjonClient organisasjonClient) {
        this.organisasjonClient = organisasjonClient;
    }

    public String hentOrgNavn(String orgnr) {
        if (orgnr != null) {
            try {
                OrganisasjonNoekkelinfoDto noekkelinfo = organisasjonClient.hentOrganisasjonNoekkelinfo(orgnr);
                if (noekkelinfo == null) {
                    log.warn("Kunne ikke hente orgnr fra Ereg: {}", orgnr);
                    return orgnr;
                }
                NavnDto navn = noekkelinfo.getNavn();
                List<String> list = new ArrayList<>(asList(navn.getNavnelinje1(), navn.getNavnelinje2(), navn.getNavnelinje3(), navn.getNavnelinje4(), navn.getNavnelinje5()));
                list.removeAll(asList("", null)); // fjern tomme strenger og null (håndteres som "null")
                return String.join(", ", list);
            } catch (Exception e) {
                log.warn("Kunne ikke hente orgnr fra Ereg: {}", orgnr, e);
                return orgnr;
            }
        } else {
            return "";
        }
    }
}
