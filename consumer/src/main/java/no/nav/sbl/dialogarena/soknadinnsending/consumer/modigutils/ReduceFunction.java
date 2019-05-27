package no.nav.sbl.dialogarena.soknadinnsending.consumer.modigutils;

public interface ReduceFunction<T, U> {
    U reduce(U accumulator, T newValue);
    U identity();
}
