package no.nav.sbl.dialogarena.soknadinnsending.consumer.modigutils;//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

import org.apache.commons.collections15.Factory;
import org.apache.commons.collections15.Transformer;

public class TransforOutputFromFactory<F, T> implements Factory<T> {
    private Factory<F> factory;
    private Transformer<? super F, T> transformer;

    public TransforOutputFromFactory(Factory<F> factory, Transformer<? super F, T> transformer) {
        this.factory = factory;
        this.transformer = transformer;
    }

    public T create() {
        return this.transformer.transform(this.factory.create());
    }
}
