// vim: noet

package vktec.geomexport;

import fi.dy.masa.malilib.config.options.ConfigHotkey;

public class Hotkeys {
	public static final ConfigHotkey TOGGLE_FOCUSED_CORNER = new ConfigHotkey("toggleFocusedCorner", "G,T", "Toggles the focused corner of the selection");
	public static final ConfigHotkey SELECTION_GROWTH_MOD  = new ConfigHotkey("selectionGrowthMod",  "G",   "When held, uses the scroll wheel to move the focused corner of the selection");
	public static final ConfigHotkey MOVE_FOCUSED_CORNER   = new ConfigHotkey("moveFocusedCorner",   "G,M", "Moves the currently focused corner to the player position");
	public static final ConfigHotkey CLEAR_SELECTION       = new ConfigHotkey("clearSelection",      "G,C", "Clears the selection coordinates");
	public static final ConfigHotkey TOGGLE_RENDERING      = new ConfigHotkey("toggleRendering",     "G,R", "Toggles all HUD and in-world rendering");
	public static final ConfigHotkey WRITE_FILE            = new ConfigHotkey("writeFile",           "G,W", "TEMPORARY: Writes the area to an obj file");

	public static final ConfigHotkey[] hotkeys = {
		TOGGLE_FOCUSED_CORNER,
		SELECTION_GROWTH_MOD,
		MOVE_FOCUSED_CORNER,
		CLEAR_SELECTION,
		TOGGLE_RENDERING,
		WRITE_FILE,
	};
}
