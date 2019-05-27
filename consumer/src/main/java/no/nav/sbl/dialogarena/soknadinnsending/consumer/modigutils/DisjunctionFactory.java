package no.nav.sbl.dialogarena.soknadinnsending.consumer.modigutils;//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

import java.io.Serializable;
import org.apache.commons.collections15.Factory;

public class DisjunctionFactory implements Factory<Boolean>, Serializable {
    private final Factory<Boolean> factory1;
    private final Factory<Boolean> factory2;

    public DisjunctionFactory(Factory<Boolean> factory1, Factory<Boolean> factory2) {
        this.factory1 = factory1;
        this.factory2 = factory2;
    }

    public Boolean create() {
        return (Boolean)this.factory1.create() || (Boolean)this.factory2.create();
    }

    public static class Either {
        private final Factory<Boolean> firstFactory;

        public Either(Factory<Boolean> firstFactory) {
            this.firstFactory = firstFactory;
        }

        public DisjunctionFactory or(Factory<Boolean> secondFactory) {
            return new DisjunctionFactory(this.firstFactory, secondFactory);
        }
    }
}
