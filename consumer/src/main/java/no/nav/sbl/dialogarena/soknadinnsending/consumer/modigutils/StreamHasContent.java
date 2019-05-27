package no.nav.sbl.dialogarena.soknadinnsending.consumer.modigutils;//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;
import org.apache.commons.collections15.Factory;

public class StreamHasContent implements Factory<Boolean>, Serializable {
    private InputStream stream;

    public StreamHasContent(InputStream stream) {
        this.stream = stream;
    }

    public Boolean create() {
        try {
            return this.stream.available() >= 1;
        } catch (IOException var2) {
            return false;
        }
    }
}
