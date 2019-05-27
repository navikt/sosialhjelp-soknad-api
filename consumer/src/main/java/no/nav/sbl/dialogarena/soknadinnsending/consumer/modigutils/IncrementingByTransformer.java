package no.nav.sbl.dialogarena.soknadinnsending.consumer.modigutils;

import org.apache.commons.collections15.Transformer;

import java.io.Serializable;


abstract class ComparatorDecorator<T> implements EnhancedComparator<T>, Serializable {

    class Decrementing extends ComparatorDecorator<T> {
        @Override
        public int compare(T o1, T o2) {
            return -1 * ComparatorDecorator.this.compare(o1, o2);
        }
    }

    class TreatNullAsLess extends ComparatorDecorator<T> {
        @Override
        public int compare(T o1, T o2) {
            if (o1 == null && o2 == null) {
                return 0;
            } else if (o1 == null) {
                return -1;
            } else if (o2 == null) {
                return 1;
            } else {
                return ComparatorDecorator.this.compare(o1, o2);
            }
        }
    }

    class TreatNullAsMore extends ComparatorDecorator<T> {
        @Override
        public int compare(T o1, T o2) {
            if (o1 == null && o2 == null) {
                return 0;
            } else if (o1 == null) {
                return 1;
            } else if (o2 == null) {
                return -1;
            } else {
                return ComparatorDecorator.this.compare(o1, o2);
            }
        }
    }

    @Override
    public EnhancedComparator<T> descending() {
        return new Decrementing();
    }

    @Override
    public EnhancedComparator<T> nullComesFirst() {
        return new TreatNullAsLess();
    }

    @Override
    public EnhancedComparator<T> nullComesLast() {
        return new TreatNullAsMore();
    }

}

public class IncrementingByTransformer<T, C extends Comparable<? super C>> extends ComparatorDecorator<T> {

    private final Transformer<T, C> yieldsComparable;

    public IncrementingByTransformer(Transformer<T, C> yieldsComparable) {
        this.yieldsComparable = yieldsComparable;
    }

    @Override
    public int compare(T o1, T o2) {
        return yieldsComparable.transform(o1).compareTo(yieldsComparable.transform(o2));
    }

}