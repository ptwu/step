// Copyright 2019 Google LLC
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//     https://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

package com.google.sps.servlets;

import com.google.auto.value.AutoValue;
import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.DatastoreServiceFactory;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.PreparedQuery;
import com.google.appengine.api.datastore.Query;
import com.google.appengine.api.datastore.Query.SortDirection;
import com.google.appengine.api.users.UserService;
import com.google.appengine.api.users.UserServiceFactory;
import com.google.gson.Gson;
import com.google.common.collect.Iterables;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Servlet that allows for creation and viewing (with a certain limit) of
 * existing comments in the Datastore
 */
@WebServlet("/data")
public class DataServlet extends HttpServlet {

  private static final String COMMENT_ENTITY_PROPERTY_NAME = "name";
  private static final String COMMENT_ENTITY_PROPERTY_EMAIL = "email";
  private static final String COMMENT_ENTITY_PROPERTY_TEXT = "text";
  private static final String COMMENT_ENTITY_PROPERTY_TIMESTAMP = "timestamp";

  @Override
  public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
    String requestParam = request.getParameter("limit");
    Query query = new Query("comment").addSort(COMMENT_ENTITY_PROPERTY_TIMESTAMP, SortDirection.DESCENDING);
    DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
    PreparedQuery results = datastore.prepare(query);

    List<Comment> comments = new ArrayList<>();
    Iterable<Entity> entityIterable = results.asIterable();
    if (requestParam != null) {
      int numCommentsToReturn = Integer.parseInt(requestParam);
      entityIterable = Iterables.limit(entityIterable, numCommentsToReturn);
    }

    for (Entity entity : entityIterable) {
      String name = (String) entity.getProperty(COMMENT_ENTITY_PROPERTY_NAME);
      String email = (String) entity.getProperty(COMMENT_ENTITY_PROPERTY_EMAIL);
      String text = (String) entity.getProperty(COMMENT_ENTITY_PROPERTY_TEXT);
      long timestamp = (long) entity.getProperty(COMMENT_ENTITY_PROPERTY_TIMESTAMP);

      Comment comment = Comment.create(name, email, text, timestamp);
      comments.add(comment);
    }

    response.setContentType("application/json;");
    Gson gson = new Gson();
    String serializedJSON = gson.toJson(comments);
    response.getWriter().println(serializedJSON);
  }

  @Override
  public void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
    UserService userService = UserServiceFactory.getUserService();
    if (!userService.isUserLoggedIn()) {
      response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
      return;
    }

    String username = request.getParameter("username");
    String email = userService.getCurrentUser().getEmail();
    String commentText = request.getParameter("text");
    long timestamp = System.currentTimeMillis();

    // Send error code 400 if comment or username is whitespace or empty
    if (commentText.trim().length() == 0 || username.trim().length() == 0) {
      response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
      return;
    }

    Entity commentEntity = new Entity("comment");
    commentEntity.setProperty(COMMENT_ENTITY_PROPERTY_NAME, username);
    commentEntity.setProperty(COMMENT_ENTITY_PROPERTY_EMAIL, email);
    commentEntity.setProperty(COMMENT_ENTITY_PROPERTY_TEXT, commentText);
    commentEntity.setProperty(COMMENT_ENTITY_PROPERTY_TIMESTAMP, timestamp);

    DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
    datastore.put(commentEntity);
    response.setStatus(HttpServletResponse.SC_OK);
  }

  /**
   * Value class for a comment, complete with a username, email address of the
   * poster, body text, and timestamp in milliseconds since the Unix epoch.
   * Generated using AutoValue.
   */
  @AutoValue
  abstract static class Comment {
    static Comment create(String name, String email, String text, long timestamp) {
      return new AutoValue_DataServlet_Comment(name, email, text, timestamp);
    }

    abstract String name();

    abstract String email();

    abstract String text();

    abstract long timestamp();
  }
}