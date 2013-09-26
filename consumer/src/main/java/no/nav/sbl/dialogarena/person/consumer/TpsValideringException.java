package no.nav.sbl.dialogarena.person.consumer;

public class TpsValideringException extends RuntimeException {
	
	public final String messagekey;

	public TpsValideringException(TpsValideringsfeil feil, Throwable cause) {
		super("Valideringsfeil fra TPS: " + feil.name(), cause);
		this.messagekey = feil.feilmeldingMsgKey;
	}

}
