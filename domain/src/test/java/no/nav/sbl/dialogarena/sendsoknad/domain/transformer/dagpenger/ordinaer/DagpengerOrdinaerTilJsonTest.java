package no.nav.sbl.dialogarena.sendsoknad.domain.transformer.dagpenger.ordinaer;

import junit.framework.AssertionFailedError;
import no.nav.sbl.dialogarena.sendsoknad.domain.*;
import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.joda.time.DateTime.now;

public class DagpengerOrdinaerTilJsonTest {


    private JsonDagpengerSoknad jsonSoknad;

    @Test
    public void skalKonvertereFaktumStruktur() {
        WebSoknad soknad = new WebSoknad()
                .medId(1234)
                .medskjemaNummer("NAV 04-01.03")
                .medBehandlingId("12xy")
                .medStatus(SoknadInnsendingStatus.UNDER_ARBEID)
                .medAktorId("12345")
                .medOppretteDato(now())
                .medDelstegStatus(DelstegStatus.VEDLEGG_VALIDERT)
                .medSoknadPrefix("dagpenger.ordinaer")
                .medFaktum(new Faktum().medKey("fnr").medValue("***REMOVED***"))
                .medFaktum(new Faktum().medKey("utenlandskKontoLand").medValue("Norge"))
                .medFaktum(new Faktum().medKey("navn").medValue("Donald D. Mockmann"))
                .medFaktum(new Faktum().medKey("alder").medValue("54"))
                .medFaktum(new Faktum().medKey("sekundarAdresse").medValue("Poitigatan 55, Nord-Poiti, 1111 Helsinki, Finland, Finland"))
                .medFaktum(new Faktum().medKey("kjonn").medValue("m"))
                .medFaktum(new Faktum().medKey("utenlandskKontoBanknavn").medValue("Nordea"))
                .medFaktum(new Faktum().medKey("gjeldendeAdresse").medValue("Grepalida 44, 0560 OSLO"))
                .medFaktum(new Faktum().medKey("statsborgerskapType").medValue("norsk"))
                .medFaktum(new Faktum().medKey("kontonummer").medValue("9876 98 98765"))
                .medFaktum(new Faktum().medKey("mellomnavn").medValue("D."))
                .medFaktum(new Faktum().medKey("gjeldendeAdresseType").medValue("BOSTEDSADRESSE"))
                .medFaktum(new Faktum().medKey("utslagskriterier.fortsettlikevel").medValue("true"))
                .medFaktum(new Faktum().medKey("utslagskriterier.dagpenger.utbetaling").medValue("nei"))
                .medFaktum(new Faktum().medKey("informasjonsside.lestbrosjyre").medValue("true"))
                .medFaktum(new Faktum().medKey("reellarbeidssoker.villigdeltid").medValue("true"))
                .medFaktum(new Faktum().medKey("reellarbeidssoker.villigpendle").medValue("true"))
                .medFaktum(new Faktum().medKey("reellarbeidssoker.villighelse").medValue("false"))
                .medFaktum(new Faktum().medKey("reellarbeidssoker.villigjobb").medValue("true"))
                .medFaktum(new Faktum().medKey("arbeidsforhold.datodagpenger").medValue("2018-05-02"))
                .medFaktum(new Faktum().medKey("arbeidsforhold.arbeidstilstand").medValue("varierendeArbeidstid"))
                .medFaktum(new Faktum().medKey("egennaering.driveregennaering").medValue("true"))
                .medFaktum(new Faktum().medKey("egennaering.gardsbruk").medValue("true"))
                .medFaktum(new Faktum().medKey("egennaering.fangstogfiske").medValue("true"))
                .medFaktum(new Faktum().medKey("utdanning").medValue("ikkeUtdanning"))
                .medFaktum(new Faktum().medKey("andreytelser.ytelser").medValue(null))
                .medFaktum(new Faktum().medKey("andreytelser.ikkeavtale").medValue("true"))
                .medFaktum(new Faktum().medKey("barn.leggtil").medValue(null))
                .medFaktum(new Faktum().medKey("tilleggsopplysninger.fritekst").medValue(null))
                .medFaktum(new Faktum().medKey("andreytelser.ytelser.offentligtjenestepensjon").medValue("false"))
                .medFaktum(new Faktum().medKey("andreytelser.ytelser.privattjenestepensjon").medValue("false"))
                .medFaktum(new Faktum().medKey("andreytelser.ytelser.stonadfisker").medValue("false"))
                .medFaktum(new Faktum().medKey("andreytelser.ytelser.garantilott").medValue("false"))
                .medFaktum(new Faktum().medKey("andreytelser.ytelser.etterlonn").medValue("false"))
                .medFaktum(new Faktum().medKey("andreytelser.ytelser.vartpenger").medValue("false"))
                .medFaktum(new Faktum().medKey("andreytelser.ytelser.dagpengereos").medValue("false"))
                .medFaktum(new Faktum().medKey("andreytelser.ytelser.annenytelse").medValue("false"))
                .medFaktum(new Faktum().medKey("andreytelser.ytelser.ingenytelse").medValue("true"))
                .medFaktum(new Faktum().medKey("skjema.sprak").medValue("nb_NO"))
                .medFaktum(new Faktum().medKey("andreytelser.ytelser").medValue("true"))
                .medFaktum(new Faktum().medKey("arbeidsforhold").medValue(null)
                        .medProperty("sagtoppavarbeidsgiver.tilbudomjobbannetsted", null)
                        .medProperty("arbeidsgivernavn", "Et firma")
                        .medProperty("eosland", "false")
                        .medProperty("datotil", "2018-04-19")
                        .medProperty("skalHaT8VedleggForKontraktUtgaatt", "false")
                        .medProperty("datofra", "2018-03-26")
                        .medProperty("rotasjonskiftturnus", "nei")
                        .medProperty("aarsak", null)
                        .medProperty("land", "NOR")
                        .medProperty("tilbudomjobbannetsted", "false")
                        .medProperty("type", "kontraktutgaatt"))
                .medVedlegg(new Vedlegg()
                        .medVedleggId(1L)
                        .medSoknadId(1234L)
                        .medSkjemaNummer("02")
                        .medSkjemanummerTillegg("kontraktutgaatt")
                        .medInnsendingsvalg(Vedlegg.Status.LastetOpp)
                        .medNavn("vedleggnavn")
                        .medStorrelse(12345L)
                        .medAntallSider(1)
                        .medFillagerReferanse("asdfs"));

        jsonSoknad = new DagpengerOrdinaerTilJson().transform(soknad);

        assertThat(jsonSoknad.getSkjemaNummer()).isEqualTo("NAV 04-01.03");
        assertThat(jsonSoknad.getAktoerId()).isEqualTo("12345");
        assertThat(jsonSoknad.getSoknadPrefix()).isEqualTo("dagpenger.ordinaer");
        assertThat(jsonSoknad.getFakta()).hasSize(41);
        assertThat(getFaktum("arbeidsforhold.arbeidstilstand").getValue()).isEqualTo(("varierendeArbeidstid"));
        assertThat(getFaktum("reellarbeidssoker.villighelse").getValue()).isEqualTo("false");
        assertThat(getFaktum("arbeidsforhold").getProperties()).hasSize(11);
        assertThat(jsonSoknad.getVedlegg()).hasSize(1);
        assertThat(jsonSoknad.getVedlegg().get(0).getSkjemaNummer()).isEqualTo("02");
        assertThat(jsonSoknad.getVedlegg().get(0).getSkjemanummerTillegg()).isEqualTo("kontraktutgaatt");
        assertThat(jsonSoknad.getVedlegg().get(0).getAntallSider()).isEqualTo(1);
    }

    private JsonDagpengerFaktum getFaktum(String key) {
        return jsonSoknad
                .getFakta()
                .stream()
                .filter(jsonDagpengerFaktum -> jsonDagpengerFaktum.getKey().equals(key))
                .findFirst()
                .orElseThrow(AssertionFailedError::new);

    }

}