package no.nav.sbl.dialogarena.sendsoknad.mockmodul.utbetaling;

import no.nav.tjeneste.virksomhet.utbetaling.v1.HentUtbetalingsinformasjonIkkeTilgang;
import no.nav.tjeneste.virksomhet.utbetaling.v1.HentUtbetalingsinformasjonPeriodeIkkeGyldig;
import no.nav.tjeneste.virksomhet.utbetaling.v1.HentUtbetalingsinformasjonPersonIkkeFunnet;
import no.nav.tjeneste.virksomhet.utbetaling.v1.UtbetalingV1;
import no.nav.tjeneste.virksomhet.utbetaling.v1.meldinger.WSHentUtbetalingsinformasjonRequest;
import no.nav.tjeneste.virksomhet.utbetaling.v1.meldinger.WSHentUtbetalingsinformasjonResponse;
import org.slf4j.Logger;

import java.util.Arrays;

import static org.slf4j.LoggerFactory.getLogger;

public class UtbetalMock implements UtbetalingV1 {

    private static final Logger logger = getLogger(UtbetalMock.class);


    @Override
    public void ping() {
        logger.info("Pinger mock");
    }

    @Override
    public WSHentUtbetalingsinformasjonResponse hentUtbetalingsinformasjon(WSHentUtbetalingsinformasjonRequest req) throws HentUtbetalingsinformasjonPeriodeIkkeGyldig, HentUtbetalingsinformasjonPersonIkkeFunnet, HentUtbetalingsinformasjonIkkeTilgang {
        logger.info("Mocker svar på request: Id=(ident={}, type={}, rolle={}), Periode=(fom={}, tom={}), ytelsetyper={} ",
                req.getId().getIdent(), req.getId().getIdentType().getValue(), req.getId().getRolle().getValue(),
                req.getPeriode().getFom(), req.getPeriode().getTom(),
                Arrays.toString(req.getYtelsestypeListe().toArray())
        );

        return null;
    }
}
