package no.nav.sbl.dialogarena.service.helpers.refusjondagligreise;

import com.github.jknack.handlebars.Options;
import no.nav.sbl.dialogarena.service.helpers.RegistryAwareHelper;
import org.joda.time.Days;
import org.joda.time.LocalDate;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.*;

import static no.nav.sbl.dialogarena.service.HandlebarsUtils.lagItererbarRespons;


@Component
public class LagKjorelisteUker extends RegistryAwareHelper<Map<String, String>> {


    @Override
    public String getNavn() {
        return "lagKjorelisteUker";
    }

    @Override
    public String getBeskrivelse() {
        return "Bygger en nestet liste over uker for et betalingsvedtak, der ukene inneholder dager det er søkt for refusjon.";
    }

    @Override
    public CharSequence apply(Map<String, String> properties, Options options) throws IOException {
        LocalDate fom = new LocalDate(properties.get("fom"));
        LocalDate tom = new LocalDate(properties.get("tom"));

        Collection<KjorelisteUke> ukerMedDager = lagKjorelisteUker(properties, fom, tom);
        return lagItererbarRespons(options, ukerMedDager);
    }

    private Collection<KjorelisteUke> lagKjorelisteUker(Map<String, String> properties, LocalDate fom, LocalDate tom) {
        List<LocalDate> sokteDatoer = finnSokteDatoer(properties, fom, tom);

        Map<Integer, KjorelisteUke> uker = new LinkedHashMap<>();

        for (LocalDate dato : sokteDatoer) {
            int ukeNr = dato.getWeekOfWeekyear();

            KjorelisteUke uke = uker.get(ukeNr);
            if (uke == null) {
                uke = new KjorelisteUke(ukeNr);
                uker.put(ukeNr, uke);
            }

            KjorelisteDag dag = new KjorelisteDag(dato.toString());
            leggTilParkering(dag, properties);
            uke.dager.add(dag);
        }

        return uker.values();
    }

    private List<LocalDate> finnSokteDatoer(Map<String, String> properties, LocalDate fom, LocalDate tom) {
        int totaltAntallDager = 1 + Days.daysBetween(fom, tom).getDays();

        List<LocalDate> datoer = new ArrayList<>();

        LocalDate dato = fom;
        for (int i = 0; i < totaltAntallDager; i++) {
            if (sokerForDato(dato, properties)) {
                datoer.add(dato);
            }
            dato = dato.plusDays(1);
        }
        return datoer;
    }

    private boolean sokerForDato(LocalDate dato, Map<String, String> properties) {
        return "true".equals(properties.get(dato.toString() + ".soker"));
    }

    private void leggTilParkering(KjorelisteDag dag, Map<String, String> properties) {
        String parkering = properties.get(dag.dato + ".parkering");
        if (parkering == null) {
            dag.parkering = "0";
        } else {
            dag.parkering = parkering;
        }
    }


    private static class KjorelisteUke {
        private int ukeNr;
        private List<KjorelisteDag> dager = new ArrayList<>();

        public KjorelisteUke(int ukeNr) {
            this.ukeNr = ukeNr;
        }

        public int getUkeNr() {
            return ukeNr;
        }

        public List<KjorelisteDag> getDager() {
            return dager;
        }
    }

    private static class KjorelisteDag {
        private String dato;
        private String parkering;

        public KjorelisteDag(String dato) {
            this.dato = dato;
        }

        public String getDato() {
            return dato;
        }

        public String getParkering() {
            return parkering;
        }
    }

}
