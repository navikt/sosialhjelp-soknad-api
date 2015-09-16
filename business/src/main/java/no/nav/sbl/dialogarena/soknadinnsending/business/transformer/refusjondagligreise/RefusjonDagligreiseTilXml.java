package no.nav.sbl.dialogarena.soknadinnsending.business.transformer.refusjondagligreise;

import meldingsmodell.no.nav.melding.virksomhet.paaloepteutgifter.v1.paaloepteutgifter.PaaloepteUtgifter;
import meldingsmodell.no.nav.melding.virksomhet.paaloepteutgifter.v1.paaloepteutgifter.Utgiftsdager;
import meldingsmodell.no.nav.melding.virksomhet.paaloepteutgifter.v1.paaloepteutgifter.Utgiftsperioder;
import no.nav.sbl.dialogarena.soknadinnsending.business.domain.AlternativRepresentasjon;
import no.nav.sbl.dialogarena.soknadinnsending.business.domain.Faktum;
import no.nav.sbl.dialogarena.soknadinnsending.business.domain.WebSoknad;
import org.apache.commons.collections15.Transformer;
import org.joda.time.LocalDate;

import javax.xml.bind.JAXB;
import java.io.ByteArrayOutputStream;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static no.nav.modig.lang.collections.IterUtils.on;
import static no.nav.sbl.dialogarena.soknadinnsending.business.domain.FaktumPredicates.harPropertyMedValue;
import static no.nav.sbl.dialogarena.soknadinnsending.business.service.ServiceUtils.datoTilString;
import static no.nav.sbl.dialogarena.soknadinnsending.business.service.ServiceUtils.stringTilXmldato;

public class RefusjonDagligreiseTilXml implements Transformer<WebSoknad, AlternativRepresentasjon>
{
    private static final class FAKTUM_TIL_UTGIFTSPERIODER implements Transformer<Faktum, Utgiftsperioder> {
        private Boolean trengerParkering;

        public FAKTUM_TIL_UTGIFTSPERIODER(Boolean trengerParkering) {
            this.trengerParkering = trengerParkering;
        }

        @Override
        public Utgiftsperioder transform(Faktum faktum) {
            Utgiftsperioder utgiftsperioder = new Utgiftsperioder();
            utgiftsperioder.setBetalingsplanId(faktum.getProperties().get("id"));

            int totaltParkeringbeløp = 0;
            int totaltAntallDager = 0;

            LocalDate fom = new LocalDate(faktum.getProperties().get("fom"));
            LocalDate tom = new LocalDate(faktum.getProperties().get("tom"));

            List<Utgiftsdager> utgiftsdager = new ArrayList<>();

            for(LocalDate date = fom; date.isBefore(tom.plusDays(1)) ; date = date.plusDays(1)) {
                String datoString = datoTilString(date);
                if(sokerForDag(datoString, faktum)) {
                    totaltAntallDager++;
                    String parkeringsUtgift = faktum.getProperties().get(datoString + ".parkering");
                    Utgiftsdager utgiftsdag = new Utgiftsdager();
                    utgiftsdag.setUtgiftsdag(stringTilXmldato(datoString));

                    if(trengerParkering && parkeringsUtgift != null) {
                        int utgift = Integer.parseInt(parkeringsUtgift);
                        totaltParkeringbeløp += utgift;
                        utgiftsdag.setParkeringsutgift(BigInteger.valueOf(utgift));
                    }
                    utgiftsdager.add(utgiftsdag);
                }
            }

            utgiftsperioder.setTotaltParkeringsbeloep(BigInteger.valueOf(totaltParkeringbeløp));
            utgiftsperioder.setTotaltAntallDagerKjoert(BigInteger.valueOf(totaltAntallDager));
            utgiftsperioder.getUtgiftsdagerMedParkering().addAll(utgiftsdager);
            return utgiftsperioder;
        }

        private boolean sokerForDag(String dato, Faktum betalingsplan) {
            return "true".equals(betalingsplan.getProperties().get(dato + ".soker"));
        }
    }
    public static PaaloepteUtgifter refusjonDagligreise(WebSoknad webSoknad) {
        PaaloepteUtgifter skjema = new PaaloepteUtgifter();
        Faktum vedtak = webSoknad.getFaktumMedKey("vedtak");
        FAKTUM_TIL_UTGIFTSPERIODER faktumTransformer = new FAKTUM_TIL_UTGIFTSPERIODER("true".equals(vedtak.getProperties().get("trengerParkering")));

        skjema.setVedtaksId(vedtak.getProperties().get("id"));
        skjema.getUtgiftsperioder().addAll(on(webSoknad.getFaktaMedKey("vedtak.betalingsplan"))
                .filter(harPropertyMedValue("registrert", "true"))
                .map(faktumTransformer).collect());

        return skjema;
    }

    @Override
    public AlternativRepresentasjon transform(WebSoknad webSoknad) {
        PaaloepteUtgifter refusjonDagligreise = refusjonDagligreise(webSoknad);
        ByteArrayOutputStream xml = new ByteArrayOutputStream();
        JAXB.marshal(refusjonDagligreise, xml);
        return new AlternativRepresentasjon()
                .medMimetype("application/xml")
                .medFilnavn("RefusjonDagligreise.xml")
                .medUuid(UUID.randomUUID().toString())
                .medContent(xml.toByteArray());
    }
}
