// vim: noet

package vktec.geomexport;

import org.lwjgl.opengl.GL11;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.BufferBuilder;
import net.minecraft.client.render.Tessellator;
import net.minecraft.client.render.VertexFormats;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.BlockPos;
import fi.dy.masa.malilib.util.Color4f;
import fi.dy.masa.malilib.render.RenderUtils;
import net.minecraft.client.util.math.MatrixStack;
import fi.dy.masa.malilib.interfaces.IRenderer;
import fi.dy.masa.malilib.config.HudAlignment;
import fi.dy.masa.malilib.util.StringUtils;
import java.util.ArrayList;

public class RenderHandler implements IRenderer {
	public static boolean enableRendering = true;

	@Override
	public void onRenderWorldLast(float partialTicks, MatrixStack matrices) {
		if (!enableRendering) return;

		if (Selection.a != null && Selection.b != null) {
			renderBox(Selection.a, Selection.b, Colors.SELECTION_BOX_EDGE_COLOR.getColor(), Colors.SELECTION_BOX_SIDE_COLOR.getColor());
		}

		if (Selection.getFocused() != null) {
			renderBox(Selection.getFocused(), Selection.getFocused(), new Color4f(0.0f, 0.0f, 0.0f, 0.0f), Colors.COLOR_CORNER_FOCUSED.getColor());
		}

		if (Selection.getUnfocused() != null) {
			renderBox(Selection.getUnfocused(), Selection.getUnfocused(), new Color4f(0.0f, 0.0f, 0.0f, 0.0f), Colors.COLOR_CORNER_UNFOCUSED.getColor());
		}
	}

	private String stringifyPos(BlockPos pos) {
		if (pos == null) return String.format("(%s)", StringUtils.translate("geomexport.text.coord_none"));
		else return String.format("%d %d %d", pos.getX(), pos.getY(), pos.getZ());
	}

	@Override
	public void onRenderGameOverlayPost(float partialTicks) {
		if (!enableRendering) return;

		ArrayList<String> txt = new ArrayList<>();

		String focused = String.format("(%s)", StringUtils.translate("geomexport.text.focused"));

		txt.add(String.format("%s: %s %s", StringUtils.translate("geomexport.text.corner_a"), stringifyPos(Selection.a), Selection.aFocused ? focused : ""));
		txt.add(String.format("%s: %s %s", StringUtils.translate("geomexport.text.corner_b"), stringifyPos(Selection.b), Selection.aFocused ? "" : focused));
		RenderUtils.renderText(10, 10, 1, 0xFFFFFFFF, 0x80000000, HudAlignment.BOTTOM_LEFT, true, true, txt);
	}

	public static void renderBox(BlockPos pos1, BlockPos pos2, Color4f lineColor, Color4f sideColor) {
		MinecraftClient mc = MinecraftClient.getInstance();

		Vec3d camPos = mc.gameRenderer.getCamera().getPos();

		double minX = Math.min(pos1.getX(), pos2.getX()) - camPos.x;
		double minY = Math.min(pos1.getY(), pos2.getY()) - camPos.y;
		double minZ = Math.min(pos1.getZ(), pos2.getZ()) - camPos.z;
		double maxX = Math.max(pos1.getX(), pos2.getX()) - camPos.x + 1.00;
		double maxY = Math.max(pos1.getY(), pos2.getY()) - camPos.y + 1.00;
		double maxZ = Math.max(pos1.getZ(), pos2.getZ()) - camPos.z + 1.00;

		// Makes it so that when you are outside the box, blocks on the
		// inner edge appear inside, and vice versa
		double factor = 0.999;
		minX *= factor;
		minY *= factor;
		minZ *= factor;
		maxX *= factor;
		maxY *= factor;
		maxZ *= factor;

		RenderSystem.disableTexture();
		RenderSystem.pushMatrix();

		RenderSystem.enableBlend();
		RenderSystem.disableCull();

		Tessellator tessellator = Tessellator.getInstance();

		BufferBuilder bufSide = tessellator.getBuffer();
		bufSide.begin(GL11.GL_QUADS, VertexFormats.POSITION_COLOR);

		RenderUtils.drawBoxAllSidesBatchedQuads(minX, minY, minZ, maxX, maxY, maxZ, sideColor, bufSide);

		tessellator.draw();

		BufferBuilder bufEdge = tessellator.getBuffer();
		bufEdge.begin(GL11.GL_LINES, VertexFormats.POSITION_COLOR);

		RenderSystem.lineWidth(1.5f);
		RenderUtils.drawBoxAllEdgesBatchedLines(minX, minY, minZ, maxX, maxY, maxZ, lineColor, bufEdge);

		tessellator.draw();

		RenderSystem.enableCull();
		RenderSystem.disableBlend();

		RenderSystem.popMatrix();
		RenderSystem.enableTexture();
	}
}
