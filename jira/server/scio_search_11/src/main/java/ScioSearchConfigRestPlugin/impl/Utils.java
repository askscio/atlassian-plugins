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
    if (isCurrentUserAdmin(userManager)){
      return;
    }
    String gleanServiceAccount = (String) pluginSettings.get(MyPluginComponentImpl.SERVICE_ACCOUNT_USER_EMAIL_CONFIG_KEY);
    final UserProfile profile = userManager.getRemoteUser();
    if (profile == null || !profile.getEmail().equals(gleanServiceAccount)) {
      throw new UnauthorizedException("Unauthorized");
    }
  }
}
