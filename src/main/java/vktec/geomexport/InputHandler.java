// vim: noet

package vktec.geomexport;

import fi.dy.masa.malilib.hotkeys.IHotkey;
import fi.dy.masa.malilib.hotkeys.IHotkeyCallback;
import fi.dy.masa.malilib.hotkeys.KeyAction;
import fi.dy.masa.malilib.hotkeys.IKeybind;
import fi.dy.masa.malilib.hotkeys.IKeybindProvider;
import fi.dy.masa.malilib.hotkeys.IKeybindManager;
import fi.dy.masa.malilib.hotkeys.IMouseInputHandler;
import fi.dy.masa.malilib.hotkeys.IKeyboardInputHandler;
import fi.dy.masa.malilib.config.options.ConfigHotkey;
import fi.dy.masa.malilib.util.GuiUtils;
import fi.dy.masa.malilib.gui.GuiBase;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.Direction;
import net.minecraft.client.MinecraftClient;
import java.io.IOException;
import java.nio.file.FileSystems;
import net.minecraft.util.math.Box;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.HitResult;
import com.google.common.collect.ImmutableList;

public class InputHandler implements IKeybindProvider, IMouseInputHandler, IHotkeyCallback, IKeyboardInputHandler {
	private static final InputHandler INSTANCE = new InputHandler();

	public static InputHandler getInstance() { return INSTANCE; }

	private static final Box FULL_BLOCK_BOUNDS = new net.minecraft.util.math.Box(0, 0, 0, 1, 1, 1);

	private InputHandler() {
		for (ConfigHotkey hk : Hotkeys.HOTKEYS) {
			hk.getKeybind().setCallback(this);
		}
	}

	@Override
	public void addKeysToMap(IKeybindManager manager) {
		for (ConfigHotkey hk : Hotkeys.HOTKEYS) {
			manager.addKeybindToMap(hk.getKeybind());
		}
	}

	@Override
	public void addHotkeys(IKeybindManager manager) {
		manager.addHotkeysForCategory("geomexport", "geomexport.hotkeys.category.all", Hotkeys.HOTKEYS);
	}

	@Override
	public boolean onKeyAction(KeyAction action, IKeybind key) {
		if (key == Hotkeys.TOGGLE_FOCUSED_CORNER.getKeybind()) {
			Selection.toggleFocus();
			return true;
		} else if (key == Hotkeys.MOVE_FOCUSED_CORNER.getKeybind()) {
			// If you're standing on e.g. a path, you probably still want to
			// select the block above, so offset up slightly
			Vec3d pos = MinecraftClient.getInstance().player.getPos().add(0, 0.2, 0);
			Selection.setFocused(new BlockPos(pos));
			return true;
		} else if (key == Hotkeys.CLEAR_SELECTION.getKeybind()) {
			Selection.a = null;
			Selection.b = null;
			return true;
		} else if (key == Hotkeys.TOGGLE_RENDERING.getKeybind()) {
			RenderHandler.enableRendering = !RenderHandler.enableRendering;
			return true;
		} else if (key == Hotkeys.OPEN_CONFIG.getKeybind()) {
			GuiBase.openGui(new GuiConfig(GuiConfig.ConfigTab.GENERIC));
			return true;
		} else if (key == Hotkeys.EXPORT_SELECTION.getKeybind()) {
			GuiBase.openGui(new GuiExport());
			return true;
		}
		return false;
	}

	private boolean tryPickBlockFocus(MinecraftClient mc) {
		if (!ConfigHandler.Generic.ENABLE_PICK_BLOCK_FOCUS.getBooleanValue()) return false;

		int range = ConfigHandler.Generic.PICK_BLOCK_FOCUS_RANGE.getIntegerValue();
		Vec3d cam = mc.player.getCameraPosVec(1f);
		Vec3d disp = mc.player.getRotationVec(1f).multiply(range);

		BlockHitResult hitA = Selection.a == null ? null : Box.rayTrace(ImmutableList.of(FULL_BLOCK_BOUNDS), cam, cam.add(disp), Selection.a);
		BlockHitResult hitB = Selection.b == null ? null : Box.rayTrace(ImmutableList.of(FULL_BLOCK_BOUNDS), cam, cam.add(disp), Selection.b);

		double sqDistA = hitA != null && hitA.getType() == HitResult.Type.BLOCK ? hitA.getPos().squaredDistanceTo(cam) : -1;
		double sqDistB = hitB != null && hitB.getType() == HitResult.Type.BLOCK ? hitB.getPos().squaredDistanceTo(cam) : -1;

		if (sqDistA != -1 && sqDistB == -1) {
			Selection.aFocused = true;
			return true;
		}

		if (sqDistA == -1 && sqDistB != -1) {
			Selection.aFocused = false;
			return true;
		}

		if (sqDistA != -1 && sqDistB != -1) {
			Selection.aFocused = sqDistA < sqDistB;
			return true;
		}

		return false;
	}

	@Override
	public boolean onMouseClick(int mouseX, int mouseY, int eventButton, boolean eventButtonState) {
		MinecraftClient mc = MinecraftClient.getInstance();

		if (GuiUtils.getCurrentScreen() != null) return false;
		if (mc.world == null) return false;
		if (mc.player == null) return false;

		if (eventButtonState &&
				mc.options.keyPickItem.matchesMouse(eventButton)) {
			return tryPickBlockFocus(mc);
		}

		return false;
	}

	@Override
	public boolean onKeyInput(int keyCode, int scanCode, int modifiers, boolean eventKeyState) {
		MinecraftClient mc = MinecraftClient.getInstance();

		if (GuiUtils.getCurrentScreen() != null) return false;
		if (mc.world == null) return false;
		if (mc.player == null) return false;

		if (eventKeyState &&
				mc.options.keyPickItem.matchesKey(keyCode, scanCode)) {
			return tryPickBlockFocus(mc);
		}

		return false;
	}

	@Override
	public boolean onMouseScroll(int mouseX, int mouseY, double _dWheel) {
		MinecraftClient mc = MinecraftClient.getInstance();

		if (GuiUtils.getCurrentScreen() != null) return false;
		if (mc.world == null) return false;
		if (mc.player == null) return false;

		int dWheel = _dWheel > 0 ? 1 : -1;
		Direction dir = Direction.getEntityFacingOrder(mc.player)[0];

		if (Hotkeys.SELECTION_GROWTH_MOD.getKeybind().isKeybindHeld()) {
			Selection.setFocused(Selection.getFocused().offset(dir, dWheel));
			return true;
		}

		return false;
	}
}
