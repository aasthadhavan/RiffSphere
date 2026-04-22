package com.riffsphere.models;

/**
 * Immutable Value Object wrapping a 0-5 star rating.
 * Prevents illegal doubles from polluting the domain.
 * Demonstrates: Encapsulation, Immutability, Value Object pattern.
 */
public final class Rating implements Comparable<Rating> {

    public static final Rating NONE = new Rating(0.0);
    public static final Rating MIN  = new Rating(1.0);
    public static final Rating MAX  = new Rating(5.0);

    private final double value;

    public Rating(double value) {
        if (value < 0.0 || value > 5.0)
            throw new IllegalArgumentException("Rating must be 0-5, got: " + value);
        this.value = Math.round(value * 10.0) / 10.0; // 1 decimal place
    }

    public static Rating of(double v) { return new Rating(v); }

    public double getValue()                   { return value; }
    public boolean isAbove(Rating other)       { return this.value > other.value; }
    public boolean isBelow(Rating other)       { return this.value < other.value; }
    public String  toStarString()              { return String.format("%.1f ★", value); }

    @Override public int compareTo(Rating o)   { return Double.compare(this.value, o.value); }
    @Override public String toString()         { return String.valueOf(value); }
    @Override public boolean equals(Object o)  {
        if (!(o instanceof Rating)) return false;
        return Double.compare(value, ((Rating) o).value) == 0;
    }
    @Override public int hashCode()            { return Double.hashCode(value); }
}
