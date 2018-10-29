package no.nav.sbl.sosialhjelp;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import no.nav.sbl.soknadsosialhjelp.soknad.JsonInternalSoknad;
import no.nav.sbl.soknadsosialhjelp.soknad.internal.JsonSoknadsmottaker;
import no.nav.sbl.sosialhjelp.domain.SoknadUnderArbeid;
import no.nav.sbl.sosialhjelp.soknadunderbehandling.SamtidigSoknadUnderArbeidOppdateringException;
import no.nav.sbl.sosialhjelp.soknadunderbehandling.SoknadUnderArbeidRepository;
import org.slf4j.Logger;
import org.springframework.stereotype.Component;

import javax.inject.Inject;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

import static java.lang.Thread.sleep;
import static no.nav.sbl.soknadsosialhjelp.json.JsonSosialhjelpValidator.ensureValidInternalSoknad;
import static org.apache.commons.lang3.StringUtils.isEmpty;
import static org.slf4j.LoggerFactory.getLogger;

@Component
public class SoknadUnderArbeidService {
    private static final Logger logger = getLogger(SoknadUnderArbeidService.class);
    private final ObjectMapper mapper = new ObjectMapper();
    private final ObjectWriter writer = mapper.writerWithDefaultPrettyPrinter();
    private int ventetidIms = 500;

    @Inject
    private SoknadUnderArbeidRepository soknadUnderArbeidRepository;

    public void settOrgnummerOgNavEnhetsnavnPaNySoknad(SoknadUnderArbeid soknadUnderArbeid, String orgnummer, String navEnhetsnavn, String eier) {
        if (soknadUnderArbeid == null) {
            throw new RuntimeException("Søknad under arbeid mangler");
        }

        if (soknadUnderArbeid.erEttersendelse()) {
            throw new IllegalStateException("Har forsøkt å oppdatere mottakerinfo for ettersending, når det skulle ha blitt hentet fra søknaden det ettersendes på.");
        } else if (isEmpty(orgnummer) || isEmpty(navEnhetsnavn)) {
            throw new RuntimeException("Informasjon om orgnummer og NAV-enhet mangler");
        } else {
            SoknadUnderArbeid oppdatertSoknadUnderArbeid = oppdaterOrgnummerOgNavEnhetsnavnPaInternalSoknad(soknadUnderArbeid, orgnummer, navEnhetsnavn);
            oppdaterSoknadsdataIDatabasen(oppdatertSoknadUnderArbeid, eier);
        }
    }

    void oppdaterSoknadsdataIDatabasen(SoknadUnderArbeid oppdatertSoknadUnderArbeid, String eier) {
        final int maksAntallForsok = 3;
        int antallOppdateringsforsok = 0;
        while (antallOppdateringsforsok < maksAntallForsok) {
            try {
                soknadUnderArbeidRepository.oppdaterSoknadsdata(oppdatertSoknadUnderArbeid, eier);
                break;
            } catch (SamtidigSoknadUnderArbeidOppdateringException e) {
                antallOppdateringsforsok++;
                vent();
                if (antallOppdateringsforsok == maksAntallForsok) {
                    throw new RuntimeException("Kunne ikke oppdatere søknadsdata for søknad med behandlingsId "
                            + oppdatertSoknadUnderArbeid.getBehandlingsId());
                }
                logger.warn(e.getMessage());
            }
        }
    }

    JsonInternalSoknad hentJsonInternalSoknadFraSoknadUnderArbeid(SoknadUnderArbeid soknadUnderArbeid) {
        try {
            return mapper.readValue(soknadUnderArbeid.getData(), JsonInternalSoknad.class);
        } catch (IOException e) {
            logger.error("Kunne ikke finne søknad", e);
            throw new RuntimeException(e);
        }
    }

    SoknadUnderArbeid oppdaterOrgnummerOgNavEnhetsnavnPaInternalSoknad(SoknadUnderArbeid soknadUnderArbeid, String orgnummer, String navEnhetsnavn) {
        final JsonInternalSoknad jsonInternalSoknad = hentJsonInternalSoknadFraSoknadUnderArbeid(soknadUnderArbeid);
        jsonInternalSoknad.setMottaker(new JsonSoknadsmottaker()
                .withOrganisasjonsnummer(orgnummer)
                .withNavEnhetsnavn(navEnhetsnavn));
        final byte[] oppdatertSoknad = mapJsonSoknadInternalTilFil(jsonInternalSoknad);
        return soknadUnderArbeid.withData(oppdatertSoknad);
    }

    byte[] mapJsonSoknadInternalTilFil(JsonInternalSoknad jsonInternalSoknad) {
        try {
            final String internalSoknad = writer.writeValueAsString(jsonInternalSoknad);
            ensureValidInternalSoknad(internalSoknad);
            return internalSoknad.getBytes(StandardCharsets.UTF_8);
        } catch (JsonProcessingException e) {
            logger.error("Kunne ikke konvertere søknadsobjekt til tekststreng", e);
            throw new RuntimeException(e);
        }
    }

    private void vent() {
        try {
            sleep(ventetidIms);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    void setVentetidIms(int ventetidIms) {
        this.ventetidIms = ventetidIms;
    }
}
