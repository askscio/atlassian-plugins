package com.askscio.atlassian_plugins.confluence.impl;

import com.atlassian.sal.api.pluginsettings.PluginSettings;

public class Utils {
  public static String getPluginSettingsValueOrElse(PluginSettings pluginSettings,
      String pluginSettingsKey, String orElse) {
    String val = (String) pluginSettings.get(pluginSettingsKey);
    return val != null ? val : orElse;
  }
}
