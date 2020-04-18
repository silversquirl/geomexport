// vim: noet

package vktec.geomexport;

import fi.dy.masa.malilib.interfaces.IWorldLoadListener;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.world.ClientWorld;

public class WorldLoadListener implements IWorldLoadListener {
	@Override
  public void onWorldLoadPre(ClientWorld worldBefore, ClientWorld worldAfter, MinecraftClient mc) {
    if (worldBefore != null) {
      Selection.save(mc);
    }
  }

  @Override
  public void onWorldLoadPost(ClientWorld worldBefore, ClientWorld worldAfter, MinecraftClient mc) {
    if (worldAfter != null) {
      Selection.load(mc);
    }
  }
}
