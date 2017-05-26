package no.nav.sbl.dialogarena.rest.ressurser;

import no.nav.melding.domene.brukerdialog.behandlingsinformasjon.v1.XMLAlternativRepresentasjon;
import no.nav.sbl.dialogarena.sendsoknad.domain.AlternativRepresentasjon;
import no.nav.sbl.dialogarena.sendsoknad.domain.WebSoknad;
import no.nav.sbl.dialogarena.sendsoknad.domain.kravdialoginformasjon.KravdialogInformasjonHolder;
import no.nav.sbl.dialogarena.sendsoknad.domain.message.NavMessageSource;
import no.nav.sbl.dialogarena.sikkerhet.SjekkTilgangTilSoknad;
import no.nav.sbl.dialogarena.soknadinnsending.business.WebSoknadConfig;
import no.nav.sbl.dialogarena.soknadinnsending.business.service.soknadservice.SoknadDataFletter;
import org.apache.commons.collections15.Transformer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;

import javax.inject.Inject;
import javax.ws.rs.*;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import static javax.ws.rs.core.MediaType.TEXT_HTML;
import static javax.ws.rs.core.MediaType.TEXT_XML;

@Controller
@Path("/representasjon")
public class AlternativRepresentasjonRessurs {

    @Inject
    private KravdialogInformasjonHolder kravdialogInformasjonHolder;
    @Inject
    private WebSoknadConfig config;
    @Inject
    private NavMessageSource messageSource;
    @Inject
    private SoknadDataFletter soknadDataFletter;
    private static final Logger LOG = LoggerFactory.getLogger(AlternativRepresentasjonRessurs.class);

    private static final boolean RESSURS_AKTIVERT = Boolean.valueOf(System.getProperty("soknad.alternativrepresentasjon.ressurs.enabled", "false"));


    @Deprecated
    @GET
    @Path("/xml/{behandlingsId}/xml")
    @Produces(TEXT_XML)
    @SjekkTilgangTilSoknad
    public byte[] xmlRepresentasjon(@PathParam("behandlingsId") String behandlingsId) throws IOException {
        erRessursAktiv("xmlRepresentasjon");
        WebSoknad soknad = soknadDataFletter.hentSoknad(behandlingsId, true, true, false);
        List<Transformer<WebSoknad, AlternativRepresentasjon>> transformers = kravdialogInformasjonHolder.hentKonfigurasjon(soknad.getskjemaNummer()).getTransformers(messageSource);
        soknad.fjernFaktaSomIkkeSkalVaereSynligISoknaden(config.hentStruktur(soknad.getskjemaNummer()));
        List<AlternativRepresentasjon> alternativRepresentasjonListe = new ArrayList<>();
        for (Transformer<WebSoknad, AlternativRepresentasjon> transformer : transformers) {
            AlternativRepresentasjon altrep = transformer.transform(soknad);
            alternativRepresentasjonListe.add(altrep);

        }
        return alternativRepresentasjonListe.get(0).getContent();
    }

    private void erRessursAktiv(String metode) {
        LOG.warn("OppsummeringRessurs metode {} forsøkt aksessert", metode);
        if (!RESSURS_AKTIVERT) {
            throw new NotFoundException("Denne informasjonen er ikke tilgjengelig");
        }
    }


}
