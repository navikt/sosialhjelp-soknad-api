package no.nav.sbl.dialogarena.soknadinnsending.consumer.dkif;

import no.nav.sbl.dialogarena.sendsoknad.domain.DigitalKontaktinfo;
import no.nav.tjeneste.virksomhet.digitalkontaktinformasjon.v1.DigitalKontaktinformasjonV1;
import no.nav.tjeneste.virksomhet.digitalkontaktinformasjon.v1.HentDigitalKontaktinformasjonKontaktinformasjonIkkeFunnet;
import no.nav.tjeneste.virksomhet.digitalkontaktinformasjon.v1.HentDigitalKontaktinformasjonPersonIkkeFunnet;
import no.nav.tjeneste.virksomhet.digitalkontaktinformasjon.v1.HentDigitalKontaktinformasjonSikkerhetsbegrensing;
import no.nav.tjeneste.virksomhet.digitalkontaktinformasjon.v1.informasjon.WSKontaktinformasjon;
import no.nav.tjeneste.virksomhet.digitalkontaktinformasjon.v1.meldinger.WSHentDigitalKontaktinformasjonRequest;
import no.nav.tjeneste.virksomhet.digitalkontaktinformasjon.v1.meldinger.WSHentDigitalKontaktinformasjonResponse;
import org.slf4j.Logger;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import javax.inject.Inject;
import javax.inject.Named;

import static org.slf4j.LoggerFactory.getLogger;

@Service
public class EpostService {

    private static final Logger logger = getLogger(EpostService.class);

    @Inject
    @Named("dkifService")
    private DigitalKontaktinformasjonV1 dkif;

    @Cacheable("dkifCache")
    public DigitalKontaktinfo hentInfoFraDKIF(String ident) {
        try {
            return mapResponsTilKontaktInfo(dkif.hentDigitalKontaktinformasjon(makeDKIFRequest(ident)));
        } catch (HentDigitalKontaktinformasjonSikkerhetsbegrensing | HentDigitalKontaktinformasjonPersonIkkeFunnet e) {
            logger.error("Person ikke tilgjengelig i dkif: {}", e.getMessage());
        } catch (HentDigitalKontaktinformasjonKontaktinformasjonIkkeFunnet e) {
            logger.info("Kunne ikke hente kontaktinformasjon fra dkif: {}", e.getMessage());
        } catch (Exception e) {
            logger.info("Feil ved henting fra dkif: {}", e.getMessage());
        }

        return new DigitalKontaktinfo().withEpostadresse("").withMobilnummer("");
    }

    DigitalKontaktinfo mapResponsTilKontaktInfo(WSHentDigitalKontaktinformasjonResponse response) {
        if (response == null || response.getDigitalKontaktinformasjon() == null) {
            return new DigitalKontaktinfo();
        }
        WSKontaktinformasjon digitalKontaktinformasjon = response.getDigitalKontaktinformasjon();
        return new DigitalKontaktinfo()
                .withEpostadresse(digitalKontaktinformasjon.getEpostadresse() != null ?
                        digitalKontaktinformasjon.getEpostadresse().getValue() : "")
                .withMobilnummer(digitalKontaktinformasjon.getMobiltelefonnummer() != null ?
                        digitalKontaktinformasjon.getMobiltelefonnummer().getValue() : "");
    }

    private WSHentDigitalKontaktinformasjonRequest makeDKIFRequest(String ident) {
        return new WSHentDigitalKontaktinformasjonRequest().withPersonident(ident);
    }
}
