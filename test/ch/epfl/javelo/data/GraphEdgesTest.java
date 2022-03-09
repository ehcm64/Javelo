package ch.epfl.javelo.data;

import org.junit.jupiter.api.Test;

import java.nio.ByteBuffer;
import java.nio.IntBuffer;
import java.nio.ShortBuffer;

import static org.junit.jupiter.api.Assertions.*;

class GraphEdgesTest {

    @Test
    void GraphEdgesWorks() {
        ByteBuffer edgesBuffer = ByteBuffer.allocate(10);
// Sens : inversé. Nœud destination : 12.
        edgesBuffer.putInt(0, ~12);
// Longueur : 0x10.b m (= 16.6875 m)
        edgesBuffer.putShort(4, (short) 0x10_b);
// Dénivelé : 0x10.0 m (= 16.0 m)
        edgesBuffer.putShort(6, (short) 0x10_0);
// Identité de l'ensemble d'attributs OSM : 2022
        edgesBuffer.putShort(8, (short) 2022);

        IntBuffer profileIds = IntBuffer.wrap(new int[]{
                // Type : 3. Index du premier échantillon : 1.
                (3 << 30) | 1
        });

        ShortBuffer elevations = ShortBuffer.wrap(new short[]{
                (short) 0,
                (short) 0x180C, (short) 0xFEFF,
                (short) 0xFFFE, (short) 0xF000
        });

        GraphEdges edges3 =
                new GraphEdges(edgesBuffer, profileIds, elevations);

        assertTrue(edges3.isInverted(0));
        assertEquals(12, edges3.targetNodeId(0));
        assertEquals(16.6875, edges3.length(0));
        assertEquals(16.0, edges3.elevationGain(0));
        assertEquals(2022, edges3.attributesIndex(0));
        float[] case3ExpectedSamples = new float[]{
                384.0625f, 384.125f, 384.25f, 384.3125f, 384.375f,
                384.4375f, 384.5f, 384.5625f, 384.6875f, 384.75f
        };

        for (float sample : edges3.profileSamples(0)) {
            System.out.println(sample);
        }
        //assertArrayEquals(case2ExpectedSamples, edges2.profileSamples());
        assertArrayEquals(case3ExpectedSamples, edges3.profileSamples(0));
    }
}