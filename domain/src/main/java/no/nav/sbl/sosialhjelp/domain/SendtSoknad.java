package no.nav.sbl.sosialhjelp.domain;

import java.time.LocalDateTime;

public class SendtSoknad {
    private Long sendtSoknadId;
    private String behandlingsId;
    private String tilknyttetBehandlingsId;
    private String eier;
    private String fiksforsendelseId;
    private LocalDateTime brukerOpprettetDato;
    private LocalDateTime brukerFerdigDato;
    private LocalDateTime sendtDato;
}
