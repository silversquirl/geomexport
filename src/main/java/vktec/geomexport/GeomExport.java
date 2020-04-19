// vim: noet

package vktec.geomexport;

import net.fabricmc.api.ModInitializer;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import fi.dy.masa.malilib.config.ConfigManager;
import fi.dy.masa.malilib.event.InitializationHandler;
import fi.dy.masa.malilib.interfaces.IInitializationHandler;
import fi.dy.masa.malilib.event.InputEventHandler;
import fi.dy.masa.malilib.event.RenderEventHandler;
import fi.dy.masa.malilib.event.WorldLoadHandler;
import fi.dy.masa.malilib.interfaces.IRenderer;
import fi.dy.masa.malilib.interfaces.IWorldLoadListener;

public class GeomExport implements ModInitializer {
	public static final String MOD_ID = "geomexport";
	public static final Logger logger = LogManager.getLogger(MOD_ID);

	@Override
	public void onInitialize() {
		InitializationHandler.getInstance().registerInitializationHandler(new InitHandler());
	}

	public class InitHandler implements IInitializationHandler {
		@Override
		public void registerModHandlers() {
			ConfigManager.getInstance().registerConfigHandler(MOD_ID, new ConfigHandler());

			InputEventHandler.getKeybindManager().registerKeybindProvider(InputHandler.getInstance());
			InputEventHandler.getInputManager().registerMouseInputHandler(InputHandler.getInstance());
			InputEventHandler.getInputManager().registerKeyboardInputHandler(InputHandler.getInstance());

			IRenderer renderer = new RenderHandler();
			RenderEventHandler.getInstance().registerGameOverlayRenderer(renderer);
			RenderEventHandler.getInstance().registerWorldLastRenderer(renderer);

			IWorldLoadListener listener = new WorldLoadListener();
			WorldLoadHandler.getInstance().registerWorldLoadPreHandler(listener);
			WorldLoadHandler.getInstance().registerWorldLoadPostHandler(listener);
		}
	}
}
