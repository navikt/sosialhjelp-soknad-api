package no.nav.sbl.dialogarena.soknadinnsending.consumer.aktor;


import no.nav.modig.core.exception.ApplicationException;
import no.nav.tjeneste.virksomhet.aktoer.v1.AktoerPortType;
import no.nav.tjeneste.virksomhet.aktoer.v1.HentAktoerIdForIdentPersonIkkeFunnet;
import no.nav.tjeneste.virksomhet.aktoer.v1.meldinger.HentAktoerIdForIdentRequest;
import org.springframework.cache.annotation.Cacheable;

import javax.inject.Inject;
import javax.inject.Named;
import javax.xml.ws.soap.SOAPFaultException;


@Named("AktorIdWSService")
public class AktorIdService {

    @Inject
    private AktoerPortType aktorPortType;

    @Cacheable("aktorCache")
    public String hentAktorIdForFno(String ident) {
        try {
            HentAktoerIdForIdentRequest request = new HentAktoerIdForIdentRequest();
            request.setIdent(ident);
            return aktorPortType.hentAktoerIdForIdent(request).getAktoerId();
        } catch (SOAPFaultException | HentAktoerIdForIdentPersonIkkeFunnet ex) {
            throw new ApplicationException("Kunne ikke kontakte AktorIdService", ex);
        }
    }

    public void ping() {
        try {
            aktorPortType.ping();
        } catch (SOAPFaultException ex) {
            throw new ApplicationException("Kunne ikke kontakte AktorIdService", ex);
        }
    }
}
