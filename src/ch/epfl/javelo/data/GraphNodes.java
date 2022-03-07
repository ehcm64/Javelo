package ch.epfl.javelo.data;

import java.nio.IntBuffer;

public record GraphNodes(IntBuffer buffer) {

    public int count() {
        return 0;
    }

    public double nodeE(int nodeId) {
        return 0;
    }

    public double nodeN(int nodeId) {
        return 0;
    }

    public int outDegree(int nodeId) {
        return 0;
    }

    public int edgeId(int nodeId, int edgeIndex) {
        return 0;
    }
}
