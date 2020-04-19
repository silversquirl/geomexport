// vim: noet

package vktec.geomexport;

import fi.dy.masa.malilib.config.IConfigHandler;
import fi.dy.masa.malilib.config.ConfigUtils;
import fi.dy.masa.malilib.util.FileUtils;
import fi.dy.masa.malilib.util.JsonUtils;
import fi.dy.masa.malilib.config.IConfigBase;
import fi.dy.masa.malilib.config.options.ConfigBoolean;
import fi.dy.masa.malilib.config.options.ConfigInteger;
import com.google.common.collect.ImmutableList;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import java.io.File;

public class ConfigHandler implements IConfigHandler {
	private static final String CONFIG_FILE = GeomExport.MOD_ID + ".json";

	public static class Generic {
		public static final ConfigBoolean ENABLE_PICK_BLOCK_FOCUS = new ConfigBoolean("enablePickBlockFocus", true, "Enable focusing a selection corner with the pick block key");
		public static final ConfigInteger PICK_BLOCK_FOCUS_RANGE  = new ConfigInteger("pickBlockFocusRange",  50,   "Range from which a selection corner can be focused with the pick block key");

		public static final ImmutableList<IConfigBase> GENERIC = ImmutableList.of(
				ENABLE_PICK_BLOCK_FOCUS,
				PICK_BLOCK_FOCUS_RANGE
		);
	}

	@Override
	public void load() {
		File configFile = new File(FileUtils.getConfigDirectory(), CONFIG_FILE);

		if (configFile.exists() && configFile.isFile() && configFile.canRead()) {
			JsonElement elem = JsonUtils.parseJsonFile(configFile);
			if (elem != null && elem.isJsonObject()) {
				JsonObject root = elem.getAsJsonObject();

				ConfigUtils.readConfigBase(root, "generic", Generic.GENERIC);
				ConfigUtils.readConfigBase(root, "hotkeys", Hotkeys.HOTKEYS);
				ConfigUtils.readConfigBase(root, "colors", Colors.COLORS);
			}
		}
	}

	@Override
	public void save() {
		File dir = FileUtils.getConfigDirectory();

		if ((dir.exists() && dir.isDirectory()) || dir.mkdirs()) {
			JsonObject root = new JsonObject();

			ConfigUtils.writeConfigBase(root, "generic", Generic.GENERIC);
			ConfigUtils.writeConfigBase(root, "hotkeys", Hotkeys.HOTKEYS);
			ConfigUtils.writeConfigBase(root, "colors", Colors.COLORS);

			JsonUtils.writeJsonToFile(root, new File(dir, CONFIG_FILE));
		}
	}
}
