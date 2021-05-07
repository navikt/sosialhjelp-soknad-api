package no.nav.sosialhjelp.soknad.consumer.pdl.adressesok;

import org.junit.Test;

import static no.nav.sosialhjelp.soknad.consumer.pdl.adressesok.AdresseHelper.formatterKommunenavn;
import static org.assertj.core.api.Assertions.assertThat;

public class AdresseHelperTest {

    @Test
    public void skalFormatterKommunenavn() {
        assertThat(formatterKommunenavn(null)).isNull();
        assertThat(formatterKommunenavn("")).isEmpty();
        assertThat(formatterKommunenavn("OSLO")).isEqualTo("Oslo");
        assertThat(formatterKommunenavn("INDRE ØSTFOLD")).isEqualTo("Indre Østfold");
        assertThat(formatterKommunenavn("AURSKOG-HØLAND")).isEqualTo("Aurskog-Høland");
        assertThat(formatterKommunenavn("NORE OG UVDAL")).isEqualTo("Nore og Uvdal");
    }
}