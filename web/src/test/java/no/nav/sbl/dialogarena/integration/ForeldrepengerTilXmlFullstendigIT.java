package no.nav.sbl.dialogarena.integration;


import no.nav.sbl.dialogarena.sendsoknad.domain.Vedlegg;
import no.nav.sbl.dialogarena.sendsoknad.domain.kravdialoginformasjon.ForeldrepengerInformasjon;
import no.nav.sbl.dialogarena.sendsoknad.domain.transformer.foreldrepenger.engangsstonad.Stonadstyper;
import org.junit.Before;
import org.junit.Test;

import java.util.HashMap;
import java.util.Map;

import static no.nav.sbl.dialogarena.sendsoknad.domain.transformer.foreldrepenger.engangsstonad.Skjemanummer.P3;
import static no.nav.sbl.dialogarena.sendsoknad.domain.transformer.foreldrepenger.engangsstonad.Skjemanummer.P5;
import static no.nav.sbl.dialogarena.sendsoknad.domain.transformer.foreldrepenger.engangsstonad.Skjemanummer.R4;
import static org.assertj.core.api.Assertions.assertThat;

public class ForeldrepengerTilXmlFullstendigIT extends AbstractIT {

    private String engangsstonadSkjemanummer = new ForeldrepengerInformasjon().getSkjemanummer().get(1);

    @Before
    public void setup() throws Exception {
        EndpointDataMocking.setupMockWsEndpointData();
    }

    @Test
    public void morFodsel() {
        Map<String, String> periodeProperties = new HashMap<>();
        periodeProperties.put("land", "AFG");
        periodeProperties.put("fradato", "2017-01-02");
        periodeProperties.put("tildato", "2017-04-01");

        SoknadTester testSoknad = soknadMedDelstegstatusOpprettet(engangsstonadSkjemanummer)
                .faktum("soknadsvalg.stonadstype").withValue(Stonadstyper.ENGANGSSTONAD_MOR).utforEndring()
                .faktum("soknadsvalg.fodselelleradopsjon").withValue("fodsel").utforEndring()

                .faktum("tilknytningnorge.oppholder").withValue("true").utforEndring()
                .faktum("tilknytningnorge.tidligere").withValue("false").utforEndring()
                .nyttFaktum("tilknytningnorge.tidligere.periode").withProperties(periodeProperties).opprett()
                .faktum("tilknytningnorge.fremtidig").withValue("true").utforEndring()

                .faktum("barnet.dato").withValue("2017-06-15").utforEndring()
                .faktum("barnet.antall").withValue("2").utforEndring()
                .faktum("barnet.termindatering").withValue("2017-04-05").utforEndring()
                .faktum("barnet.signertterminbekreftelse").withValue("Jordmor Mats").utforEndring()
                .faktum("veiledning.mor.terminbekreftelse").withValue("merEnn26Uker")
                .withProperty("skalHaFodselsattest", "true").utforEndring()

                .faktum("infofar.opplysninger.fornavn").withValue("Test").utforEndring()
                .faktum("infofar.opplysninger.etternavn").withValue("Testesen").utforEndring()
                .faktum("infofar.opplysninger.kanIkkeOppgi").withValue("true").utforEndring()
                .faktum("infofar.opplysninger.kanIkkeOppgi.true.arsak")
                .withValue("utenlandsk").withProperty("land", "AUS").utforEndring()
                .faktum("infofar.opplysninger.kanIkkeOppgi.true.arsak.utenlandsk.fodselsnummer")
                .withValue("123456").utforEndring()

                .faktum("tilleggsopplysninger.fritekst").withValue("Test").utforEndring()

                .hentPaakrevdeVedlegg()
                .vedlegg(P3).withInnsendingsValg(Vedlegg.Status.SendesSenere).utforEndring()
                .hentPaakrevdeVedlegg()
                .vedlegg(R4).withInnsendingsValg(Vedlegg.Status.SendesSenere).utforEndring()
                .hentPaakrevdeVedlegg()
                .soknad()
                .settDelstegstatus("oppsummering");

        assertThat(testSoknad.hentAlternativRepresentasjonResponseMedStatus().getStatus()).isEqualTo(200);

    }

    @Test
    public void farAdopsjon() {
        SoknadTester testSoknad = soknadMedDelstegstatusOpprettet(engangsstonadSkjemanummer)
                .faktum("soknadsvalg.stonadstype").withValue(Stonadstyper.ENGANGSSTONAD_FAR).utforEndring()
                .faktum("soknadsvalg.fodselelleradopsjon").withValue("adopsjon").utforEndring()

                .faktum("rettigheter.overtak").withValue("overtattOmsorgInnen53UkerFodsel").utforEndring()

                .faktum("tilknytningnorge.oppholder").withValue("true").utforEndring()
                .faktum("tilknytningnorge.tidligere").withValue("true").utforEndring()
                .faktum("tilknytningnorge.fremtidig").withValue("true").utforEndring()

                .faktum("barnet.dato").withValue("2017-06-08").utforEndring()
                .faktum("barnet.antall").withValue("2").utforEndring()
                .faktum("barnet.alder").withValue("2016-10-20").utforEndring()

                .faktum("infomor.opplysninger.fornavn").withValue("Test").utforEndring()
                .faktum("infomor.opplysninger.etternavn").withValue("Testesen").utforEndring()
                .faktum("infomor.opplysninger.personinfo").withProperty("personnummer", "***REMOVED***").utforEndring()

                .hentPaakrevdeVedlegg()
                .vedlegg(P5).withInnsendingsValg(Vedlegg.Status.LastetOpp).utforEndring()
                .hentPaakrevdeVedlegg()
                .soknad().settDelstegstatus("oppsummering");

        assertThat(testSoknad.hentAlternativRepresentasjonResponseMedStatus().getStatus()).isEqualTo(200);

    }
}
