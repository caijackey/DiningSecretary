package com.fg114.main.util;

/**
 * 两个对象的配对，同系统的Pair，为兼容低版本系统
 * @author wufucheng
 *
 * @param <F>
 * @param <S>
 */
public class MyPair<F, S> {
    public final F first;
    public final S second;

    /**
     * Constructor for a Pair. If either are null then equals() and hashCode() will throw
     * a NullPointerException.
     * @param first the first object in the Pair
     * @param second the second object in the pair
     */
    public MyPair(F first, S second) {
        this.first = first;
        this.second = second;
    }

    /**
     * Checks the two objects for equality by delegating to their respective equals() methods.
     * @param o the Pair to which this one is to be checked for equality
     * @return true if the underlying objects of the Pair are both considered equals()
     */
    @SuppressWarnings("unchecked")
	public boolean equals(Object o) {
        if (o == this) return true;
        if (!(o instanceof MyPair)) return false;
        final MyPair<F, S> other;
        try {
            other = (MyPair<F, S>) o;
        } catch (ClassCastException e) {
            return false;
        }
        return first.equals(other.first) && second.equals(other.second);
    }

    /**
     * Compute a hash code using the hash codes of the underlying objects
     * @return a hashcode of the Pair
     */
    public int hashCode() {
        int result = 17;
        result = 31 * result + first.hashCode();
        result = 31 * result + second.hashCode();
        return result;
    }

    /**
     * Convenience method for creating an appropriately typed pair.
     * @param a the first object in the Pair
     * @param b the second object in the pair
     * @return a Pair that is templatized with the types of a and b
     */
    public static <A, B> MyPair <A, B> create(A a, B b) {
        return new MyPair<A, B>(a, b);
    }
}
