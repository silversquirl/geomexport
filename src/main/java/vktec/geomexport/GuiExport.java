// vim: noet

package vktec.geomexport;

import fi.dy.masa.malilib.util.StringUtils;
import fi.dy.masa.malilib.util.InfoUtils;
import fi.dy.masa.malilib.gui.Message.MessageType;
import fi.dy.masa.malilib.gui.GuiListBase;
import fi.dy.masa.malilib.gui.interfaces.ISelectionListener;
import fi.dy.masa.malilib.gui.button.IButtonActionListener;
import fi.dy.masa.malilib.gui.button.ButtonBase;
import fi.dy.masa.malilib.gui.button.ButtonGeneric;
import fi.dy.masa.malilib.gui.GuiConfirmAction;
import fi.dy.masa.malilib.gui.GuiBase;
import fi.dy.masa.malilib.gui.widgets.WidgetDirectoryEntry;
import fi.dy.masa.malilib.gui.widgets.WidgetFileBrowserBase.DirectoryEntry;
import fi.dy.masa.malilib.interfaces.IConfirmationListener;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.MinecraftClient;
import java.io.File;
import java.io.IOException;
import java.nio.file.Path;

public class GuiExport extends GuiListBase<DirectoryEntry, WidgetDirectoryEntry, WidgetExportBrowser> implements IButtonActionListener, IConfirmationListener {
	public GuiExport() {
		super(10, 48);
		this.title = StringUtils.translate("geomexport.gui.title.export");
	}

	private String infoString = "";
	private ButtonBase exportBtn = null;
	private boolean doClose = false;

	@Override
	public void initGui() {
		super.initGui();

		if (this.doClose) {
			this.closeGui(false);
			return;
		}

		this.exportBtn = new ButtonGeneric(12, this.height - 27, -1, false, "geomexport.gui.button.export");
		this.addButton(this.exportBtn, this);

		if (Selection.a == null || Selection.b == null) {
			this.infoString = "You must create a selection before exporting it.";
		} else {
			this.infoString = "Navigate to a folder to export to.";
		}

		onDirChange(this.getListWidget().getCurrentDirectory());
	}

	@Override
	public void actionPerformedWithButton(ButtonBase b, int mouseButton) {
		if (b != this.exportBtn) return;

		// Sanity checks
		if (Selection.a == null || Selection.b == null) {
			this.infoString = "You must create a selection before exporting it.";
			this.exportBtn.setEnabled(false);
			return;
		}

		GuiConfirmAction confirm = new GuiConfirmAction(320, "geomexport.gui.title.confirm_export", this, this, "geomexport.gui.text.confirm_export");
		GuiBase.openGui(confirm);
	}

	@Override
	public boolean onActionConfirmed() {
		this.doClose = true;

		MinecraftClient.getInstance().execute(() -> {
			InfoUtils.showInGameMessage(MessageType.INFO, "geomexport.message.export_start");

			Path path = this.getListWidget().getCurrentDirectory().toPath();

			try (BlocksWriter bw = new BlocksWriter(path)) {
				bw.writeRegion(MinecraftClient.getInstance().world, Selection.a, Selection.b);
				InfoUtils.showInGameMessage(MessageType.SUCCESS, "geomexport.message.export_success");
			} catch (IOException e) {
				InfoUtils.showInGameMessage(MessageType.ERROR, "geomexport.message.export_failure");
			}
		});

		return true;
	}

	@Override
	public boolean onActionCancelled() {
		return false;
	}

	public void onDirChange(File dir) {
		if (Selection.a == null || Selection.b == null) {
			this.exportBtn.setEnabled(false);
		} else if (dir.equals(this.getListWidget().getRootDirectory())) {
			this.exportBtn.setEnabled(false);
		} else {
			this.exportBtn.setEnabled(true);
		}
	}

	@Override
	protected WidgetExportBrowser createListWidget(int x, int y) {
		WidgetExportBrowser b = new WidgetExportBrowser(x, y, 0, 0, this, "export", this.getSelectionListener());
		b.setParent(this.getParent());
		return b;
	}

	@Override
	protected ISelectionListener<DirectoryEntry> getSelectionListener() {
		return null;
	}

	@Override
	public int getBrowserWidth() {
		return this.width - 20;
	}

	@Override
	public int getBrowserHeight() {
		return this.height - 85;
	}

	@Override
	public void render(int mouseX, int mouseY, float partialTicks) {
		super.render(mouseX, mouseY, partialTicks);

		this.drawString(infoString, 12, 30, COLOR_WHITE);
	}
}
