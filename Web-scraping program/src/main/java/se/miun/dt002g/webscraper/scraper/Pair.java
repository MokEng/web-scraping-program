package se.miun.dt002g.webscraper.scraper;

import java.io.Serializable;

/**
 * Serializable class for Pairing two different objects.
 * @param <T>
 * @param <S>
 */
public final class Pair<T, S> implements Serializable {
    public final T first;
    public final S second;

    Pair(T first, S second) {
        this.first = first;
        this.second = second;
    }

    public static <T, S> Pair<T, S> of(T first, S second) {
        return new Pair(first, second);
    }

    public T getFirst() {
        return this.first;
    }

    public S getSecond() {
        return this.second;
    }

    public Pair<S, T> swap() {
        return of(this.second, this.first);
    }

    public String toString() {
        return "(" + this.first + ", " + this.second + ")";
    }

    public boolean equals(Object o) {
        if (o == null) {
            return false;
        } else if (this == o) {
            return true;
        } else if (this.getClass() != o.getClass()) {
            return false;
        } else {
            Pair<?, ?> that = (Pair)o;
            return this.firstEqual(that) && this.secondEqual(that);
        }
    }

    public int hashCode() {
        return this.first.hashCode() ^ this.second.hashCode();
    }

    private boolean firstEqual(Pair<?, ?> that) {
        return this.first == null && that.first == null || this.first != null && this.first.equals(that.first);
    }

    private boolean secondEqual(Pair<?, ?> that) {
        return this.second == null && that.second == null || this.second != null && this.second.equals(that.second);
    }
}