package com.askscio.atlassian_plugins.confluence.impl;

import com.atlassian.sal.api.pluginsettings.PluginSettings;
import com.atlassian.sal.api.user.UserManager;
import com.atlassian.sal.api.user.UserProfile;

public class Utils {

  public static String getPluginSettingsValueOrElse(PluginSettings pluginSettings,
      String pluginSettingsKey, String orElse) {
    String val = (String) pluginSettings.get(pluginSettingsKey);
    return val != null ? val : orElse;
  }

  public static boolean isCurrentUserAdmin(UserManager userManager) {
    final UserProfile profile = userManager.getRemoteUser();
    return profile != null && userManager.isSystemAdmin(profile.getUserKey());
  }

  public static void validateUserIsAdmin(UserManager userManager) {
    if (!isCurrentUserAdmin(userManager)) {
      throw new UnauthorizedException("Unauthorized");
    }
  }
}
