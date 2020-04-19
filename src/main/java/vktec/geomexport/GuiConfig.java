package vktec.geomexport;

import fi.dy.masa.malilib.gui.GuiConfigsBase;
import fi.dy.masa.malilib.util.StringUtils;
import java.util.List;
import java.util.Arrays;

public class GuiConfig extends GuiConfigsBase {
  public GuiConfig() {
    super(10, 50, GeomExport.MOD_ID, null, "geomexport.gui.title.config");
  }

  @Override
  public void initGui() {
    super.initGui();

    int x = 12;
    int y = 30;
  }

  @Override
  public List<ConfigOptionWrapper> getConfigs() {
    return ConfigOptionWrapper.createFor(Arrays.asList(Hotkeys.hotkeys));
  }
}
