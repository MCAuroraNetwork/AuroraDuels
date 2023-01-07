package club.aurorapvp.modules.arenas;

public interface ArenaInterface {
  String getName();
  boolean exists();
  boolean inUse();
  void setAvailibility(boolean value);
}