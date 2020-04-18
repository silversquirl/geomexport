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

import java.util.Arrays;

public class RenderHandler implements IRenderer {
	@Override
	public void onRenderWorldLast(float partialTicks, MatrixStack matrices) {
		renderBox(Selection.a, Selection.b, new Color4f(1.0f, 0.0f, 0.0f), new Color4f(0.2f, 0.7f, 0.7f, 0.4f));
		renderBox(Selection.getFocused(),   Selection.getFocused(),   new Color4f(0.0f, 0.0f, 0.0f, 0.0f), new Color4f(1.0f, 0.0f, 0.0f, 0.4f));
		renderBox(Selection.getUnfocused(), Selection.getUnfocused(), new Color4f(0.0f, 0.0f, 0.0f, 0.0f), new Color4f(0.0f, 1.0f, 0.0f, 0.2f));
	}

	@Override
	public void onRenderGameOverlayPost(float partialTicks) { }

	public static void renderBox(BlockPos pos1, BlockPos pos2, Color4f lineColor, Color4f sideColor) {
		MinecraftClient mc = MinecraftClient.getInstance();

		Vec3d camPos = mc.gameRenderer.getCamera().getPos();

		double minX = Math.min(pos1.getX(), pos2.getX()) - camPos.x;
		double minY = Math.min(pos1.getY(), pos2.getY()) - camPos.y;
		double minZ = Math.min(pos1.getZ(), pos2.getZ()) - camPos.z;
		double maxX = Math.max(pos1.getX(), pos2.getX()) - camPos.x + 1.00;
		double maxY = Math.max(pos1.getY(), pos2.getY()) - camPos.y + 1.00;
		double maxZ = Math.max(pos1.getZ(), pos2.getZ()) - camPos.z + 1.00;

		double offset = 0.002;

		if (minX > 0) minX -= offset; else minX += offset;
		if (minY > 0) minY -= offset; else minY += offset;
		if (minZ > 0) minZ -= offset; else minZ += offset;
		if (maxX > 0) maxX -= offset; else maxX += offset;
		if (maxY > 0) maxY -= offset; else maxY += offset;
		if (maxZ > 0) maxZ -= offset; else maxZ += offset;

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

		RenderSystem.lineWidth(2f);
		RenderUtils.drawBoxAllEdgesBatchedLines(minX, minY, minZ, maxX, maxY, maxZ, lineColor, bufEdge);

		tessellator.draw();

		RenderSystem.enableCull();
		RenderSystem.disableBlend();

		RenderSystem.popMatrix();
		RenderSystem.enableTexture();
	}
}
