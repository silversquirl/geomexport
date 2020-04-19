// vim: noet

package vktec.geomexport;

import fi.dy.masa.malilib.config.options.ConfigColor;

public class Colors {
	public static final ConfigColor SELECTION_BOX_SIDE_COLOR = new ConfigColor("selectionBoxSideColor", "0x4030A0A0", "The color of the sides of the selection box");
	public static final ConfigColor SELECTION_BOX_EDGE_COLOR = new ConfigColor("selectionBoxEdgeColor", "0xFF000000", "The color of the edges of the selection box");
	public static final ConfigColor COLOR_CORNER_FOCUSED     = new ConfigColor("colorCornerFocused",    "0x70FF0000", "The color of the focused corner");
	public static final ConfigColor COLOR_CORNER_UNFOCUSED   = new ConfigColor("colorCornerUnfocused",  "0x4000FF00", "The color of the unfocused corner");

	public static final ConfigColor[] COLORS = {
		SELECTION_BOX_SIDE_COLOR,
		SELECTION_BOX_EDGE_COLOR,
		COLOR_CORNER_FOCUSED,
		COLOR_CORNER_UNFOCUSED
	};
}
