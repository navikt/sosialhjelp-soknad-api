package no.nav.sosialhjelp.soknad.domain.model.skattbarinntekt;

import java.util.ArrayList;
import java.util.List;

public class OppgaveInntektsmottaker {
    public String kalendermaaned;
    public String opplysningspliktigId;
    public List<Inntekt> inntekt = new ArrayList<>();
    public List<Forskuddstrekk> forskuddstrekk = new ArrayList<>();
}