package no.nav.sbl.dialogarena.sendsoknad.domain.util;

import org.bouncycastle.jcajce.provider.digest.SHA512;
import org.bouncycastle.util.encoders.Hex;
import org.joda.time.DateTime;

import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.XMLGregorianCalendar;


public class ServiceUtils {
    public final static String FASIT_ENVIRONMENT_NAME = "FASIT_ENVIRONMENT_NAME";
    public final static String IS_SCHEDULED_TASKS_DISABLED = "scheduler.disable";

    public static XMLGregorianCalendar stringTilXmldato(String dato) {
        return lagDatatypeFactory().newXMLGregorianCalendar(DateTime.parse(dato).toGregorianCalendar());
    }

    public static DatatypeFactory lagDatatypeFactory() {
        try {
            return DatatypeFactory.newInstance();
        } catch (DatatypeConfigurationException e) {
            throw new RuntimeException(e);
        }
    }

    public static String getSha512FromByteArray(byte[] bytes) {

        if (bytes == null) {
            return "";
        }

        SHA512.Digest sha512 = new SHA512.Digest();
        sha512.update(bytes);

        return Hex.toHexString(sha512.digest());
    }

    public static boolean isRunningInProd(){
        return "p".equals(System.getenv(FASIT_ENVIRONMENT_NAME));
    }

    public static boolean isScheduledTasksDisabled(){
        return Boolean.valueOf(System.getProperty(IS_SCHEDULED_TASKS_DISABLED, "false"));
    }
}
