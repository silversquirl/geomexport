// vim: noet

package vktec.geomexport;

import net.minecraft.util.math.BlockPos;

public class Selection {
	public static boolean aFocused = true;
	public static BlockPos a = new BlockPos(0, 90, 0);
	public static BlockPos b = new BlockPos(10, 100, 10);

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
}
