package ch.epfl.javelo.data;

import org.junit.jupiter.api.Test;

import static ch.epfl.javelo.data.Attribute.*;
import static org.junit.jupiter.api.Assertions.*;

class AttributeSetTest {

    @Test
    void of() {
        AttributeSet set = AttributeSet.of(TRACKTYPE_GRADE1, HIGHWAY_TRACK, LCN_YES);
        assertEquals(2_305_843_009_213_825_026L, set.bits());
    }

    @Test
    void contains() {
        AttributeSet set = AttributeSet.of(TRACKTYPE_GRADE1, HIGHWAY_TRACK, LCN_YES);
        assertTrue(set.contains(LCN_YES));
    }

    @Test
    void intersects() {
        AttributeSet set = AttributeSet.of(TRACKTYPE_GRADE1, HIGHWAY_TRACK, LCN_YES);
        AttributeSet otherSet = AttributeSet.of(LCN_YES);
        assertTrue(set.intersects(otherSet));
    }

    @Test
    void testToString() {
        AttributeSet set = AttributeSet.of(TRACKTYPE_GRADE1, HIGHWAY_TRACK, LCN_YES);
        assertEquals("{highway=track,tracktype=grade1,lcn=yes}", set.toString());
    }

}