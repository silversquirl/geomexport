// vim: noet

package vktec.geomexport;

import fi.dy.masa.malilib.gui.GuiConfigsBase;
import fi.dy.masa.malilib.config.IConfigBase;
import fi.dy.masa.malilib.gui.button.IButtonActionListener;
import fi.dy.masa.malilib.gui.button.ButtonBase;
import fi.dy.masa.malilib.gui.button.ButtonGeneric;
import fi.dy.masa.malilib.util.StringUtils;
import java.util.List;

public class GuiConfig extends GuiConfigsBase {
	private ConfigTab tab;

	public GuiConfig(ConfigTab tab) {
		super(10, 50, GeomExport.MOD_ID, null, "geomexport.gui.title.config");
		this.tab = tab;
	}

	@Override
	public void initGui() {
		super.initGui();

		int x = 10;
		int y = 26;

		x += this.createButton(x, y, ConfigTab.GENERIC);
		x += this.createButton(x, y, ConfigTab.HOTKEYS);
		x += this.createButton(x, y, ConfigTab.COLORS);
	}

	private int createButton(int x, int y, ConfigTab tab) {
		ButtonGeneric b = new ButtonGeneric(x, y, -1, 20, tab.getDisplayName());
		b.setEnabled(this.tab != tab);
		this.addButton(b, new IButtonActionListener() {
			@Override
			public void actionPerformedWithButton(ButtonBase button, int mouseButton) {
				GuiConfig.this.tab = tab;
				GuiConfig.this.initGui();
			}
		});
		return b.getWidth() + 2;
	}

	@Override
	public List<ConfigOptionWrapper> getConfigs() {
		List<? extends IConfigBase> configs = null;

		if (this.tab == ConfigTab.GENERIC) {
			configs = ConfigHandler.Generic.GENERIC;
		} else if (this.tab == ConfigTab.HOTKEYS) {
			configs = Hotkeys.HOTKEYS;
		} else if (this.tab == ConfigTab.COLORS) {
			configs = Colors.COLORS;
		}

		return ConfigOptionWrapper.createFor(configs);
	}

	public enum ConfigTab {
		GENERIC("geomexport.gui.button.config_generic"),
		HOTKEYS("geomexport.gui.button.config_hotkeys"),
		COLORS("geomexport.gui.button.config_colors");

		private final String translationKey;

		private ConfigTab(String translationKey) {
			this.translationKey = translationKey;
		}

		public String getDisplayName() {
			return StringUtils.translate(this.translationKey);
		}
	}
}
