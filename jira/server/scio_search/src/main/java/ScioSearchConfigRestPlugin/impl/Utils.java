package ScioSearchConfigRestPlugin.impl;

import com.atlassian.crowd.embedded.api.User;
import com.atlassian.sal.api.pluginsettings.PluginSettings;
import com.atlassian.sal.api.user.UserManager;
import com.atlassian.sal.api.user.UserProfile;

public class Utils {
  public static boolean isCurrentUserAdmin(UserManager userManager) {
    final UserProfile profile = userManager.getRemoteUser();
    return profile != null && userManager.isAdmin(profile.getUserKey());
  }

  public static void validateUserIsAdmin(UserManager userManager) {
    if (!isCurrentUserAdmin(userManager)) {
      throw new UnauthorizedException("Unauthorized");
    }
  }

  public static void validateUser(UserManager userManager, PluginSettings pluginSettings) {
    validateUserIsAdmin(userManager);
  }
}
