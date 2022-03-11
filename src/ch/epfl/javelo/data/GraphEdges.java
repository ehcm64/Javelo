package ch.epfl.javelo.data;

import ch.epfl.javelo.Bits;
import ch.epfl.javelo.Math2;
import ch.epfl.javelo.Q28_4;

import java.nio.ByteBuffer;
import java.nio.IntBuffer;
import java.nio.ShortBuffer;

/**
 * Represents a collection of all Javelo's graph's edges.
 *
 * @author Edouard Mignan (345875)
 */
public record GraphEdges(ByteBuffer edgesBuffer, IntBuffer profileIds, ShortBuffer elevations) {
    private static final int WAY_AND_NODE_ID = 0;
    private static final int LENGTH_OFFSET = WAY_AND_NODE_ID + Integer.BYTES;
    private static final int HEIGHT_DIFFERENCE_OFFSET = LENGTH_OFFSET + Short.BYTES;
    private static final int ATTRIBUTES_INDEX_OFFSET = HEIGHT_DIFFERENCE_OFFSET + Short.BYTES;
    private static final int NEW_EDGE_OFFSET = ATTRIBUTES_INDEX_OFFSET + Short.BYTES;

    private static float[] invertArray(float[] array) {
        float[] invertedList = new float[array.length];
        for (int i = 0; i < array.length; i++) {
            invertedList[i] = array[array.length - 1 - i];
        }
        return invertedList;
    }

    /**
     * Checks if an edge is inverted.
     *
     * @param edgeId the identity (or index) of the edge
     * @return a boolean : true if the edge is inverted - false otherwise
     */
    public boolean isInverted(int edgeId) {
        return (edgesBuffer.getInt(edgeId * NEW_EDGE_OFFSET) & (1L << 31)) != 0;
    }

    /**
     * Returns the index of the target node of an edge.
     *
     * @param edgeId the index of the edge
     * @return the index of the target node
     */
    public int targetNodeId(int edgeId) {
        int wayAndNodeId = edgesBuffer.getInt(edgeId * NEW_EDGE_OFFSET);
        return isInverted(edgeId) ? ~wayAndNodeId : wayAndNodeId;
    }

    /**
     * Returns the length of an edge.
     *
     * @param edgeId the index of the edge
     * @return the length of the edge
     */
    public double length(int edgeId) {
        return Q28_4.asDouble(edgesBuffer.getShort(edgeId * NEW_EDGE_OFFSET + LENGTH_OFFSET));
    }

    /**
     * Returns the net positive elevation gain of an edge
     * (Sum of all positive elevation gains between samples).
     *
     * @param edgeId the index of the edge
     * @return the net positive elevation gain
     */
    public double elevationGain(int edgeId) {
        return Q28_4.asDouble(edgesBuffer.getShort(edgeId * NEW_EDGE_OFFSET + HEIGHT_DIFFERENCE_OFFSET));
    }

    /**
     * Checks if a given edge has an elevation profile.
     *
     * @param edgeId the index of the edge
     * @return a boolean : true if the index has a profile - false otherwise
     */
    public boolean hasProfile(int edgeId) {
        int profileType = Bits.extractUnsigned(profileIds.get(edgeId), 30, 2);
        return profileType != 0;
    }

    /**
     * Returns the array of elevation samples associated to an edge.
     *
     * @param edgeId the index of the edge
     * @return the array of floats containing the elevation samples
     */
    public float[] profileSamples(int edgeId) {
        int profileType = Bits.extractUnsigned(profileIds.get(edgeId), 30, 2);
        int firstProfileSampleIndex = Bits.extractUnsigned(profileIds.get(edgeId), 0, 30);
        int nbOfSamples = 1 + Math2.ceilDiv(edgesBuffer.getShort(edgeId * NEW_EDGE_OFFSET + LENGTH_OFFSET), Q28_4.ofInt(2));
        float[] profileSamples = new float[nbOfSamples];

        switch (profileType) {
            case 1:
                for (int i = 0; i < nbOfSamples; i++) {
                    profileSamples[i] = Q28_4.asFloat(elevations.get(firstProfileSampleIndex + i));
                }
                return isInverted(edgeId) ? invertArray(profileSamples) : profileSamples;
            case 2:
                profileSamples[0] = Q28_4.asFloat(elevations.get(firstProfileSampleIndex));
                for (int i = 1; i < nbOfSamples; i++) {
                    short differences = elevations.get(firstProfileSampleIndex + ((i + 1) / 2));
                    int sampleDiff = Bits.extractSigned(differences, 8 * (i % 2), 8);
                    profileSamples[i] = profileSamples[i - 1] + Q28_4.asFloat(sampleDiff);
                }
                return isInverted(edgeId) ? invertArray(profileSamples) : profileSamples;
            case 3:
                profileSamples[0] = Q28_4.asFloat(elevations.get(firstProfileSampleIndex));
                for (int i = 1; i < nbOfSamples; i++) {
                    short differences = elevations.get(firstProfileSampleIndex + ((i + 3) / 4));
                    int sampleDiff;
                    if (i % 4 == 0) { // case where we need to extract fourth 4bit section from short integer.
                        sampleDiff = Bits.extractSigned(differences, 0, 4);
                    } else { // case where we need to extract 1st, 2nd or 3rd 4bit section from short integer.
                        sampleDiff = Bits.extractSigned(differences, 16 - (i % 4) * 4, 4);
                    }
                    profileSamples[i] = profileSamples[i - 1] + Q28_4.asFloat(sampleDiff);
                }
                return isInverted(edgeId) ? invertArray(profileSamples) : profileSamples;
            default:
                return new float[0];
        }
    }

    /**
     * Returns the index of the attribute set associated to an edge.
     *
     * @param edgeId the index of the edge
     * @return the index of the attribute set
     */
    public int attributesIndex(int edgeId) {
        return edgesBuffer.getShort(edgeId * NEW_EDGE_OFFSET + ATTRIBUTES_INDEX_OFFSET);
    }
}
