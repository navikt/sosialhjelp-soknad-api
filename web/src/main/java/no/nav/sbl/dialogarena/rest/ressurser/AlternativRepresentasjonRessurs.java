package no.nav.sbl.dialogarena.rest.ressurser;

import no.nav.sbl.dialogarena.sendsoknad.domain.AlternativRepresentasjon;
import no.nav.sbl.dialogarena.sendsoknad.domain.WebSoknad;
import no.nav.sbl.dialogarena.sendsoknad.domain.kravdialoginformasjon.KravdialogInformasjonHolder;
import no.nav.sbl.dialogarena.sendsoknad.domain.message.NavMessageSource;
import no.nav.sbl.dialogarena.sendsoknad.domain.transformer.AlternativRepresentasjonTransformer;
import no.nav.sbl.dialogarena.sendsoknad.domain.transformer.AlternativRepresentasjonType;
import no.nav.sbl.dialogarena.sikkerhet.SjekkTilgangTilSoknad;
import no.nav.sbl.dialogarena.soknadinnsending.business.WebSoknadConfig;
import no.nav.sbl.dialogarena.soknadinnsending.business.service.soknadservice.AlternativRepresentasjonService;
import no.nav.sbl.dialogarena.soknadinnsending.business.service.soknadservice.SoknadDataFletter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;

import javax.inject.Inject;
import javax.ws.rs.*;
import java.io.IOException;
import java.util.List;
import java.util.Optional;

import static javax.ws.rs.core.MediaType.TEXT_XML;

@Controller
@Path("/representasjon")
public class AlternativRepresentasjonRessurs {

    @Inject
    private AlternativRepresentasjonService alternativRepresentasjonService;
    @Inject
    private NavMessageSource messageSource;
    @Inject
    private SoknadDataFletter soknadDataFletter;
    private static final Logger LOG = LoggerFactory.getLogger(AlternativRepresentasjonRessurs.class);

    private static final boolean RESSURS_AKTIVERT = Boolean.valueOf(System.getProperty("soknad.alternativrepresentasjon.ressurs.enabled", "false"));


    @Deprecated
    @GET
    @Path("/xml/{behandlingsId}")
    @Produces(TEXT_XML)
    @SjekkTilgangTilSoknad
    public byte[] xmlRepresentasjon(@PathParam("behandlingsId") String behandlingsId) throws IOException {
        erRessursAktiv("xmlRepresentasjon");
        WebSoknad soknad = soknadDataFletter.hentSoknad(behandlingsId, true, true, false);
        List<AlternativRepresentasjon> representasjoner = alternativRepresentasjonService.hentAlternativeRepresentasjoner(soknad, messageSource);

        Optional<AlternativRepresentasjon> optionalRepresentasjoner = representasjoner.stream().filter(r -> r.getRepresentasjonsType().equals(AlternativRepresentasjonType.XML)).findFirst();

        return optionalRepresentasjoner.get().getContent();
    }

    private void erRessursAktiv(String metode) {
        LOG.warn("OppsummeringRessurs metode {} forsøkt aksessert", metode);
        if (!RESSURS_AKTIVERT) {
            throw new NotFoundException("Denne informasjonen er ikke tilgjengelig");
        }
    }


}
