// vim: noet

package vktec.geomexport;

import net.minecraft.util.math.BlockPos;
import net.minecraft.client.MinecraftClient;
import fi.dy.masa.malilib.util.FileUtils;
import fi.dy.masa.malilib.util.StringUtils;
import fi.dy.masa.malilib.util.JsonUtils;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import java.io.File;

public class Selection {
	public static boolean aFocused = true;
	public static BlockPos a;
	public static BlockPos b;

	public static void toggleFocus() {
		aFocused = !aFocused;
	}

	public static BlockPos getFocused() {
		return aFocused ? a : b;
	}

	public static BlockPos getUnfocused() {
		return aFocused ? b : a;
	}

	public static void setFocused(BlockPos p) {
		if (aFocused) a = p;
		else b = p;
	}

	public static void load(MinecraftClient mc) {
		JsonElement elem = JsonUtils.parseJsonFile(getCurrentFile(mc));

		if (elem != null && elem.isJsonObject()) {
			JsonObject root = elem.getAsJsonObject();

			Selection.a = JsonUtils.blockPosFromJson(root, "coord_a");
			Selection.b = JsonUtils.blockPosFromJson(root, "coord_b");

			String focused = JsonUtils.getStringOrDefault(root, "focused", "a");
			Selection.aFocused = !focused.equals("b");
		} else {
			Selection.a = null;
			Selection.b = null;
		}
	}

	public static void save(MinecraftClient mc) {
		JsonObject root = new JsonObject();

		if (Selection.a != null) root.add("coord_a", JsonUtils.blockPosToJson(Selection.a));
		if (Selection.b != null) root.add("coord_b", JsonUtils.blockPosToJson(Selection.b));

		root.add("focused", new JsonPrimitive(Selection.aFocused ? "a" : "b"));

		JsonUtils.writeJsonToFile(root, getCurrentFile(mc));
	}

	private static File getCurrentFile(MinecraftClient mc) {
		File configDir = new File(FileUtils.getConfigDirectory(), GeomExport.MOD_ID);

		if (!configDir.exists() && !configDir.mkdirs()) {
			GeomExport.logger.warn("Failed to create config directory '{}'", configDir.getAbsolutePath());
		}
		return new File(configDir, StringUtils.getStorageFileName(false, "selection_", ".json", "default"));
	}
}
