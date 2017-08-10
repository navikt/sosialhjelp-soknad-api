package no.nav.sbl.dialogarena.sendsoknad.domain.transformer.sosialhjelp;

import no.nav.sbl.dialogarena.sendsoknad.domain.Vedlegg.Status;
import no.nav.sbl.dialogarena.sendsoknad.domain.WebSoknad;
import no.nav.sbl.dialogarena.sendsoknad.domain.exception.AlleredeHandtertException;
import no.nav.sbl.dialogarena.sendsoknad.domain.Vedlegg;
import org.slf4j.Logger;
import no.nav.melding.domene.brukerdialog.soeknadsskjemasosialhjelp.v1.*;

import java.util.List;
import java.util.function.Function;

import static java.util.stream.Collectors.toList;
import static org.slf4j.LoggerFactory.getLogger;

public class VedleggTilXml implements Function<WebSoknad, XMLVedleggListe> {
    private static final Logger log = getLogger(VedleggTilXml.class);

    @Override
    public XMLVedleggListe apply(WebSoknad webSoknad) {
        List<XMLVedlegg> vedlegg = webSoknad.getVedlegg().stream().map(TO_VEDLEGG).collect(toList());
        return new XMLVedleggListe(vedlegg);
    }

    private Function<Vedlegg, XMLVedlegg> TO_VEDLEGG = soknadVedlegg -> new XMLVedlegg()
            .withSkjemanummer(soknadVedlegg.getSkjemaNummer())
            .withTittel(soknadVedlegg.getTittel())
            .withNavn(soknadVedlegg.getNavn())
            .withInnsendingsvalg(innsendingsvalg(soknadVedlegg.getInnsendingsvalg()))
            .withTilleggsinfo(aarsak(soknadVedlegg));

    private String aarsak(Vedlegg vedlegg) {
        if (vedlegg.getInnsendingsvalg() == Status.VedleggSendesIkke || vedlegg.getInnsendingsvalg() == Status.VedleggAlleredeSendt) {
            return vedlegg.getAarsak();
        }
        return null;
    }

    private XMLInnsendingsvalg innsendingsvalg(Status innsendingsvalg) {
        switch (innsendingsvalg) {
            case LastetOpp:
                return XMLInnsendingsvalg.LASTET_OPP;
            case SendesSenere:
                return XMLInnsendingsvalg.SEND_SENERE;
            case VedleggSendesAvAndre:
                return XMLInnsendingsvalg.VEDLEGG_SENDES_AV_ANDRE;
            case VedleggAlleredeSendt:
                return XMLInnsendingsvalg.VEDLEGG_ALLEREDE_SENDT;
            case VedleggSendesIkke:
                return XMLInnsendingsvalg.SENDES_IKKE;
            // VedleggKreves er egentlig ikke gyldig for transformasjon,
            // men kan trigges via xml-endepunkt før søknaden sendes inn
            case VedleggKreves:
                return XMLInnsendingsvalg.IKKE_VALGT;
        }
        log.error("Feil under transformering av vedlegg til XML, {} er ikke et gydlig innsendingsvalg", innsendingsvalg);
        throw new AlleredeHandtertException();
    }

}
