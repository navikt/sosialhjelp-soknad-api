package no.nav.sbl.dialogarena.soknad.service;

import no.nav.modig.core.exception.ApplicationException;
import no.nav.sbl.dialogarena.soknad.domain.Faktum;
import no.nav.sbl.dialogarena.soknad.domain.Soknad;
import no.nav.tjeneste.domene.brukerdialog.sendsoknad.v1.SendSoknadPortType;
import no.nav.tjeneste.domene.brukerdialog.sendsoknad.v1.informasjon.WSBrukerData;
import no.nav.tjeneste.domene.brukerdialog.sendsoknad.v1.informasjon.WSSoknadData;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.xml.ws.soap.SOAPFaultException;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static no.nav.modig.lang.collections.IterUtils.on;
import static no.nav.sbl.dialogarena.soknad.service.Transformers.TIL_SOKNADID;
import static no.nav.sbl.dialogarena.soknad.service.Transformers.tilFaktum;

public class SoknadService {

    private static final Logger logger = LoggerFactory.getLogger(SoknadService.class);

    @Inject
    private SendSoknadPortType sendsoknadPortType;

    public Long startSoknad(String navSoknadId) {
        try {
            return sendsoknadPortType.startSoknad(navSoknadId);
        } catch (SOAPFaultException e) {
            logger.error("Feil ved oppretting av søknad med ID", navSoknadId, e);
            throw new ApplicationException("Kunne ikke opprette ny søknad", e);
        }
    }

    public Soknad hentSoknad(long soknadId) {
        try {
            WSSoknadData soknadData = sendsoknadPortType.hentSoknad(soknadId);
            return convertToSoknad(soknadData);
        } catch (SOAPFaultException e) {
            logger.error("Feil ved henting av søknadsstruktur for søknad med ID {}", soknadId, e);
            throw new ApplicationException("SoapFaultException", e);
        }
    }

    public void lagreSoknadsFelt(long soknadId, String key, String value) {
        try {
            sendsoknadPortType.lagreBrukerData(soknadId, key, value);
        } catch (SOAPFaultException e) {
            logger.error("Feil ved henting av søknadsstruktur for søknad med ID {}", soknadId, e);
            throw new ApplicationException("SoapFaultException", e);
        }
    }

    public void sendSoknad(long soknadId) {
        try {
            sendsoknadPortType.sendSoknad(soknadId);
        } catch (SOAPFaultException e) {
            logger.error("Feil ved sending av søknad med ID {}", soknadId, e);
            throw new ApplicationException("Feil ved sending av søknad", e);
        }
    }

    public List<Long> hentMineSoknader(String aktorId) {
        try {
            return on(sendsoknadPortType.hentSoknadListe(aktorId))
                    .map(TIL_SOKNADID)
                    .collect();
        } catch (SOAPFaultException e) {
            logger.error("Feil ved sending av søknader for aktør med ID {}", aktorId, e);
            throw new ApplicationException("Feil ved henting av søknader", e);
        }
    }

    private Soknad convertToSoknad(WSSoknadData wsSoknad) {
        Long soknadId = Long.parseLong(wsSoknad.getSoknadId());
        Map<String, Faktum> fakta = new LinkedHashMap<>();
        for (WSBrukerData wsBrukerData : wsSoknad.getFaktum()) {
            fakta.put(wsBrukerData.getNokkel(), tilFaktum(soknadId).transform(wsBrukerData));
        }

        Soknad soknad = new Soknad();
        soknad.soknadId = soknadId;
        soknad.gosysId = wsSoknad.getGosysId();
        soknad.leggTilFakta(fakta);

        return soknad;
    }
}
