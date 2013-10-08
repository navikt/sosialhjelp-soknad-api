package no.nav.sbl.dialogarena.websoknad.servlet;

import static java.lang.String.format;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class NokkelHenter {
    public static List<String> hentNokler(String side) {
        List<String> nokler = new ArrayList<>();

        String filnavn = format("/templatekeys/%s.txt", side);
        InputStream ressursStrom = NokkelHenter.class.getResourceAsStream(filnavn);
        Scanner scanner = null;
        try {
            scanner = new Scanner(ressursStrom);
            while (scanner.hasNextLine()) {
                nokler.add(scanner.nextLine());
            }
        } finally {
            if (scanner != null) {
                scanner.close();
            }
        }
        return nokler;
    }
}