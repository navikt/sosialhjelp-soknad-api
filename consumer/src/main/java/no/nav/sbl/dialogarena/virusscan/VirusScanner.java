package no.nav.sbl.dialogarena.virusscan;

public interface VirusScanner {
    boolean scan(String filnavn, byte[] data);
}
