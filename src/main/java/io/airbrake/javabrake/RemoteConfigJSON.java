package io.airbrake.javabrake;

import java.util.ArrayList;

class RemoteConfigJSON {
  String config_route;
  int poll_sec;
  ArrayList<RemoteSettingJSON> settings = new ArrayList<RemoteSettingJSON>();

  public void merge(RemoteConfigJSON other) {
    this.config_route = other.config_route;
    this.poll_sec = other.poll_sec;

    if (other.settings == null) {
      return;
    }

    for (RemoteSettingJSON os : other.settings) {
      Boolean found = false;

      for (RemoteSettingJSON s : settings) {
        if (!s.name.equals(os.name)) {
          continue;
        }

        found = true;
        s.enabled = os.enabled;
        s.endpoint = os.endpoint;
        break;
      }

      if (!found) {
        settings.add(os);
      }
    }
  }
}
