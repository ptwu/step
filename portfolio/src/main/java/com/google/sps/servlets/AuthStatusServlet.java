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
      statusData = UserAuthStatus.builder().setIsLoggedIn(true).setEmail(userEmail).setLogoutUrl(logoutUrl).build();
    } else {
      String loginUrl = userService.createLoginURL(AUTH_LOGIN_LOGOUT_REDIRECT_URI);
      statusData = UserAuthStatus.builder().setIsLoggedIn(false).setLoginUrl(loginUrl).build();
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

    abstract boolean isLoggedIn();

    abstract String email();

    abstract String loginUrl();

    abstract String logoutUrl();

    static Builder builder() {
      return new AutoValue_AuthStatusServlet_UserAuthStatus.Builder().setEmail("none").setLoginUrl("none")
          .setLogoutUrl("none");
    }

    @AutoValue.Builder
    abstract static class Builder {
      abstract Builder setIsLoggedIn(boolean value);

      abstract Builder setEmail(String value);

      abstract Builder setLoginUrl(String value);

      abstract Builder setLogoutUrl(String value);

      abstract UserAuthStatus build();
    }
  }
}