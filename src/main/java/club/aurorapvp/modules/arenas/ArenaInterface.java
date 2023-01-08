package club.aurorapvp.modules.arenas;

import club.aurorapvp.datahandlers.arenadata.ArenaData;

public interface ArenaInterface {
  String getName();
  boolean exists();
  boolean inUse();
  void setAvailability(boolean value);
  ArenaData getData();
}