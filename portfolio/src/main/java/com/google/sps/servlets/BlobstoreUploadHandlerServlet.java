package com.google.sps.servlets;

import com.google.appengine.api.blobstore.BlobstoreService;
import com.google.appengine.api.blobstore.BlobstoreServiceFactory;
import java.io.IOException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Handles creation of image upload URLs for the Blobstore. Intended for use
 * with the MapMarkerServlet to allow for image uploads with markers.
 */
@WebServlet("/blobstore-upload-url")
public class BlobstoreUploadHandlerServlet extends HttpServlet {

  @Override
  public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
    BlobstoreService blobstoreService = BlobstoreServiceFactory.getBlobstoreService();
    String uploadUrl = blobstoreService.createUploadUrl("/map-marker");

    response.setContentType("text/html");
    response.getWriter().println(uploadUrl);
  }
}