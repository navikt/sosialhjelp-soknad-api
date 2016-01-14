package no.nav.sbl.dialogarena.soknadinnsending.business.service;


import com.google.common.collect.Lists;
import no.nav.sbl.dialogarena.soknadinnsending.business.domain.Faktum;
import no.nav.sbl.dialogarena.soknadinnsending.business.person.BolkService;
import no.nav.tjeneste.virksomhet.sakogaktivitet.v1.FinnAktivitetOgVedtakDagligReiseListePersonIkkeFunnet;
import no.nav.tjeneste.virksomhet.sakogaktivitet.v1.FinnAktivitetOgVedtakDagligReiseListeSikkerhetsbegrensning;
import no.nav.tjeneste.virksomhet.sakogaktivitet.v1.SakOgAktivitetV1;
import no.nav.tjeneste.virksomhet.sakogaktivitet.v1.informasjon.WSAktivitetOgVedtak;
import no.nav.tjeneste.virksomhet.sakogaktivitet.v1.informasjon.WSBetalingsplan;
import no.nav.tjeneste.virksomhet.sakogaktivitet.v1.informasjon.WSPeriode;
import no.nav.tjeneste.virksomhet.sakogaktivitet.v1.informasjon.WSVedtaksinformasjon;
import no.nav.tjeneste.virksomhet.sakogaktivitet.v1.meldinger.WSFinnAktivitetOgVedtakDagligReiseListeRequest;
import no.nav.tjeneste.virksomhet.sakogaktivitet.v1.meldinger.WSFinnAktivitetOgVedtakDagligReiseListeResponse;
import org.apache.commons.collections15.Transformer;
import org.apache.commons.lang3.StringUtils;
import org.joda.time.LocalDate;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import javax.inject.Inject;
import javax.inject.Named;
import java.util.Collections;
import java.util.List;

import static no.nav.modig.lang.collections.IterUtils.on;
import static no.nav.sbl.dialogarena.soknadinnsending.business.service.ServiceUtils.datoTilString;

@Component
public class AktivitetBetalingsplanService implements BolkService {

    private static final org.slf4j.Logger LOG = LoggerFactory.getLogger(AktivitetBetalingsplanService.class);
    private static final Transformer<WSAktivitetOgVedtak, Iterable<WSVedtaksinformasjon>> AKTIVITET_TIL_VEDTAK = new Transformer<WSAktivitetOgVedtak, Iterable<WSVedtaksinformasjon>>() {
        @Override
        public Iterable<WSVedtaksinformasjon> transform(WSAktivitetOgVedtak wsAktivitetOgVedtak) {
            return wsAktivitetOgVedtak.getSaksinformasjon().getVedtaksinformasjon();
        }
    };
    private static final Transformer<WSVedtaksinformasjon, Iterable<WSBetalingsplan>> VEDTAK_TIL_BETALINGSPLAN = new Transformer<WSVedtaksinformasjon, Iterable<WSBetalingsplan>>() {
        @Override
        public Iterable<WSBetalingsplan> transform(WSVedtaksinformasjon wsVedtaksinformasjon) {
            return wsVedtaksinformasjon.getBetalingsplan();
        }
    };
    @Inject
    private FaktaService faktaService;
    @Inject
    @Named("sakOgAktivitetEndpoint")
    private SakOgAktivitetV1 aktivitetWebService;

    private static Transformer<WSBetalingsplan, Faktum> betalingplanTilFaktum(final Long soknadId) {
        return new Transformer<WSBetalingsplan, Faktum>() {
            @Override
            public Faktum transform(WSBetalingsplan wsVedtaksinformasjon) {
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
            }
        };
    }

    private static org.apache.commons.collections15.Predicate<WSVedtaksinformasjon> vedtakMedId(final String vedtakId) {
        return new org.apache.commons.collections15.Predicate<WSVedtaksinformasjon>() {
            @Override
            public boolean evaluate(WSVedtaksinformasjon vedtak) {
                return vedtak.getVedtakId().equals(vedtakId);
            }
        };
    }

    private static org.apache.commons.collections15.Predicate<WSAktivitetOgVedtak> aktivitetMatcherId(final String aktivitetId) {
        return new org.apache.commons.collections15.Predicate<WSAktivitetOgVedtak>() {
            @Override
            public boolean evaluate(WSAktivitetOgVedtak wsAktivitetOgVedtak) {
                return wsAktivitetOgVedtak.getAktivitetId().equals(aktivitetId);
            }
        };
    }

    @Override
    public String tilbyrBolk() {
        return VEDTAKPERIODER;
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
            return on(response.getAktivitetOgVedtakListe())
                    .filter(aktivitetMatcherId(aktivitetId))
                    .flatmap(AKTIVITET_TIL_VEDTAK)
                    .filter(vedtakMedId(vedtakId))
                    .flatmap(VEDTAK_TIL_BETALINGSPLAN)
                    .map(betalingplanTilFaktum(soknadId)).collect();

        } catch (FinnAktivitetOgVedtakDagligReiseListePersonIkkeFunnet e) {
            LOG.debug("person ikke funnet", e);
            return Collections.emptyList();
        } catch (FinnAktivitetOgVedtakDagligReiseListeSikkerhetsbegrensning e) {
            throw new RuntimeException(e.getMessage(), e);
        }
    }
}
