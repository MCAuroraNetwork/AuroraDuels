package club.aurorapvp.modules.arena;

public interface ArenaInterface {
  String getName();
  boolean exists();
  boolean inUse();
  void setAvailibility(boolean value);
}