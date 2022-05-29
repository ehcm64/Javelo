package ch.epfl.javelo.routing;

import ch.epfl.javelo.projection.PointCh;
import org.w3c.dom.*;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.*;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;

/**
 * This class allows the creation of a Gpx file with information
 * on an itinerary (length, elevations, etc)
 *
 * @author Timo Moebel (345665)
 */
public class GpxGenerator {

    private GpxGenerator() {
    }

    private static Document newDocument() {
        try {
            return DocumentBuilderFactory
                    .newDefaultInstance()
                    .newDocumentBuilder()
                    .newDocument();
        } catch (ParserConfigurationException e) {
            throw new Error(e); // Should never happen
        }
    }

    /**
     * Returns a Document containing the Gpx Information of an itinerary and its profile.
     *
     * @param itinerary the route itinerary
     * @param profile   the profile of the route
     * @return a Document instance
     */
    public static Document createGpx(Route itinerary, ElevationProfile profile) {
        Document doc = newDocument();

        Element root = doc
                .createElementNS("http://www.topografix.com/GPX/1/1",
                        "gpx");
        doc.appendChild(root);

        root.setAttributeNS(
                "http://www.w3.org/2001/XMLSchema-instance",
                "xsi:schemaLocation",
                "http://www.topografix.com/GPX/1/1 "
                        + "http://www.topografix.com/GPX/1/1/gpx.xsd");
        root.setAttribute("version", "1.1");
        root.setAttribute("creator", "JaVelo");

        Element metadata = doc.createElement("metadata");
        root.appendChild(metadata);

        Element name = doc.createElement("name");
        metadata.appendChild(name);
        name.setTextContent("Route JaVelo");

        Element route = doc.createElement("rte");
        root.appendChild(route);

        double position = 0;
        for (int i = 0; i < itinerary.points().size(); i++) {
            PointCh point = itinerary.points().get(i);
            position += i == 0 ? 0 : itinerary.edges().get(i - 1).length();
            double altitude = profile.elevationAt(position);

            Element routePoint = doc.createElement("rtept");
            routePoint.setAttribute("lat", Double.toString(
                    Math.toDegrees(point.lat())));
            routePoint.setAttribute("lon", Double.toString(
                    Math.toDegrees(point.lon())));

            Element elevation = doc.createElement("ele");
            elevation.setTextContent(Double.toString(altitude));

            routePoint.appendChild(elevation);
            route.appendChild(routePoint);
        }
        return doc;
    }

    /**
     * Creates a Gpx file containing the information of an itinerary and its profile.
     *
     * @param fileName  the name to give to the file (must include the extension)
     * @param itinerary the itinerary
     * @param profile   the profile
     * @throws IOException if there is an IO problem
     */
    public static void writeGpx(String fileName,
                                Route itinerary,
                                ElevationProfile profile) throws IOException {

        Document doc = createGpx(itinerary, profile);
        Writer w = new FileWriter(fileName);

        try {
            Transformer transformer = TransformerFactory
                    .newDefaultInstance()
                    .newTransformer();
            transformer.setOutputProperty(OutputKeys.INDENT, "yes");
            transformer.transform(new DOMSource(doc),
                    new StreamResult(w));
        } catch (TransformerConfigurationException e) {
            throw new Error(e); // Should never happen
        } catch (TransformerException e) {
            throw new RuntimeException(e); // Should never happen
        }
    }
}
