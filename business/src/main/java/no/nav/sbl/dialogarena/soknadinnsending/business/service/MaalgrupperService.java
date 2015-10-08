package no.nav.sbl.dialogarena.soknadinnsending.business.service;

import com.google.common.base.Function;
import com.google.common.collect.Lists;
import no.nav.sbl.dialogarena.soknadinnsending.business.domain.Faktum;
import no.nav.tjeneste.virksomhet.maalgruppe.v1.FinnMaalgruppeinformasjonListePersonIkkeFunnet;
import no.nav.tjeneste.virksomhet.maalgruppe.v1.FinnMaalgruppeinformasjonListeSikkerhetsbegrensning;
import no.nav.tjeneste.virksomhet.maalgruppe.v1.MaalgruppeV1;
import no.nav.tjeneste.virksomhet.maalgruppe.v1.informasjon.WSMaalgruppe;
import no.nav.tjeneste.virksomhet.maalgruppe.v1.informasjon.WSPeriode;
import no.nav.tjeneste.virksomhet.maalgruppe.v1.meldinger.WSFinnMaalgruppeinformasjonListeRequest;
import no.nav.tjeneste.virksomhet.maalgruppe.v1.meldinger.WSFinnMaalgruppeinformasjonListeResponse;
import org.joda.time.LocalDate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import javax.inject.Inject;
import javax.inject.Named;
import javax.xml.ws.soap.SOAPFaultException;
import java.util.Collections;
import java.util.List;

@Service
public class MaalgrupperService {
    private static final Logger LOG = LoggerFactory.getLogger(MaalgrupperService.class);

    @Inject
    @Named("maalgruppeEndpoint")
    private MaalgruppeV1 maalgruppeinformasjon;
    private MaalgruppeTilFaktum maalgruppeTilFaktum = new MaalgruppeTilFaktum();

    public List<Faktum> hentMaalgrupper(String fodselsnummer) {
        try {
            WSFinnMaalgruppeinformasjonListeResponse maalgrupper = maalgruppeinformasjon.finnMaalgruppeinformasjonListe(lagRequest(fodselsnummer));
            return Lists.transform(maalgrupper.getMaalgruppeListe(), maalgruppeTilFaktum);
        } catch (SOAPFaultException ex) {
            if (ex.getCause() instanceof FinnMaalgruppeinformasjonListePersonIkkeFunnet) {
                return Collections.emptyList();
            }
        } catch (FinnMaalgruppeinformasjonListePersonIkkeFunnet | FinnMaalgruppeinformasjonListeSikkerhetsbegrensning e) {
            throw new RuntimeException(e);
        } catch (Exception ex) {
            //Maalgruppetjenesten er nede etter kl 8 på kvelden. Om den er nede skal vi bare returnere en tom liste.
            LOG.debug("Maalgruppetjeneste nede: ", ex);
        }
        return Collections.emptyList();
    }

    private WSFinnMaalgruppeinformasjonListeRequest lagRequest(String fodselsnummer) {
        return new WSFinnMaalgruppeinformasjonListeRequest()
                .withPersonident(fodselsnummer)
                .withPeriode(new WSPeriode()
                                .withFom(LocalDate.now().minusMonths(6))
                                .withTom(LocalDate.now().plusMonths(2))
                );
    }

    private static class MaalgruppeTilFaktum implements Function<WSMaalgruppe, Faktum> {
        public static final String DATOFORMAT = "yyyy-MM-dd";

        @Override
        public Faktum apply(WSMaalgruppe maalgruppe) {
            return new Faktum()
                    .medKey("maalgruppe")
                    .medProperty("navn", maalgruppe.getMaalgruppenavn())
                    .medProperty("fom", datoTilString(maalgruppe.getGyldighetsperiode().getFom()))
                    .medProperty("tom", datoTilString(maalgruppe.getGyldighetsperiode().getTom()))
                    .medProperty("kodeverkVerdi", maalgruppe.getMaalgruppetype().getValue());
        }

        private String datoTilString(LocalDate date) {
            return date != null ? date.toString(DATOFORMAT) : "";
        }
    }
}
