// vim: noet

package vktec.geomexport;

import net.fabricmc.api.ModInitializer;
import fi.dy.masa.malilib.event.InitializationHandler;
import fi.dy.masa.malilib.interfaces.IInitializationHandler;
import fi.dy.masa.malilib.event.InputEventHandler;
import fi.dy.masa.malilib.event.RenderEventHandler;
import fi.dy.masa.malilib.interfaces.IRenderer;

public class GeomExport implements ModInitializer {
	@Override
	public void onInitialize() {
		InitializationHandler.getInstance().registerInitializationHandler(new InitHandler());
	}

	public class InitHandler implements IInitializationHandler {
		@Override
		public void registerModHandlers() {
			InputEventHandler.getKeybindManager().registerKeybindProvider(InputHandler.getInstance());
			InputEventHandler.getInputManager().registerMouseInputHandler(InputHandler.getInstance());
			IRenderer renderer = new RenderHandler();
			RenderEventHandler.getInstance().registerGameOverlayRenderer(renderer);
			RenderEventHandler.getInstance().registerWorldLastRenderer(renderer);
		}
	}
}
