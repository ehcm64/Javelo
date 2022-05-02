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
public record GraphEdges(ByteBuffer edgesBuffer, IntBuffer profileIds,
                         ShortBuffer elevations) {

    private static final int LENGTH_OFFSET = Integer.BYTES;
    private static final int HEIGHT_DIFFERENCE_OFFSET = LENGTH_OFFSET
            + Short.BYTES;
    private static final int ATTRIBUTES_INDEX_OFFSET = HEIGHT_DIFFERENCE_OFFSET
            + Short.BYTES;
    private static final int NEW_EDGE_OFFSET = ATTRIBUTES_INDEX_OFFSET
            + Short.BYTES;

    private static final int PROFILE_TYPE_START = 30;
    private static final int PROFILE_TYPE_LENGTH = 2;
    private static final int PROFILE_INFO_START = 0;
    private static final int PROFILE_INFO_LENGTH = 30;

    private static final int NIBBLE_SIZE = 4;

    /**
     * Checks if an edge is inverted.
     *
     * @param edgeId the identity (or index) of the edge
     * @return a boolean : true if the edge is inverted - false otherwise
     */
    public boolean isInverted(int edgeId) {
        int wayAndNodeId = edgesBuffer.getInt(edgeId * NEW_EDGE_OFFSET);
        return wayAndNodeId < 0;
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
        short extractedLength = edgesBuffer.getShort(
                edgeId * NEW_EDGE_OFFSET + LENGTH_OFFSET);
        return Q28_4.asDouble(Short.toUnsignedInt(extractedLength));
    }

    /**
     * Returns the net positive elevation gain of an edge
     * (Sum of all positive elevation gains between samples).
     *
     * @param edgeId the index of the edge
     * @return the net positive elevation gain
     */
    public double elevationGain(int edgeId) {
        short extractedElevation = edgesBuffer.getShort(
                edgeId * NEW_EDGE_OFFSET + HEIGHT_DIFFERENCE_OFFSET);
        return Q28_4.asDouble(Short.toUnsignedInt(extractedElevation));
    }

    /**
     * Checks if a given edge has an elevation profile.
     *
     * @param edgeId the index of the edge
     * @return a boolean : true if the index has a profile - false otherwise
     */
    public boolean hasProfile(int edgeId) {
        int profileInfo = profileIds.get(edgeId);
        int profileType = Bits.extractUnsigned(profileInfo,
                PROFILE_TYPE_START,
                PROFILE_TYPE_LENGTH);
        return profileType != 0;
    }

    /**
     * Returns the array of elevation samples associated to an edge.
     *
     * @param edgeId the index of the edge
     * @return the array of floats containing the elevation samples
     */
    public float[] profileSamples(int edgeId) {
        int profileInfo = profileIds.get(edgeId);
        int profileType = Bits.extractUnsigned(
                profileInfo,
                PROFILE_TYPE_START,
                PROFILE_TYPE_LENGTH);
        int firstSampleIndex = Bits.extractUnsigned(
                profileInfo,
                PROFILE_INFO_START,
                PROFILE_INFO_LENGTH);
        short elevationData = edgesBuffer.getShort(edgeId
                * NEW_EDGE_OFFSET
                + LENGTH_OFFSET);
        int nbOfSamples = 1 + Math2.ceilDiv(elevationData, Q28_4.ofInt(2));
        float[] profileSamples = new float[nbOfSamples];

        switch (profileType) {
            case 1:
                for (int i = 0; i < nbOfSamples; i++) {
                    profileSamples[i] = Q28_4.asFloat(
                            Short.toUnsignedInt(
                                    elevations.get(firstSampleIndex + i)));
                }
                return isInverted(edgeId) ? invertArray(profileSamples) : profileSamples;
            case 2:
                return getProfileSamples(2, nbOfSamples, firstSampleIndex, edgeId);
            case 3:
                return getProfileSamples(3, nbOfSamples, firstSampleIndex, edgeId);
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
        short extractedAttributes = edgesBuffer.getShort(
                edgeId
                        * NEW_EDGE_OFFSET
                        + ATTRIBUTES_INDEX_OFFSET);
        return Short.toUnsignedInt(extractedAttributes);
    }

    private static float[] invertArray(float[] array) {
        float[] invertedList = new float[array.length];
        for (int i = 0; i < array.length / 2 + 1; i++) {
            invertedList[i] = array[array.length - 1 - i];
            invertedList[array.length - 1 - i] = array[i];
        }
        return invertedList;
    }

    private float[] getProfileSamples(int type, int nbOfSamples, int firstSampleIndex, int edgeId) {
        float[] profileSamples = new float[nbOfSamples];

        profileSamples[0] = Q28_4.asFloat(elevations.get(firstSampleIndex));
        short differences = 0;
        int start = 0;
        int length = type == 2 ? Byte.SIZE : NIBBLE_SIZE;

        for (int i = 1; i < nbOfSamples; i++) {
            if (type == 2) {
                differences = elevations.get(firstSampleIndex + (i + 1) / 2);
                start = Byte.SIZE * (i % 2); // to extract first or second byte
            } else if (type == 3) {
                differences = elevations.get(firstSampleIndex + (i + 3) / NIBBLE_SIZE);
                // to extract the correct nibble (4th or 1st, 2nd, 3rd
                start = (i % NIBBLE_SIZE == 0) ? 0 : (Short.SIZE - i % NIBBLE_SIZE * NIBBLE_SIZE);
            }
            int sampleDiff = Bits.extractSigned(differences, start, length);
            profileSamples[i] = profileSamples[i - 1] + Q28_4.asFloat(sampleDiff);
        }
        return isInverted(edgeId) ? invertArray(profileSamples) : profileSamples;
    }
}
