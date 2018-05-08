package no.nav.sbl.dialogarena.soknadinnsending.consumer.utbetaling;

import no.nav.tjeneste.virksomhet.utbetaling.v1.HentUtbetalingsinformasjonIkkeTilgang;
import no.nav.tjeneste.virksomhet.utbetaling.v1.HentUtbetalingsinformasjonPeriodeIkkeGyldig;
import no.nav.tjeneste.virksomhet.utbetaling.v1.HentUtbetalingsinformasjonPersonIkkeFunnet;
import no.nav.tjeneste.virksomhet.utbetaling.v1.UtbetalingV1;
import no.nav.tjeneste.virksomhet.utbetaling.v1.informasjon.WSForespurtPeriode;
import no.nav.tjeneste.virksomhet.utbetaling.v1.informasjon.WSIdent;
import no.nav.tjeneste.virksomhet.utbetaling.v1.informasjon.WSIdentroller;
import no.nav.tjeneste.virksomhet.utbetaling.v1.informasjon.WSIdenttyper;
import no.nav.tjeneste.virksomhet.utbetaling.v1.meldinger.WSHentUtbetalingsinformasjonRequest;
import org.springframework.stereotype.Service;

import javax.inject.Inject;

import static org.joda.time.DateTime.now;

@Service
public class UtbetalingService {

    @Inject
    UtbetalingV1 utbetalingV1;

    public String dummy() {
        try {
            utbetalingV1.hentUtbetalingsinformasjon(
                    new WSHentUtbetalingsinformasjonRequest()
                            .withId(
                                    new WSIdent()
                                            .withIdentType(new WSIdenttyper().withValue("Personnr"))
                                            .withIdent("12345678912")
                                            .withRolle(new WSIdentroller().withValue("Rettighetshaver")))
                            .withPeriode(
                                    new WSForespurtPeriode()
                                            .withFom(now().minusDays(30))
                                            .withTom(now().plusDays(20))
                            )
            );
            return "ok";
        } catch (HentUtbetalingsinformasjonPeriodeIkkeGyldig hentUtbetalingsinformasjonPeriodeIkkeGyldig) {
            hentUtbetalingsinformasjonPeriodeIkkeGyldig.printStackTrace();
        } catch (HentUtbetalingsinformasjonPersonIkkeFunnet hentUtbetalingsinformasjonPersonIkkeFunnet) {
            hentUtbetalingsinformasjonPersonIkkeFunnet.printStackTrace();
        } catch (HentUtbetalingsinformasjonIkkeTilgang hentUtbetalingsinformasjonIkkeTilgang) {
            hentUtbetalingsinformasjonIkkeTilgang.printStackTrace();
        }

        return "feil";
    }
}
