package no.nav.sbl.dialogarena.sendsoknad.mockmodul.tjenester;

import no.nav.tjeneste.virksomhet.sakogaktivitet.v1.*;
import no.nav.tjeneste.virksomhet.sakogaktivitet.v1.informasjon.*;
import no.nav.tjeneste.virksomhet.sakogaktivitet.v1.meldinger.WSFinnAktivitetOgVedtakDagligReiseListeRequest;
import no.nav.tjeneste.virksomhet.sakogaktivitet.v1.meldinger.WSFinnAktivitetOgVedtakDagligReiseListeResponse;
import no.nav.tjeneste.virksomhet.sakogaktivitet.v1.meldinger.WSFinnAktivitetsinformasjonListeRequest;
import no.nav.tjeneste.virksomhet.sakogaktivitet.v1.meldinger.WSFinnAktivitetsinformasjonListeResponse;
import org.joda.time.LocalDate;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class AktiviteterMock implements SakOgAktivitetV1 {

    @Override
    public void ping() {

    }

    @Override
    public WSFinnAktivitetOgVedtakDagligReiseListeResponse finnAktivitetOgVedtakDagligReiseListe(WSFinnAktivitetOgVedtakDagligReiseListeRequest wsFinnAktivitetOgVedtakDagligReiseListeRequest) throws FinnAktivitetOgVedtakDagligReiseListeSikkerhetsbegrensning, FinnAktivitetOgVedtakDagligReiseListePersonIkkeFunnet {
        WSFinnAktivitetOgVedtakDagligReiseListeResponse response = new WSFinnAktivitetOgVedtakDagligReiseListeResponse();
        response.withAktivitetOgVedtakListe(new WSAktivitetOgVedtak()
                .withPeriode(new WSPeriode().withFom(new LocalDate(2015, 1, 1)).withTom(new LocalDate(2015, 12, 31)))
                .withAktivitetId("100")
                .withAktivitetsnavn("navn på aktivitet")
                .withErStoenadsberettigetAktivitet(true)
                .withSaksinformasjon(new WSSaksinformasjon().withSaksnummerArena("saksnummerarena").withVedtaksinformasjon(
                        new WSVedtaksinformasjon()
                                .withPeriode(new WSPeriode().withFom(new LocalDate(2015, 1, 1)).withTom(new LocalDate(2015, 3, 31)))
                                .withVedtakId("1000")
                                .withForventetDagligParkeringsutgift(100)
                                .withTrengerParkering(true)
                                .withDagsats(555.0)
                                .withBetalingsplan(createBetalingsplaner(1, new LocalDate(2015, 1, 1), new LocalDate(2015, 3, 31), 7))
                )
                        .withVedtaksinformasjon(
                                new WSVedtaksinformasjon()
                                        .withPeriode(new WSPeriode().withFom(new LocalDate(2015, 5, 1)).withTom(new LocalDate(2015, 12, 24)))
                                        .withVedtakId("1002")
                                        .withForventetDagligParkeringsutgift(50)
                                        .withTrengerParkering(false)
                                        .withDagsats(50.0)
                                        .withBetalingsplan(createBetalingsplaner(1000, new LocalDate(2015, 5, 1), new LocalDate(2015, 12, 24), 30))

                        )));
        return response;
    }

    private Collection<WSBetalingsplan> createBetalingsplaner(int startId, LocalDate fom, LocalDate tom, int antDagerBetalingsplan) {
        List<WSBetalingsplan> result = new ArrayList<>();
        int id = startId;
        for (int i = 0; ; i += antDagerBetalingsplan) {
            result.add(new WSBetalingsplan()
                    .withBetalingsplanId("" + (id++))
                    .withJournalpostId("" + (id % 3 == 0 ? id : ""))
                    .withBeloep(((i + 1) * 300 % 5000))
                    .withUtgiftsperiode(new WSPeriode()
                            .withFom(fom.plusDays(i))
                            .withTom(fom.plusDays(i + antDagerBetalingsplan))));

            if (fom.plusDays(i + antDagerBetalingsplan).isAfter(tom)) {
                return result;
            }
        }
    }

    @Override
    public WSFinnAktivitetsinformasjonListeResponse finnAktivitetsinformasjonListe(WSFinnAktivitetsinformasjonListeRequest wsFinnAktivitetsinformasjonListeRequest) throws FinnAktivitetsinformasjonListeSikkerhetsbegrensning, FinnAktivitetsinformasjonListePersonIkkeFunnet {
        WSFinnAktivitetsinformasjonListeResponse response = new WSFinnAktivitetsinformasjonListeResponse();
        WSPeriode periode = new WSPeriode().withFom(new LocalDate("2015-01-15")).withTom(new LocalDate("2015-02-15"));
        WSAktivitet aktivitet = new WSAktivitet()
                .withAktivitetId("9999")
                .withErStoenadsberettigetAktivitet(true)
                .withAktivitetsnavn("Arbeidspraksis i ordinær virksomhet")
                .withPeriode(periode);

        WSPeriode periode2 = new WSPeriode().withFom(new LocalDate("2015-02-28"));
        WSAktivitet aktivitet2 = new WSAktivitet()
                .withAktivitetId("8888")
                .withErStoenadsberettigetAktivitet(true)
                .withAktivitetsnavn("Arbeid med bistand")
                .withPeriode(periode2);


        response.withAktivitetListe(aktivitet, aktivitet2);
        return response;
    }
}
