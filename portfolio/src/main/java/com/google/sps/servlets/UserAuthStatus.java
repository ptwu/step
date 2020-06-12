package com.google.sps.servlets;

import com.google.auto.value.AutoValue;

/**
 * Value class for a certain authenticated/unauthenticated user, with a boolean
 * repesenting login status, email, and login url or logout url. If one of these
 * are not avaiable, the empty string is used.
 */
@AutoValue
abstract class UserAuthStatus {

  abstract boolean isLoggedIn();

  abstract String email();

  abstract String loginUrl();

  abstract String logoutUrl();

  static Builder builder() {
    return AutoValue_UserAuthStatus.builder();
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