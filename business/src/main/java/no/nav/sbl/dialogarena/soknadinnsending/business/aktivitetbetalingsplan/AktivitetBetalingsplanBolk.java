package no.nav.sbl.dialogarena.soknadinnsending.business.aktivitetbetalingsplan;


import com.google.common.collect.Lists;
import no.nav.sbl.dialogarena.sendsoknad.domain.Faktum;
import no.nav.sbl.dialogarena.sendsoknad.domain.kravdialoginformasjon.SoknadRefusjonDagligreise;
import no.nav.sbl.dialogarena.soknadinnsending.business.service.*;
import no.nav.tjeneste.virksomhet.sakogaktivitet.v1.FinnAktivitetOgVedtakDagligReiseListePersonIkkeFunnet;
import no.nav.tjeneste.virksomhet.sakogaktivitet.v1.FinnAktivitetOgVedtakDagligReiseListeSikkerhetsbegrensning;
import no.nav.tjeneste.virksomhet.sakogaktivitet.v1.SakOgAktivitetV1;
import no.nav.tjeneste.virksomhet.sakogaktivitet.v1.informasjon.WSBetalingsplan;
import no.nav.tjeneste.virksomhet.sakogaktivitet.v1.informasjon.WSPeriode;
import no.nav.tjeneste.virksomhet.sakogaktivitet.v1.meldinger.WSFinnAktivitetOgVedtakDagligReiseListeRequest;
import no.nav.tjeneste.virksomhet.sakogaktivitet.v1.meldinger.WSFinnAktivitetOgVedtakDagligReiseListeResponse;
import org.apache.commons.lang3.StringUtils;
import org.joda.time.LocalDate;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import javax.inject.Inject;
import javax.inject.Named;
import java.util.Collections;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

import static no.nav.sbl.dialogarena.sendsoknad.domain.util.ServiceUtils.*;

@Component
public class AktivitetBetalingsplanBolk implements BolkService {

    private static final org.slf4j.Logger LOG = LoggerFactory.getLogger(AktivitetBetalingsplanBolk.class);

    @Inject
    private FaktaService faktaService;
    @Inject
    @Named("sakOgAktivitetEndpoint")
    private SakOgAktivitetV1 aktivitetWebService;

    private static Function<WSBetalingsplan, Faktum> betalingplanTilFaktum(final Long soknadId) {
        return wsVedtaksinformasjon -> {
            Faktum betalingsplan = new Faktum().medKey("vedtak.betalingsplan")
                    .medUnikProperty("id")
                    .medSoknadId(soknadId)
                    .medProperty("id", wsVedtaksinformasjon.getBetalingsplanId())
                    .medProperty("fom", datoTilString(wsVedtaksinformasjon.getUtgiftsperiode().getFom()))
                    .medProperty("tom", datoTilString(wsVedtaksinformasjon.getUtgiftsperiode().getTom()))
                    .medProperty("refunderbartBeloep", "" + wsVedtaksinformasjon.getBeloep())
                    .medProperty("alleredeSokt", "" + StringUtils.isNotBlank(wsVedtaksinformasjon.getJournalpostId()));
            if (StringUtils.isNotBlank(wsVedtaksinformasjon.getJournalpostId())) {
                betalingsplan.medProperty("sokerForPeriode", "false");
            }
            return betalingsplan;
        };
    }


    @Override
    public String tilbyrBolk() {
        return SoknadRefusjonDagligreise.VEDTAKPERIODER;
    }

    @Override
    public List<Faktum> genererSystemFakta(String fodselsnummer, Long soknadId) {
        Faktum vedtakFaktum = faktaService.hentFaktumMedKey(soknadId, "vedtak");
        if (vedtakFaktum != null) {
            return hentBetalingsplanerForVedtak(soknadId, fodselsnummer, vedtakFaktum.getProperties().get("aktivitetId")
                    , vedtakFaktum.getProperties().get("id"));

        }
        return null;
    }

    public List<Faktum> hentBetalingsplanerForVedtak(Long soknadId, String fodselsnummer, final String aktivitetId, final String vedtakId) {

        try {
            WSFinnAktivitetOgVedtakDagligReiseListeRequest request = new WSFinnAktivitetOgVedtakDagligReiseListeRequest()
                    .withPersonident(fodselsnummer)
                    .withPeriode(new WSPeriode().withFom(LocalDate.now().minusMonths(6)).withTom(LocalDate.now().plusMonths(2)));
            WSFinnAktivitetOgVedtakDagligReiseListeResponse response = aktivitetWebService.finnAktivitetOgVedtakDagligReiseListe(request);
            if (response == null) {
                return Lists.newArrayList();
            }
            return response.getAktivitetOgVedtakListe().stream()
                    .filter(wsAktivitetOgVedtak -> wsAktivitetOgVedtak.getAktivitetId().equals(aktivitetId))
                    .flatMap(wsAktivitetOgVedtak -> wsAktivitetOgVedtak.getSaksinformasjon().getVedtaksinformasjon().stream())
                    .filter(vedtak -> vedtak.getVedtakId().equals(vedtakId))
                    .flatMap(wsVedtaksinformasjon -> wsVedtaksinformasjon.getBetalingsplan().stream())
                    .map(betalingplanTilFaktum(soknadId)).collect(Collectors.toList());

        } catch (FinnAktivitetOgVedtakDagligReiseListePersonIkkeFunnet e) {
            LOG.debug("person ikke funnet", e);
            return Collections.emptyList();
        } catch (FinnAktivitetOgVedtakDagligReiseListeSikkerhetsbegrensning e) {
            throw new RuntimeException(e.getMessage(), e);
        }
    }
}
