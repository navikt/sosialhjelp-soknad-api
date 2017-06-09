package no.nav.sbl.dialogarena.sendsoknad.domain.transformer.foreldrepenger.engangsstonad;

import no.nav.melding.virksomhet.soeknadsskjemaengangsstoenad.v1.Innsendingsvalg;
import no.nav.melding.virksomhet.soeknadsskjemaengangsstoenad.v1.Vedlegg;
import no.nav.sbl.dialogarena.sendsoknad.domain.Vedlegg.Status;
import no.nav.sbl.dialogarena.sendsoknad.domain.WebSoknad;
import no.nav.sbl.dialogarena.sendsoknad.domain.exception.AlleredeHandtertException;
import org.slf4j.Logger;

import java.util.List;
import java.util.function.Function;

import static java.util.stream.Collectors.toList;
import static org.slf4j.LoggerFactory.getLogger;

public class VedleggTilXml implements Function<WebSoknad, List<Vedlegg>> {
    private static final Logger log = getLogger(VedleggTilXml.class);

    @Override
    public List<Vedlegg> apply(WebSoknad webSoknad) {
        return webSoknad.getVedlegg().stream().map(TO_VEDLEGG).collect(toList());
    }

    private Function<no.nav.sbl.dialogarena.sendsoknad.domain.Vedlegg, Vedlegg> TO_VEDLEGG = soknadVedlegg -> new Vedlegg()
            .withSkjemanummer(dokumentTypeId(soknadVedlegg.getSkjemaNummer()))
            .withErPaakrevdISoeknadsdialog(erPaakrevd(soknadVedlegg.getSkjemaNummer()))
            .withInnsendingsvalg(innsendingsvalg(soknadVedlegg.getInnsendingsvalg()))
            .withTilleggsinfo(aarsak(soknadVedlegg));

    private String aarsak(no.nav.sbl.dialogarena.sendsoknad.domain.Vedlegg vedlegg) {
        if (vedlegg.getInnsendingsvalg() == Status.VedleggSendesIkke || vedlegg.getInnsendingsvalg() == Status.VedleggAlleredeSendt) {
            return vedlegg.getAarsak();
        }
        return null;
    }

    private String dokumentTypeId(String skjemanummer) {
        return Skjemanummer.valueOf(skjemanummer).dokumentTypeId();
    }

    private boolean erPaakrevd(String skjemanummer) {
        return Skjemanummer.valueOf(skjemanummer).erPaakrevd();
    }

    private Innsendingsvalg innsendingsvalg(Status innsendingsvalg) {
        switch (innsendingsvalg) {
            case LastetOpp:
                return Innsendingsvalg.LASTET_OPP;
            case SendesSenere:
                return Innsendingsvalg.SEND_SENERE;
            case VedleggSendesAvAndre:
                return Innsendingsvalg.VEDLEGG_SENDES_AV_ANDRE;
            case VedleggAlleredeSendt:
                return Innsendingsvalg.VEDLEGG_ALLEREDE_SENDT;
            case VedleggSendesIkke:
                return Innsendingsvalg.SENDES_IKKE;
            // VedleggKreves er egentlig ikke gyldig for transformasjon,
            // men kan trigges via xml-endepunkt før søknaden sendes inn
            case VedleggKreves:
                return Innsendingsvalg.IKKE_VALGT;
        }
        log.error("Feil under transformering av vedlegg til XML, {} er ikke et gydlig innsendingsvalg", innsendingsvalg);
        throw new AlleredeHandtertException();
    }

}
