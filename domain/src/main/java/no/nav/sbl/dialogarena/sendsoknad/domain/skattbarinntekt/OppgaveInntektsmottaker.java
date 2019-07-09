package no.nav.sbl.dialogarena.sendsoknad.domain.skattbarinntekt;

import java.util.ArrayList;
import java.util.List;

public class OppgaveInntektsmottaker {
    public String kalendermaaned;
    public String virksomhetId;
    public List<Inntekt> inntekt = new ArrayList<>();
    public List<Forskuddstrekk> forskuddstrekk = new ArrayList<>();
}