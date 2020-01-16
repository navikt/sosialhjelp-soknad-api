package no.nav.sbl.dialogarena.soknadinnsending.consumer.organisasjon;

import com.google.common.base.Joiner;
import no.nav.sbl.dialogarena.soknadinnsending.consumer.organisasjon.dto.NavnDto;
import no.nav.sbl.dialogarena.soknadinnsending.consumer.organisasjon.dto.OrganisasjonNoekkelinfoDto;
import no.nav.tjeneste.virksomhet.organisasjon.v4.binding.OrganisasjonV4;
import no.nav.tjeneste.virksomhet.organisasjon.v4.informasjon.UstrukturertNavn;
import no.nav.tjeneste.virksomhet.organisasjon.v4.meldinger.HentOrganisasjonRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import javax.inject.Inject;
import javax.inject.Named;
import java.util.ArrayList;
import java.util.List;

import static java.util.Arrays.asList;

@Component
public class OrganisasjonService {

    private static final Logger log = LoggerFactory.getLogger(OrganisasjonService.class);
    private static final String EREG_API_ENABLED = "ereg_api_enabled";

    @Inject
    private OrganisasjonConsumer organisasjonConsumer;

    @Inject
    @Named("organisasjonEndpoint")
    private OrganisasjonV4 organisasjonWebService;

    public boolean brukEregRestApi() {
        return Boolean.parseBoolean(System.getProperty(EREG_API_ENABLED, "false"));
    }

    public String hentOrgNavn(String orgnr) {
        return brukEregRestApi() ? hentOrgNavnRest(orgnr) : hentOrgNavnWebservice(orgnr);
    }

    private String hentOrgNavnRest(String orgnr) {
        log.info("Bruker Ereg rest api");
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

    private String hentOrgNavnWebservice(String orgnr) {
        log.info("Bruker Ereg webservice");
        if (orgnr != null) {
            HentOrganisasjonRequest hentOrganisasjonRequest = lagOrgRequest(orgnr);
            try {
                //Kan bare være ustrukturert navn.
                no.nav.tjeneste.virksomhet.organisasjon.v4.informasjon.Organisasjon organisasjon = organisasjonWebService.hentOrganisasjon(hentOrganisasjonRequest).getOrganisasjon();
                if (organisasjon == null) {
                    log.warn("Kunne ikke hente orgnr: " + orgnr);
                    return orgnr;
                }
                List<String> orgNavn = ((UstrukturertNavn) organisasjon.getNavn()).getNavnelinje();
                orgNavn.removeAll(asList("", null));
                return Joiner.on(", ").join(orgNavn);
            } catch (Exception ex) {
                log.warn("Kunne ikke hente orgnr: " + orgnr, ex);
                return orgnr;
            }
        } else {
            return "";
        }
    }

    private HentOrganisasjonRequest lagOrgRequest(String orgnr) {
        HentOrganisasjonRequest hentOrganisasjonRequest = new HentOrganisasjonRequest();
        hentOrganisasjonRequest.setOrgnummer(orgnr);
        hentOrganisasjonRequest.setInkluderHierarki(false);
        hentOrganisasjonRequest.setInkluderHistorikk(false);
        return hentOrganisasjonRequest;
    }
}
