package no.nav.sbl.dialogarena.soknadinnsending.consumer.modigutils;//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

import java.io.Serializable;
import org.apache.commons.collections15.Factory;

public class NotFactory implements Factory<Boolean>, Serializable {
    private final Factory<Boolean> factory;

    public NotFactory(Factory<Boolean> factory) {
        this.factory = factory;
    }

    public Boolean create() {
        return !(Boolean)this.factory.create();
    }
}
