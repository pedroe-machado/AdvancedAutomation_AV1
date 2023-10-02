package io.sim;
import org.w3c.dom.*;
import org.xml.sax.SAXException;

import javax.xml.parsers.*;
import java.io.*;
import java.util.ArrayList;

public class Itinerario {
    private String uriRoutesXML;
    private ArrayList<Route> routes;

    public Itinerario(String _uriRoutesXML) {
        this.uriRoutesXML = _uriRoutesXML;
        this.routes = parseRoutes();
    }

    private ArrayList<Route> parseRoutes() {
        ArrayList<Route> routesList = new ArrayList<>();

        try {
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            DocumentBuilder builder = factory.newDocumentBuilder();
            Document doc = builder.parse(this.uriRoutesXML);
            NodeList nList = doc.getElementsByTagName("vehicle");

            for (int i = 0; i < nList.getLength(); i++) {
                Node nNode = nList.item(i);
                if (nNode.getNodeType() == Node.ELEMENT_NODE) {
                    Element elem = (Element) nNode;
                    String id = elem.getAttribute("id");
                    Node node = elem.getElementsByTagName("route").item(0);
                    Element edges = (Element) node;
                    String[] edgesArray = edges.getAttribute("edges").split(" ");
                    ArrayList<String> edgesList = new ArrayList<>();
                    for (String edge : edgesArray) {
                        edgesList.add(edge);
                    }
                    routesList.add(new Route(id, edgesList));
                }
            }

        } catch (SAXException | IOException | ParserConfigurationException e) {
            e.printStackTrace();
        }

        return routesList;
    }

    public ArrayList<Route> getRoutes() {
        return this.routes;
    }
}

