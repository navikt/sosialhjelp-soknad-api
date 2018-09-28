package no.nav.sbl.sosialhjelp.domain;

import java.time.LocalDateTime;

public class SoknadUnderArbeid {
    private Long soknadId;
    private int versjon;
    private String behandlingsId;
    private String tilknyttetBehandlingsId;
    private String eier;
    private byte[] data;
    private String orgnummer;
    private String status;
    private LocalDateTime opprettetDato;
    private LocalDateTime sistEndretDato;
}
