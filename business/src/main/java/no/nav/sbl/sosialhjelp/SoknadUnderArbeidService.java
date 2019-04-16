package no.nav.sbl.sosialhjelp;

import no.nav.sbl.soknadsosialhjelp.soknad.internal.JsonSoknadsmottaker;
import no.nav.sbl.sosialhjelp.domain.SoknadUnderArbeid;
import no.nav.sbl.sosialhjelp.soknadunderbehandling.SoknadUnderArbeidRepository;
import org.springframework.stereotype.Component;

import javax.inject.Inject;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.Optional;

import static org.apache.commons.lang3.StringUtils.isEmpty;


@Component
public class SoknadUnderArbeidService {

    @Inject
    private SoknadUnderArbeidRepository soknadUnderArbeidRepository;

    public void settOrgnummerOgNavEnhetsnavnPaSoknad(SoknadUnderArbeid soknadUnderArbeid, String orgnummer, String navEnhetsnavn, String eier) {
        if (soknadUnderArbeid == null) {
            throw new RuntimeException("Søknad under arbeid mangler");
        }

        if (isEmpty(orgnummer) || isEmpty(navEnhetsnavn)) {
            throw new RuntimeException("Informasjon om orgnummer og NAV-enhet mangler");
        } else {
            SoknadUnderArbeid oppdatertSoknadUnderArbeid = oppdaterOrgnummerOgNavEnhetsnavnPaInternalSoknad(soknadUnderArbeid, orgnummer, navEnhetsnavn);
            soknadUnderArbeidRepository.oppdaterSoknadsdata(oppdatertSoknadUnderArbeid, eier);
        }
    }
    
    public void settInnsendingstidspunktPaSoknad(SoknadUnderArbeid soknadUnderArbeid) {
        if (soknadUnderArbeid == null) {
            throw new RuntimeException("Søknad under arbeid mangler");
        }
        if (soknadUnderArbeid.erEttersendelse()){
            return;
        }
        soknadUnderArbeid.getJsonInternalSoknad().getSoknad().setInnsendingstidspunkt(nowWithMilliseconds());
        soknadUnderArbeidRepository.oppdaterSoknadsdata(soknadUnderArbeid, soknadUnderArbeid.getEier());
    }

    private String nowWithMilliseconds() {
        OffsetDateTime now = OffsetDateTime.now(ZoneOffset.UTC);
        if (now.getNano() == 0) {
            return now.plusNanos(1_000_000).toString();
        }
        return now.toString();
    }

    public SoknadUnderArbeid oppdaterEllerOpprettSoknadUnderArbeid(SoknadUnderArbeid soknadUnderArbeid, String eier) {
        if (soknadUnderArbeid == null) {
            throw new RuntimeException("Søknad under arbeid mangler");
        } else if (isEmpty(soknadUnderArbeid.getBehandlingsId())) {
            throw new RuntimeException("Søknad under arbeid mangler behandlingsId");
        }
        Optional<SoknadUnderArbeid> soknadUnderArbeidOptional = soknadUnderArbeidRepository.hentSoknad(soknadUnderArbeid.getBehandlingsId(), eier);
        if (soknadUnderArbeidOptional.isPresent()) {
            SoknadUnderArbeid soknadUnderArbeidFraDB = soknadUnderArbeidOptional.get();
            soknadUnderArbeidFraDB.withJsonInternalSoknad(soknadUnderArbeid.getJsonInternalSoknad());
            soknadUnderArbeidRepository.oppdaterSoknadsdata(soknadUnderArbeidFraDB, eier);
        } else {
            soknadUnderArbeidRepository.opprettSoknad(soknadUnderArbeid, eier);
        }
        Optional<SoknadUnderArbeid> oppdatertSoknadUnderArbeidOptional = soknadUnderArbeidRepository.hentSoknad(soknadUnderArbeid.getBehandlingsId(), eier);
        if (!oppdatertSoknadUnderArbeidOptional.isPresent()) {
            throw new RuntimeException("Kunne ikke hente oppdatert søknad under arbeid fra database");
        }
        return oppdatertSoknadUnderArbeidOptional.get();
    }

    SoknadUnderArbeid oppdaterOrgnummerOgNavEnhetsnavnPaInternalSoknad(SoknadUnderArbeid soknadUnderArbeid, String orgnummer, String navEnhetsnavn) {
        soknadUnderArbeid.getJsonInternalSoknad().setMottaker(new JsonSoknadsmottaker()
                .withOrganisasjonsnummer(orgnummer)
                .withNavEnhetsnavn(navEnhetsnavn));
        return soknadUnderArbeid;
    }

}
