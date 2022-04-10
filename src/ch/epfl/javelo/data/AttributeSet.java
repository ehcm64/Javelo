package ch.epfl.javelo.data;

import ch.epfl.javelo.Preconditions;

import java.util.StringJoiner;

/**
 * Represents a set of OpenStreetMap attributes
 * @author Edouard Mignan (345875)
 */
public record AttributeSet(long bits) {

    public AttributeSet {
        boolean notNegative = (bits & (1L << Long.SIZE - 1)) == 0;
        boolean noIllegalAttribute = bits < (1L << (Attribute.COUNT));
        Preconditions.checkArgument(noIllegalAttribute && notNegative);
    }

    /**
     *  returns a set containing only the attributes given as argument
      * @param attributes attribute given as argument
     * @return a set containing only the attributes given as argument
     */
    public static AttributeSet of(Attribute... attributes) {
        long bits = 0;
        for (Attribute attribute : attributes) {
            long mask = 1L << attribute.ordinal();
            bits = bits | mask;
        }
        return new AttributeSet(bits);
    }

    /**
     * returns true iff the receiver set (this) contains the given attribute
     * @param attribute the given attribute
     * @return true iff the receiver set (this) contains the given attribute
     */
    public boolean contains(Attribute attribute) {
        long mask = 1L << attribute.ordinal();
        return (this.bits & mask) != 0;
    }

    /**
     * returns true iff the intersection of the receiver set (this) with the one passed as argument (that) is not empty
     * @param that argument passed as argument
     * @return true iff the intersection of the receiver set (this) with the one passed as argument (that) is not empty
     */
    public boolean intersects(AttributeSet that) {
        return (this.bits & that.bits) != 0;
    }

    @Override
    public String toString() {
        StringJoiner joiner = new StringJoiner(",", "{", "}");
        for (int i = 0; i < Attribute.COUNT; i++) {
            long mask = 1L << i;
            if ((this.bits & mask) != 0) joiner.add(Attribute.ALL.get(i).keyValue());
        }
        return joiner.toString();
    }
}
