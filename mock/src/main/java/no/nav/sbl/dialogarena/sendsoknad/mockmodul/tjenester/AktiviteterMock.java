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
                                .withPeriode(new WSPeriode().withFom(new LocalDate(2015, 1, 1)).withTom(new LocalDate(2015, 4, 30)))
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
                    .withJournalpostId("" + (id % 2 == 0 ? id : ""))
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
        WSAktivitetstyper aktivitetstype = new WSAktivitetstyper().withValue("arbeidspraksis");
        WSAktivitet aktivitet = new WSAktivitet()
                .withAktivitetId("9999")
                .withErStoenadsberettigetAktivitet(true)
                .withAktivitetsnavn("Arbeidspraksis i ordinær virksomhet med sluttdato fra arena ")
                .withPeriode(periode)
                .withAktivitetstype(aktivitetstype)
                .withArrangoer("Horten kommune");

        WSPeriode periode2 = new WSPeriode().withFom(new LocalDate("2015-02-28")).withTom(new LocalDate("2015-04-15"));
        WSAktivitetstyper aktivitetstype2 = new WSAktivitetstyper().withValue("jobbsøkerkurs");
        WSAktivitet aktivitet2 = new WSAktivitet()
                .withAktivitetId("8888")
                .withErStoenadsberettigetAktivitet(true)
                .withAktivitetsnavn("Arbeid med bistand jobbsøkerkurs med sluttdato fra arena")
                .withPeriode(periode2)
                .withAktivitetstype(aktivitetstype2)
                .withArrangoer("Oslo kommune, Bydel Ullern");

        WSPeriode periode3 = new WSPeriode().withFom(new LocalDate("2015-05-28"));
        WSAktivitetstyper aktivitetstype3 = new WSAktivitetstyper().withValue("amo");
        WSAktivitet aktivitet3 = new WSAktivitet()
                .withAktivitetId("7777")
                .withErStoenadsberettigetAktivitet(true)
                .withAktivitetsnavn("Arbeid med service amo uten sluttdato fra arena")
                .withPeriode(periode3)
                .withAktivitetstype(aktivitetstype3)
                .withArrangoer("Oslo kommune, Bydel Alna");

        WSPeriode periode4 = new WSPeriode().withFom(new LocalDate("2015-01-15"));
        WSAktivitetstyper aktivitetstype4 = new WSAktivitetstyper().withValue("arbeidspraksis");
        WSAktivitet aktivitet4 = new WSAktivitet()
                .withAktivitetId("6666")
                .withErStoenadsberettigetAktivitet(true)
                .withAktivitetsnavn("Arbeidspraksis i ordinær virksomhet uten sluttdato fra arena ")
                .withPeriode(periode4)
                .withAktivitetstype(aktivitetstype4)
                .withArrangoer("Ulsteinvik kommune");

        response.withAktivitetListe(aktivitet, aktivitet2, aktivitet3, aktivitet4);
        return response;
    }
}
