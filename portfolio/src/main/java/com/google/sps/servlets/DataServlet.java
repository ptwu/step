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

import com.google.gson.Gson;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Servlet that returns some example content of comments on the portfolio.
 */
@WebServlet("/data")
public class DataServlet extends HttpServlet {

  private List<Comment> commentsList;

  @Override
  public void init() {
    commentsList = new ArrayList<>();
    commentsList.add(new Comment("Alice", "Wow, this website is great!"));
    commentsList.add(new Comment("Bob", "Cool website!"));
  }

  @Override
  public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
    response.setContentType("application/json;");
    Gson gson = new Gson();
    String serializedJSON = gson.toJson(commentsList);
    response.getWriter().println(serializedJSON);
  }

  @Override
  public void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
    String username = request.getParameter("comment-username");
    String commentText = request.getParameter("comment-input");
    commentsList.add(new Comment(username, commentText));
    response.sendRedirect("/");
  }

  /** Representation type for a comment, complete with a username and body text */
  private class Comment {

    private String name;
    private String text;

    /**
     * Initalize the fields of this Comment using a constructor
     * 
     * @param name The username of the user posting the comment
     * @param text The body text of the comment posted by the user
     */
    public Comment(String name, String text) {
      this.name = name;
      this.text = text;
    }

    public String getName() {
      return name;
    }

    public String getComment() {
      return text;
    }

    public String toString() {
      return name + ": " + text;
    }
  }
}
