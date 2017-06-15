package no.nav.sbl.dialogarena.sendsoknad.domain.transformer.refusjondagligreise;

import no.nav.melding.virksomhet.paaloepteutgifter.v1.paaloepteutgifter.PaaloepteUtgifter;
import no.nav.melding.virksomhet.paaloepteutgifter.v1.paaloepteutgifter.Utgiftsdager;
import no.nav.melding.virksomhet.paaloepteutgifter.v1.paaloepteutgifter.Utgiftsperioder;
import no.nav.sbl.dialogarena.sendsoknad.domain.AlternativRepresentasjon;
import no.nav.sbl.dialogarena.sendsoknad.domain.Faktum;
import no.nav.sbl.dialogarena.sendsoknad.domain.transformer.AlternativRepresentasjonTransformer;
import no.nav.sbl.dialogarena.sendsoknad.domain.transformer.AlternativRepresentasjonType;
import no.nav.sbl.dialogarena.sendsoknad.domain.util.ServiceUtils;
import no.nav.sbl.dialogarena.sendsoknad.domain.WebSoknad;
import org.apache.commons.collections15.Transformer;
import org.joda.time.LocalDate;

import javax.xml.bind.JAXB;
import java.io.ByteArrayOutputStream;
import java.math.BigInteger;
import java.util.UUID;
import java.util.function.Function;

import static java.util.stream.Collectors.toList;
import static no.nav.sbl.dialogarena.sendsoknad.domain.FaktumPredicates.harPropertyMedValue;

public class RefusjonDagligreiseTilXml implements AlternativRepresentasjonTransformer {
    @Override
    public AlternativRepresentasjonType getRepresentasjonsType() {
        return AlternativRepresentasjonType.XML;
    }

    public static PaaloepteUtgifter refusjonDagligreise(WebSoknad webSoknad) {
        PaaloepteUtgifter skjema = new PaaloepteUtgifter();
        Faktum vedtak = webSoknad.getFaktumMedKey("vedtak");
        FaktumTilUtgiftsperiode faktumTransformer = new FaktumTilUtgiftsperiode("true".equals(vedtak.getProperties().get("trengerParkering")));

        skjema.setVedtaksId(vedtak.getProperties().get("id"));
        skjema.getUtgiftsperioder().addAll(
                webSoknad.getFaktaMedKey("vedtak.betalingsplan").stream()
                        .filter(harPropertyMedValue("registrert", "true"))
                        .map(faktum -> faktumTransformer.transform(faktum))
                        .collect(toList()));

        return skjema;
    }

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

    @Override
    public AlternativRepresentasjon apply(WebSoknad webSoknad) {
        return transform(webSoknad);
    }

    private static final class FaktumTilUtgiftsperiode implements Transformer<Faktum, Utgiftsperioder> {
        private Boolean trengerParkering;

        public FaktumTilUtgiftsperiode(Boolean trengerParkering) {
            this.trengerParkering = trengerParkering;
        }

        private static final Function<Faktum, Utgiftsperioder> tilUtgiftsperiode(boolean trengerParkering) {
            return faktum -> {
                Utgiftsperioder utgiftsperioder = new Utgiftsperioder();
                utgiftsperioder.setBetalingsplanId(faktum.getProperties().get("id"));

                int totaltParkeringbelop = 0;
                int totaltAntallDager = 0;

                LocalDate fom = new LocalDate(faktum.getProperties().get("fom"));
                LocalDate tom = new LocalDate(faktum.getProperties().get("tom"));

                for (LocalDate date = fom; date.isBefore(tom.plusDays(1)); date = date.plusDays(1)) {
                    String datoString = ServiceUtils.datoTilString(date);
                    boolean sokerForDag = "true".equals(faktum.getProperties().get(datoString + ".soker"));
                    if (sokerForDag) {
                        totaltAntallDager++;
                        String parkeringsUtgift = faktum.getProperties().get(datoString + ".parkering");
                        Utgiftsdager utgiftsdag = new Utgiftsdager();
                        utgiftsdag.setUtgiftsdag(ServiceUtils.stringTilXmldato(datoString));

                        if (trengerParkering && parkeringsUtgift != null) {
                            int utgift = Integer.parseInt(parkeringsUtgift);
                            totaltParkeringbelop += utgift;
                            utgiftsdag.setParkeringsutgift(BigInteger.valueOf(utgift));
                        }
                        utgiftsperioder.getUtgiftsdagerMedParkering().add(utgiftsdag);
                    }
                }

                utgiftsperioder.setTotaltParkeringsbeloep(BigInteger.valueOf(totaltParkeringbelop));
                utgiftsperioder.setTotaltAntallDagerKjoert(BigInteger.valueOf(totaltAntallDager));
                return utgiftsperioder;
            };
        }

        @Override
        public Utgiftsperioder transform(Faktum faktum) {
            Utgiftsperioder utgiftsperioder = new Utgiftsperioder();
            utgiftsperioder.setBetalingsplanId(faktum.getProperties().get("id"));

            int totaltParkeringbelop = 0;
            int totaltAntallDager = 0;

            LocalDate fom = new LocalDate(faktum.getProperties().get("fom"));
            LocalDate tom = new LocalDate(faktum.getProperties().get("tom"));

            for (LocalDate date = fom; date.isBefore(tom.plusDays(1)); date = date.plusDays(1)) {
                String datoString = ServiceUtils.datoTilString(date);
                if (sokerForDag(datoString, faktum)) {
                    totaltAntallDager++;
                    String parkeringsUtgift = faktum.getProperties().get(datoString + ".parkering");
                    Utgiftsdager utgiftsdag = new Utgiftsdager();
                    utgiftsdag.setUtgiftsdag(ServiceUtils.stringTilXmldato(datoString));

                    if (trengerParkering && parkeringsUtgift != null) {
                        int utgift = Integer.parseInt(parkeringsUtgift);
                        totaltParkeringbelop += utgift;
                        utgiftsdag.setParkeringsutgift(BigInteger.valueOf(utgift));
                    }
                    utgiftsperioder.getUtgiftsdagerMedParkering().add(utgiftsdag);
                }
            }

            utgiftsperioder.setTotaltParkeringsbeloep(BigInteger.valueOf(totaltParkeringbelop));
            utgiftsperioder.setTotaltAntallDagerKjoert(BigInteger.valueOf(totaltAntallDager));
            return utgiftsperioder;
        }

        private boolean sokerForDag(String dato, Faktum betalingsplan) {
            return "true".equals(betalingsplan.getProperties().get(dato + ".soker"));
        }
    }
}
