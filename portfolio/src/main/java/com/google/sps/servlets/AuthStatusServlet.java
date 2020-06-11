package com.google.sps.servlets;

import java.io.IOException;

import com.google.appengine.api.users.UserService;
import com.google.appengine.api.users.UserServiceFactory;
import com.google.auto.value.AutoValue;
import com.google.gson.Gson;

import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Servlet that handles authentication: provides login and logout URLs for users
 * and checks login status of the user
 */
@WebServlet("/auth-status")
public class AuthStatusServlet extends HttpServlet {

  private static final String AUTH_LOGIN_LOGOUT_REDIRECT_URI = "/";

  @Override
  public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
    UserService userService = UserServiceFactory.getUserService();
    UserAuthStatus statusData;
    if (userService.isUserLoggedIn()) {
      String logoutUrl = userService.createLoginURL(AUTH_LOGIN_LOGOUT_REDIRECT_URI);
      String userEmail = userService.getCurrentUser().getEmail();
      statusData = UserAuthStatus.create(true, userEmail, "", logoutUrl);
    } else {
      String loginUrl = userService.createLoginURL(AUTH_LOGIN_LOGOUT_REDIRECT_URI);
      statusData = UserAuthStatus.create(false, "", loginUrl, "");
    }

    response.setContentType("application/json;");
    Gson gson = new Gson();
    String serializedJSON = gson.toJson(statusData);
    response.getWriter().println(serializedJSON);
  }

  /**
   * Value class for a certain authenticated/unauthenticated user, with a boolean
   * repesenting login status, email, and login url or logout url. If one of these
   * are not avaiable, the empty string is used.
   */
  @AutoValue
  abstract static class UserAuthStatus {
    static UserAuthStatus create(boolean isLoggedIn, String email, String loginUrl, String logoutUrl) {
      return new AutoValue_AuthStatusServlet_UserAuthStatus(isLoggedIn, email, loginUrl, logoutUrl);
    }

    abstract boolean isLoggedIn();

    abstract String email();

    abstract String loginUrl();

    abstract String logoutUrl();
  }
}