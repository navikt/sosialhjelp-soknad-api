package no.nav.sbl.dialogarena.websoknad.servlet;

import static java.lang.String.format;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

import no.nav.modig.core.exception.ApplicationException;

public class NokkelHenter {
        public static List<String> hentNokler(String side) {
        List<String> nokler = new ArrayList<>();

        String filnavn = format("/templatekeys/%s.txt", side);
        try(InputStream ressursStrom = NokkelHenter.class.getResourceAsStream(filnavn);
        		Scanner scanner = new Scanner(ressursStrom);) {
            while (scanner.hasNextLine()) {
                nokler.add(scanner.nextLine());
            }
        } 
        catch (NullPointerException | IOException e) {
			throw new ApplicationException("Fant ikke fil",e);
        }
        return nokler;
    }
}
