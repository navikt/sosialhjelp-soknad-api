package no.nav.sbl.dialogarena.soknadinnsending.business.transformer.refusjondagligreise;

import no.nav.melding.virksomhet.paaloepteutgifter.v1.paaloepteutgifter.PaaloepteUtgifter;
import no.nav.melding.virksomhet.paaloepteutgifter.v1.paaloepteutgifter.Utgiftsperioder;
import no.nav.sbl.dialogarena.sendsoknad.domain.AlternativRepresentasjon;
import no.nav.sbl.dialogarena.sendsoknad.domain.Faktum;
import no.nav.sbl.dialogarena.sendsoknad.domain.util.ServiceUtils;
import no.nav.sbl.dialogarena.sendsoknad.domain.WebSoknad;
import no.nav.sbl.dialogarena.sendsoknad.domain.transformer.refusjondagligreise.RefusjonDagligreiseTilXml;
import org.joda.time.LocalDate;
import org.junit.Test;

import java.math.BigInteger;

import static org.assertj.core.api.Assertions.assertThat;

public class RefusjonDagligreiseTilXmlTest {


    @Test
    public void skalKonvertereTilAlternativRepresentasjon() throws Exception {
        Faktum vedtak = settOppVedtak();
        Faktum betalingsplan = new Faktum()
                .medKey("vedtak.betalingsplan")
                .medProperty("registrert", "false");
        WebSoknad soknad = new WebSoknad().medFaktum(vedtak).medFaktum(betalingsplan);

        AlternativRepresentasjon representasjon = new RefusjonDagligreiseTilXml().apply(soknad);

        assertThat(representasjon.getMimetype()).isEqualTo("application/xml");
        assertThat(representasjon.getFilnavn()).isEqualTo("RefusjonDagligreise.xml");
        assertThat(representasjon.getContent()).isNotNull();
    }

    @Test
    public void skalLeggeTilDag() throws Exception {
        Boolean trengerParkering = false;
        Faktum vedtak = settOppVedtak().medProperty("trengerParkering", trengerParkering.toString());
        Faktum betalingsplan = lagBetalingsplan("2015-01-01", "2015-01-01", trengerParkering, "1");
        WebSoknad soknad = new WebSoknad().medFaktum(vedtak).medFaktum(betalingsplan);

        PaaloepteUtgifter paaloepteUtgifter = RefusjonDagligreiseTilXml.refusjonDagligreise(soknad);

        assertThat(paaloepteUtgifter.getVedtaksId()).isEqualTo("1234");
        assertThat(paaloepteUtgifter.getUtgiftsperioder().size()).isEqualTo(1);

        Utgiftsperioder utgiftsperiode = paaloepteUtgifter.getUtgiftsperioder().get(0);
        assertThat(utgiftsperiode.getBetalingsplanId()).isEqualTo("1");
        assertThat(utgiftsperiode.getTotaltAntallDagerKjoert()).isEqualTo(BigInteger.valueOf(1));
        assertThat(utgiftsperiode.getUtgiftsdagerMedParkering().size()).isEqualTo(1);
        assertThat(utgiftsperiode.getUtgiftsdagerMedParkering().get(0).getUtgiftsdag()).isEqualTo(ServiceUtils.stringTilXmldato("2015-01-01"));
    }

    @Test
    public void skalRegneUtParkeringsutgifterOgAntallDagerKjort() throws Exception {
        Boolean trengerParkering = true;
        Faktum vedtak = settOppVedtak().medProperty("trengerParkering", trengerParkering.toString());
        Faktum betalingsplan = lagBetalingsplan("2015-01-01", "2015-01-03", trengerParkering, "1");
        betalingsplan.medProperty("2015-01-02.soker", "false");

        WebSoknad soknad = new WebSoknad().medFaktum(vedtak).medFaktum(betalingsplan);
        PaaloepteUtgifter paaloepteUtgifter = RefusjonDagligreiseTilXml.refusjonDagligreise(soknad);
        Utgiftsperioder utgiftsperiode = paaloepteUtgifter.getUtgiftsperioder().get(0);

        assertThat(utgiftsperiode.getTotaltAntallDagerKjoert()).isEqualTo(BigInteger.valueOf(2L));
        assertThat(utgiftsperiode.getTotaltParkeringsbeloep()).isEqualTo(BigInteger.valueOf(200L));
    }

    private Faktum lagBetalingsplan(String fom, String tom, Boolean trengerParkering, String id) {
        Faktum betalingsplan = new Faktum().medKey("vedtak.betalingsplan")
                .medProperty("registrert", "true")
                .medProperty("id", id)
                .medProperty("fom", fom)
                .medProperty("tom", tom);

        leggTilDager(betalingsplan, fom, tom, trengerParkering);
        return betalingsplan;
    }

    private void leggTilDager(Faktum faktum, String fom, String tom, Boolean trengerParkering) {
        LocalDate fomDato = new LocalDate(fom);
        LocalDate tomDato = new LocalDate(tom);

        for (LocalDate date = fomDato; date.isBefore(tomDato.plusDays(1)); date = date.plusDays(1)) {
            faktum.medProperty(ServiceUtils.datoTilString(date) + ".soker", "true");
            if(trengerParkering) {
                faktum.medProperty(ServiceUtils.datoTilString(date) + ".parkering", "100");
            }
        }
    }

    private Faktum settOppVedtak() {
        return new Faktum()
                .medKey("vedtak")
                .medProperty("id", "1234")
                .medProperty("trengerParkering", "false");
    }
}