package ch.epfl.javelo.data;

import ch.epfl.javelo.Bits;
import ch.epfl.javelo.Preconditions;

import java.nio.IntBuffer;

public record GraphNodes(IntBuffer buffer) {
    private static final int OFFSET_E = 0;
    private static final int OFFSET_N = OFFSET_E + 1;
    private static final int OFFSET_OUT_EDGES = OFFSET_N + 1;
    private static final int NODE_INTS = OFFSET_OUT_EDGES + 1;


    public int count(){
        return buffer.capacity()/3;
    }

    public double nodeE(int nodeId) {
        return buffer.get(nodeId*3 + OFFSET_E);
    }

    public double nodeN(int nodeId){
        return buffer.get(nodeId*3 + OFFSET_N);
    }

    public int outDegree(int nodeId){
        int edgesAndIndex = buffer.get(nodeId*3 + OFFSET_OUT_EDGES);
        int nbEdges = Bits.extractUnsigned(edgesAndIndex,28,4);
        return nbEdges;


    }
    public int edgeId(int nodeId, int edgeIndex){
        Preconditions.checkArgument( 0 <= edgeIndex && edgeIndex < outDegree(nodeId));
        int edgesAndIndex = buffer.get(nodeId*3 + OFFSET_OUT_EDGES);
        int firstEdgeIndex = Bits.extractUnsigned(edgesAndIndex,0,28);
        return (int) Math.scalb(firstEdgeIndex, edgeIndex);
    }

}
