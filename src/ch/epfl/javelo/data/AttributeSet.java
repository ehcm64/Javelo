package ch.epfl.javelo.data;

import ch.epfl.javelo.Bits;
import ch.epfl.javelo.Preconditions;

import java.util.StringJoiner;

public record AttributeSet(long bits) {

    public AttributeSet {
        Preconditions.checkArgument(bits < Math.scalb(2, Attribute.COUNT));
    }

    public static AttributeSet of(Attribute... attributes) {
        long bits = 0;
        for (Attribute attribute : attributes) {
            long mask = 1L << attribute.ordinal();
            bits = bits | mask;
        }
        return new AttributeSet(bits);
    }

    public boolean contains(Attribute attribute) {
        long mask = 1L << attribute.ordinal();
        if ((this.bits & mask) != 0) {
            return true;
        } else return false;
    }

    public boolean intersects(AttributeSet that) {
        for (int i = 0; i < Attribute.COUNT; i++) {
            long thisIBit = (this.bits & (1L << i));
            long thatIBit = (that.bits & (1L << i));
            if ((thisIBit & thatIBit) != 0) return true;
        }
        return false;
    }

    @Override
    public String toString() {
        StringJoiner joiner = new StringJoiner(",", "{", "}");
        for (int i = 0; i < Attribute.COUNT; i++) {
            long mask = 1L << i;
            if ((this.bits & mask) != 0) joiner.add(Attribute.ALL.get(i).keyValue().toString());
        }
        return joiner.toString();
    }
}
