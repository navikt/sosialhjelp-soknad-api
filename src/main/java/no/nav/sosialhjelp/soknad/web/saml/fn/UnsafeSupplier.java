package no.nav.sosialhjelp.soknad.web.saml.fn;

import lombok.SneakyThrows;

import java.util.function.Supplier;

@FunctionalInterface
public interface UnsafeSupplier<T> extends Supplier<T> {

    static UnsafeSupplier<Void> toVoid(UnsafeRunnable unsafeRunnable) {
        return () -> {
            unsafeRunnable.run();
            return null;
        };
    }

    @Override
    @SneakyThrows
    default T get() {
        return unsafeGet();
    }

    T unsafeGet() throws Throwable;
}
