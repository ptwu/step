package com.google.sps.servlets;

import com.google.auto.value.AutoValue;
import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.DatastoreServiceFactory;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.PreparedQuery;
import com.google.appengine.api.datastore.Query;
import com.google.gson.Gson;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Servlet that provides an API for retrieving and creating markers in the
 * Google Map of the website frontend. Note: no support for image upload yet
 * (Blobstore can be used for this later)
 */
@WebServlet("/map-marker")
public class MapMarkerServlet extends HttpServlet {

  private static final String MARKER_ENTITY_PROPERTY_LATITUDE = "lat";
  private static final String MARKER_ENTITY_PROPERTY_LONGITUDE = "lng";
  private static final String MARKER_ENTITY_PROPERTY_TITLE = "title";
  private static final String MARKER_ENTITY_PROPERTY_CONTENT = "content";

  @Override
  public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
    Query query = new Query("marker");
    DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
    PreparedQuery results = datastore.prepare(query);

    List<Marker> markers = new ArrayList<>();
    Iterable<Entity> entityIterable = results.asIterable();

    for (Entity entity : entityIterable) {
      double latitude = (double) entity.getProperty(MARKER_ENTITY_PROPERTY_LATITUDE);
      double longitude = (double) entity.getProperty(MARKER_ENTITY_PROPERTY_LONGITUDE);
      String title = (String) entity.getProperty(MARKER_ENTITY_PROPERTY_TITLE);
      String content = (String) entity.getProperty(MARKER_ENTITY_PROPERTY_CONTENT);
      Marker marker = Marker.create(latitude, longitude, title, content);
      markers.add(marker);
    }

    response.setContentType("application/json;");
    Gson gson = new Gson();
    String serializedJSON = gson.toJson(markers);
    response.getWriter().println(serializedJSON);
  }

  @Override
  public void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
    String latitudeString = request.getParameter(MARKER_ENTITY_PROPERTY_LATITUDE);
    String longitudeString = request.getParameter(MARKER_ENTITY_PROPERTY_LONGITUDE);
    String title = request.getParameter(MARKER_ENTITY_PROPERTY_TITLE);
    String content = request.getParameter(MARKER_ENTITY_PROPERTY_CONTENT);

    double latitude = Double.parseDouble(latitudeString);
    double longitude = Double.parseDouble(longitudeString);

    // Send error code 400 if any text input is whitespace or empty
    if (latitudeString.trim().length() == 0 || longitudeString.trim().length() == 0 || title.trim().length() == 0
        || content.trim().length() == 0) {
      response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
      return;
    }

    Entity markerEntity = new Entity("marker");
    markerEntity.setProperty(MARKER_ENTITY_PROPERTY_LATITUDE, latitude);
    markerEntity.setProperty(MARKER_ENTITY_PROPERTY_LONGITUDE, longitude);
    markerEntity.setProperty(MARKER_ENTITY_PROPERTY_TITLE, title);
    markerEntity.setProperty(MARKER_ENTITY_PROPERTY_CONTENT, content);

    DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
    datastore.put(markerEntity);
    response.sendRedirect("/");
  }

  /**
   * Value class for a marker, complete with a latitude, longitude, title text,
   * and content body text. Generated using AutoValue.
   */
  @AutoValue
  abstract static class Marker {
    static Marker create(double latitude, double longitude, String title, String content) {
      return new AutoValue_MapMarkerServlet_Marker(latitude, longitude, title, content);
    }

    abstract double latitude();

    abstract double longitude();

    abstract String title();

    abstract String content();
  }
}
