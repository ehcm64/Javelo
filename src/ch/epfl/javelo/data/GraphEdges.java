package ch.epfl.javelo.data;

import java.nio.ByteBuffer;
import java.nio.IntBuffer;
import java.nio.ShortBuffer;

public record GraphEdges(ByteBuffer edgesBuffer, IntBuffer profileIds, ShortBuffer elevations) {

    public boolean isInverted(int edgeId) {
        return false;
    }

    public int targetNodeId(int edgeId) {
        return 0;
    }

    public double length(int edgeId) {
        return 0;
    }

    public double elevationGain(int edgeId) {
        return 0;
    }

    public boolean hasProfile(int edgeId) {
        return false;
    }

    public float[] profileSamples(int edgeId) {
        return null;
    }

    public int attributesIndex(int edgeId) {
        return 0;
    }
}
