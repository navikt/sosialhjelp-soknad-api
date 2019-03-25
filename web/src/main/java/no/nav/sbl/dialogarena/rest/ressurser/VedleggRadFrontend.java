package no.nav.sbl.dialogarena.rest.ressurser;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;

@JsonDeserialize(using = CustomVedleggRadDeserializer.class)
public interface VedleggRadFrontend { }