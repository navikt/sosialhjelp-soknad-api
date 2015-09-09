package no.nav.sbl.dialogarena.soknadinnsending.business.service;

import com.google.common.base.Function;
import com.google.common.base.Predicate;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import no.nav.sbl.dialogarena.soknadinnsending.business.domain.Faktum;
import no.nav.tjeneste.virksomhet.sakogaktivitet.v1.FinnAktivitetOgVedtakDagligReiseListePersonIkkeFunnet;
import no.nav.tjeneste.virksomhet.sakogaktivitet.v1.FinnAktivitetOgVedtakDagligReiseListeSikkerhetsbegrensning;
import no.nav.tjeneste.virksomhet.sakogaktivitet.v1.FinnAktivitetsinformasjonListePersonIkkeFunnet;
import no.nav.tjeneste.virksomhet.sakogaktivitet.v1.FinnAktivitetsinformasjonListeSikkerhetsbegrensning;
import no.nav.tjeneste.virksomhet.sakogaktivitet.v1.SakOgAktivitetV1;
import no.nav.tjeneste.virksomhet.sakogaktivitet.v1.informasjon.WSAktivitet;
import no.nav.tjeneste.virksomhet.sakogaktivitet.v1.informasjon.WSAktivitetOgVedtak;
import no.nav.tjeneste.virksomhet.sakogaktivitet.v1.informasjon.WSPeriode;
import no.nav.tjeneste.virksomhet.sakogaktivitet.v1.informasjon.WSVedtaksinformasjon;
import no.nav.tjeneste.virksomhet.sakogaktivitet.v1.meldinger.WSFinnAktivitetOgVedtakDagligReiseListeRequest;
import no.nav.tjeneste.virksomhet.sakogaktivitet.v1.meldinger.WSFinnAktivitetOgVedtakDagligReiseListeResponse;
import no.nav.tjeneste.virksomhet.sakogaktivitet.v1.meldinger.WSFinnAktivitetsinformasjonListeRequest;
import no.nav.tjeneste.virksomhet.sakogaktivitet.v1.meldinger.WSFinnAktivitetsinformasjonListeResponse;
import org.apache.commons.collections15.Transformer;
import org.joda.time.LocalDate;
import org.springframework.stereotype.Service;

import javax.inject.Inject;
import javax.inject.Named;
import java.util.List;

import static no.nav.modig.lang.collections.IterUtils.on;

@Service
public class AktiviteterService {

    private static final Predicate<Faktum> BARE_AKTIVITETER_SOM_KAN_HA_STONADER = new Predicate<Faktum>() {
        @Override
        public boolean apply(Faktum faktum) {
            return "true".equals(faktum.getProperties().get("erStoenadsberettiget"));
        }
    };
    @Inject
    @Named("sakOgAktivitetEndpoint")
    private SakOgAktivitetV1 aktivitetWebService;

    private AktiviteterTransformer transformer = new AktiviteterTransformer();
    private VedtakTransformer vedtakTransformer = new VedtakTransformer();

    public List<Faktum> hentAktiviteter(String fodselnummer) {
        try {
            WSFinnAktivitetsinformasjonListeResponse aktiviteter = aktivitetWebService.finnAktivitetsinformasjonListe(lagAktivitetsRequest(fodselnummer));
            if (aktiviteter == null) {
                return Lists.newArrayList();
            }
            List<Faktum> listeMedAktiviteter = Lists.transform(aktiviteter.getAktivitetListe(), transformer);
            return Lists.newArrayList(Iterables.filter(listeMedAktiviteter, BARE_AKTIVITETER_SOM_KAN_HA_STONADER));
        } catch (FinnAktivitetsinformasjonListePersonIkkeFunnet | FinnAktivitetsinformasjonListeSikkerhetsbegrensning e) {
            throw new RuntimeException(e.getMessage(), e);
        }
    }

    public List<Faktum> hentVedtak(String fodselsnummer) {
        try {
            WSFinnAktivitetOgVedtakDagligReiseListeResponse response = aktivitetWebService.finnAktivitetOgVedtakDagligReiseListe(new WSFinnAktivitetOgVedtakDagligReiseListeRequest().withPersonident(fodselsnummer));
            return on(response.getAktivitetOgVedtakListe()).flatmap(vedtakTransformer).collect();

        } catch (FinnAktivitetOgVedtakDagligReiseListeSikkerhetsbegrensning | FinnAktivitetOgVedtakDagligReiseListePersonIkkeFunnet e) {
            throw new RuntimeException(e.getMessage(), e);
        }
    }

    private WSFinnAktivitetsinformasjonListeRequest lagAktivitetsRequest(String fodselnummer) {
        return new WSFinnAktivitetsinformasjonListeRequest()
                .withPersonident(fodselnummer)
                .withPeriode(new WSPeriode().withFom(LocalDate.now().minusYears(1)).withTom(LocalDate.now()));
    }

    private class AktiviteterTransformer implements Function<WSAktivitet, Faktum> {

        @Override
        public Faktum apply(WSAktivitet wsAktivitet) {
            Faktum faktum = new Faktum()
                    .medKey("aktivitet")
                    .medProperty("id", wsAktivitet.getAktivitetId())
                    .medProperty("navn", wsAktivitet.getAktivitetsnavn());

            WSPeriode periode = wsAktivitet.getPeriode();
            faktum.medProperty("fom", datoTilString(periode.getFom()));
            faktum.medProperty("tom", datoTilString(periode.getTom()));
            faktum.medProperty("erStoenadsberettiget", "" + wsAktivitet.isErStoenadsberettigetAktivitet());

            return faktum;
        }

    }

    private String datoTilString(LocalDate date) {
        return date != null ? date.toString("yyyy-MM-dd") : "";
    }

    private class VedtakTransformer implements Transformer<WSAktivitetOgVedtak, Iterable<Faktum>> {
        @Override
        public Iterable<Faktum> transform(WSAktivitetOgVedtak wsAktivitetOgVedtak) {
            return Lists.transform(wsAktivitetOgVedtak.getSaksinformasjon().getVedtaksinformasjon(), new VedtakinformasjonTransformer(wsAktivitetOgVedtak));
        }
    }

    private class VedtakinformasjonTransformer implements Function<WSVedtaksinformasjon, Faktum> {
        private final WSAktivitetOgVedtak aktivitet;

        public VedtakinformasjonTransformer(WSAktivitetOgVedtak aktivitet) {
            this.aktivitet = aktivitet;
        }

        @Override
        public Faktum apply(WSVedtaksinformasjon input) {
            Faktum faktum = new Faktum()
                    .medKey("vedtak")
                    .medProperty("aktivitetId", aktivitet.getAktivitetId())
                    .medProperty("aktivitetNavn", aktivitet.getAktivitetsnavn())
                    .medProperty("erStoenadsberettiget", "" + aktivitet.isErStoenadsberettigetAktivitet())
                    .medProperty("forventetDagligParkeringsutgift", nullToBlank(input.getForventetDagligParkeringsutgift()))
                    .medProperty("dagsats", nullToBlank(input.getDagsats()))
                    .medProperty("trengerParkering", nullToBlank(input.isTrengerParkering()))
                    .medProperty("id", input.getVedtakId())
                    ;
            WSPeriode periode = aktivitet.getPeriode();
            faktum.medProperty("aktivitetFom", datoTilString(periode.getFom()));
            faktum.medProperty("aktivitetTom", datoTilString(periode.getTom()));

            periode = input.getPeriode();
            faktum.medProperty("fom", datoTilString(periode.getFom()));
            faktum.medProperty("tom", datoTilString(periode.getTom()));

            return faktum;
        }
    }

    private static String nullToBlank(Object value) {
        if(value != null){
            return value.toString();
        }
        return "";
    }
}
