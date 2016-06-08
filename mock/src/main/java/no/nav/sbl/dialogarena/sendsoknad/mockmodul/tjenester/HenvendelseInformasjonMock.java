package no.nav.sbl.dialogarena.sendsoknad.mockmodul.tjenester;

import no.nav.melding.domene.brukerdialog.behandlingsinformasjon.v1.*;
import no.nav.tjeneste.domene.brukerdialog.henvendelse.v2.henvendelse.HenvendelsePortType;
import no.nav.tjeneste.domene.brukerdialog.henvendelse.v2.meldinger.WSHentHenvendelseListeRequest;
import no.nav.tjeneste.domene.brukerdialog.henvendelse.v2.meldinger.WSHentHenvendelseListeResponse;
import org.joda.time.DateTime;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

import static java.util.UUID.randomUUID;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;



public class HenvendelseInformasjonMock {

    public static final String DAGPENGER_SKJEMAKODE_NAV_04_01_03 = "NAV 04-01.03";
    public static final String GJENOPPTAK_DAGPENGER_SKJEMAKODE_NAV_04_16_03 = "NAV 04-16.03";
    public static final String AAP_SKJEMAKODE_NAV_11_13_05 = "NAV 11-13.05";
    public static final String GENERISK_BEHANDLINGSID = "behandlingsid123";
    public static final String DAGPENGER_BEHANDLINGSID = "behandlingsidX";
    public static final String GJENOPPTAK_DAGPENGER_BEHANDLINGSID = "gjenopptakX";
    public static final String AAP_BEHANDLINGSID = "xxxx-mockbehandlingid2";
    public static final String DAGPENGER_ETTERSENDELSE_BEHANDLINGSID = "ettersendingbehandlingsid2";
    public static final String AAP_ETTERSENDELSE_BEHANDLINGSID = "ettersendingbehandlingsid1";
    public static final String DAGPENGEARKIVTEMA = "DAG";
    public static final String AAPARKIVTEMA = "AAP";
    public static final String DAGPENGER_BEHANDLINGSTEMA = "ab0001";
    public static final String AAP_BEHANDLINGSTEMA = "aX000X";

    public static HenvendelsePortType getHenvendelseSoknaderPortTypeMock() {

        HenvendelsePortType mock = mock(HenvendelsePortType.class);
        when(mock.hentHenvendelseListe(any(WSHentHenvendelseListeRequest.class))).thenAnswer(new Answer<Object>() {
            @Override
            public Object answer(InvocationOnMock invocationOnMock) throws Throwable {
                return soknadListe();
            }
        });
        return mock;
    }

    public static XMLHenvendelse gammelFerdigWSSoknad() {
        return new XMLHenvendelse()
                .withBehandlingsId("behandlingId1")
                .withJournalfortInformasjon(new XMLJournalfortInformasjon().withJournalpostId("348274526"))
                .withHenvendelseType(XMLHenvendelseType.DOKUMENTINNSENDING.toString())
                .withOpprettetDato(DateTime.now().minusDays(41))
                .withAvsluttetDato(DateTime.now().minusDays(40))
                .withMetadataListe(new XMLMetadataListe().withMetadata(
                        new XMLHovedskjema()
                                .withSkjemanummer("NAV 04-01.05"),
                        new XMLVedlegg()
                                .withSkjemanummer("NAV 04-01.03")
                                .withUuid(randomUUID().toString())
                                .withArkivreferanse("348128630")
                                .withInnsendingsvalg(XMLInnsendingsvalg.LASTET_OPP.value()),
                        new XMLVedlegg()
                                .withSkjemanummer("L7")
                                .withUuid(randomUUID().toString())
                                .withArkivreferanse("358128632")
                                .withInnsendingsvalg(XMLInnsendingsvalg.LASTET_OPP.value()),
                        new XMLVedlegg()
                                .withSkjemanummer("NAV 03-16.10")
                                .withUuid(randomUUID().toString())
                                .withArkivreferanse("358128630")
                                .withInnsendingsvalg(XMLInnsendingsvalg.SEND_SENERE.value())
                ));
    }

    public static WSHentHenvendelseListeResponse soknadListe() {
        return new WSHentHenvendelseListeResponse().withAny(
                kvitteringDokInnsending(),
                kvitteringEttersendelseDokInnsending(),
                kvitteringSendsoknad(),
                kvitteringEttersendelseSendsoknad(),
                kvitteringGjenopptakSendsoknad(),
                gammelFerdigWSSoknad()
        );
    }

    public static XMLHenvendelse kvitteringDokInnsending() {
        return new XMLHenvendelse()
                .withBehandlingsId(AAP_BEHANDLINGSID)
                .withHenvendelseType(XMLHenvendelseType.DOKUMENTINNSENDING.toString())
                .withMetadataListe(new XMLMetadataListe().withMetadata(
                        new XMLHovedskjema()
                                .withSkjemanummer(AAP_SKJEMAKODE_NAV_11_13_05)))
                .withOpprettetDato(DateTime.now().minusYears(1).minusDays(29))
                .withAvsluttetDato(DateTime.now().minusYears(1));
    }

    public static XMLHenvendelse kvitteringEttersendelseDokInnsending() {
        return new XMLHenvendelse()
                .withBehandlingsId(AAP_ETTERSENDELSE_BEHANDLINGSID)
                .withJournalfortInformasjon(new XMLJournalfortInformasjon().withJournalpostId("348274526"))
                .withHenvendelseType(XMLHenvendelseType.DOKUMENTINNSENDING.toString())
                .withMetadataListe(new XMLMetadataListe().withMetadata(
                        new XMLHovedskjema()
                                .withSkjemanummer(AAP_SKJEMAKODE_NAV_11_13_05),
                        new XMLVedlegg()
                                .withSkjemanummer("NAV 04-01.03")
                                .withUuid(randomUUID().toString())
                                .withArkivreferanse("368128630")
                                .withInnsendingsvalg(XMLInnsendingsvalg.LASTET_OPP.value()),
                        new XMLVedlegg()
                                .withSkjemanummer("NAV 04-01.03")
                                .withUuid(randomUUID().toString())
                                .withInnsendingsvalg(XMLInnsendingsvalg.LASTET_OPP.value())
                        ,
                        new XMLVedlegg()
                                .withSkjemanummer("L7")
                                .withUuid(randomUUID().toString())
                                .withArkivreferanse("358128632")
                                .withInnsendingsvalg(XMLInnsendingsvalg.LASTET_OPP.value()),
                        new XMLVedlegg()
                                .withSkjemanummer("NAV 00-01.01")
                                .withUuid(randomUUID().toString())
                                .withArkivreferanse("378128630")
                                .withInnsendingsvalg(XMLInnsendingsvalg.SENDES_IKKE.value())
                ))
                .withOpprettetDato(DateTime.now().minusDays(2))
                .withAvsluttetDato(DateTime.now());
    }


    public static XMLHenvendelse kvitteringEttersendelseSendsoknad() {
        return new XMLHenvendelse()
                .withBehandlingsId(DAGPENGER_ETTERSENDELSE_BEHANDLINGSID)
                .withJournalfortInformasjon(new XMLJournalfortInformasjon().withJournalpostId("368274526"))
                .withHenvendelseType(XMLHenvendelseType.SEND_SOKNAD.toString())
                .withOpprettetDato(DateTime.now().minusDays(3))
                .withAvsluttetDato(DateTime.now())
                .withLestDato(DateTime.now().minusDays(2))
                .withMetadataListe(new XMLMetadataListe().withMetadata(
                        new XMLHovedskjema()
                                .withSkjemanummer(DAGPENGER_SKJEMAKODE_NAV_04_01_03),
                        new XMLVedlegg()
                                .withSkjemanummer("NAV 04-01.03")
                                .withUuid(randomUUID().toString())
                                .withArkivreferanse("388128630")
                                .withInnsendingsvalg(XMLInnsendingsvalg.LASTET_OPP.value()),
                        new XMLVedlegg()
                                .withSkjemanummer("NAV 03-16.10")
                                .withUuid(randomUUID().toString())
                                .withArkivreferanse("398128630")
                                .withInnsendingsvalg(XMLInnsendingsvalg.LASTET_OPP.value()),
                        new XMLVedlegg()
                                .withSkjemanummer("L7")
                                .withUuid(randomUUID().toString())
                                .withArkivreferanse("358128632")
                                .withInnsendingsvalg(XMLInnsendingsvalg.LASTET_OPP.value())
                ));
    }

    public static XMLHenvendelse kvitteringSendsoknad() {
        return new XMLHenvendelse()
                .withBehandlingsId(DAGPENGER_BEHANDLINGSID)
                .withJournalfortInformasjon(new XMLJournalfortInformasjon().withJournalpostId("368274540"))
                .withHenvendelseType(XMLHenvendelseType.SEND_SOKNAD.toString())
                .withOpprettetDato(DateTime.now().minusDays(6))
                .withLestDato(DateTime.now())
                .withAvsluttetDato(DateTime.now().minusDays(5))
                .withMetadataListe(new XMLMetadataListe().withMetadata(
                        new XMLHovedskjema()
                                .withSkjemanummer(DAGPENGER_SKJEMAKODE_NAV_04_01_03),
                        new XMLVedlegg()
                                .withSkjemanummer("NAV 04-01.03")
                                .withUuid(randomUUID().toString())
                                .withArkivreferanse("378128640")
                                .withInnsendingsvalg(XMLInnsendingsvalg.LASTET_OPP.value()),
                        new XMLVedlegg()
                                .withSkjemanummer("L7")
                                .withUuid(randomUUID().toString())
                                .withArkivreferanse("358128632")
                                .withInnsendingsvalg(XMLInnsendingsvalg.LASTET_OPP.value()),
                        new XMLVedlegg()
                                .withSkjemanummer("NAV 03-16.10")
                                .withUuid(randomUUID().toString())
                                .withArkivreferanse("378128650")
                                .withInnsendingsvalg(XMLInnsendingsvalg.SEND_SENERE.value())
                ));
    }

    public static XMLHenvendelse kvitteringGjenopptakSendsoknad() {
        return new XMLHenvendelse()
                .withBehandlingsId(GJENOPPTAK_DAGPENGER_BEHANDLINGSID)
                .withJournalfortInformasjon(new XMLJournalfortInformasjon().withJournalpostId("368274536"))
                .withHenvendelseType(XMLHenvendelseType.SEND_SOKNAD.toString())
                .withOpprettetDato(DateTime.now().minusDays(6))
                .withAvsluttetDato(DateTime.now())
                .withLestDato(DateTime.now().minusDays(5))
                .withMetadataListe(new XMLMetadataListe().withMetadata(
                        new XMLHovedskjema()
                                .withSkjemanummer(GJENOPPTAK_DAGPENGER_SKJEMAKODE_NAV_04_16_03),
                        new XMLVedlegg()
                                .withSkjemanummer(GJENOPPTAK_DAGPENGER_SKJEMAKODE_NAV_04_16_03)
                                .withUuid(randomUUID().toString())
                                .withArkivreferanse("378128640")
                                .withInnsendingsvalg(XMLInnsendingsvalg.LASTET_OPP.value()),
                        new XMLVedlegg()
                                .withSkjemanummer("NAV 03-16.10")
                                .withUuid(randomUUID().toString())
                                .withInnsendingsvalg(XMLInnsendingsvalg.SEND_SENERE.value())
                ));
    }

}

