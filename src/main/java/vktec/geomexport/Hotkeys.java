// vim: noet

package vktec.geomexport;

import fi.dy.masa.malilib.config.options.ConfigHotkey;
import fi.dy.masa.malilib.hotkeys.KeybindSettings;
import com.google.common.collect.ImmutableList;

public class Hotkeys {
	public static final ConfigHotkey SELECTION_GROWTH_MOD  = new ConfigHotkey("selectionGrowthMod", "LEFT_ALT", KeybindSettings.MODIFIER_INGAME,   "When held, uses the scroll wheel to move the focused corner of the selection");
	public static final ConfigHotkey EXPORT_SELECTION      = new ConfigHotkey("exportSelection",    "G",        KeybindSettings.RELEASE_EXCLUSIVE, "Exports the selection into a folder");

	public static final ConfigHotkey TOGGLE_FOCUSED_CORNER = new ConfigHotkey("toggleFocusedCorner", "G,T", "Toggles the focused corner of the selection");
	public static final ConfigHotkey MOVE_FOCUSED_CORNER   = new ConfigHotkey("moveFocusedCorner",   "G,M", "Moves the currently focused corner to the player's feet");
	public static final ConfigHotkey CLEAR_SELECTION       = new ConfigHotkey("clearSelection",      "G,D", "Clears the selection coordinates");
	public static final ConfigHotkey TOGGLE_RENDERING      = new ConfigHotkey("toggleRendering",     "G,R", "Toggles all HUD and in-world rendering");
	public static final ConfigHotkey OPEN_CONFIG           = new ConfigHotkey("openConfig",          "G,C", "Opens the config menu");

	public static final ImmutableList<ConfigHotkey> HOTKEYS = ImmutableList.of(
		SELECTION_GROWTH_MOD,
		EXPORT_SELECTION,

		TOGGLE_FOCUSED_CORNER,
		MOVE_FOCUSED_CORNER,
		CLEAR_SELECTION,
		TOGGLE_RENDERING,
		OPEN_CONFIG
	);
}
