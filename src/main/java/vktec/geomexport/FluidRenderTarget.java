// vim: noet

package vktec.geomexport;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import net.minecraft.block.Blocks;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.block.BlockModels;
import net.minecraft.client.render.model.ModelLoader;
import net.minecraft.client.texture.Sprite;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;

public class FluidRenderTarget extends RenderTarget {
	public FluidRenderTarget(BlockPos origin, ObjCaches cache) {
		super(origin, cache);
	}

	protected List<Sprite> initSprites() {
		// Stolen from client.render.block.FluidRenderer
		final BlockModels models = MinecraftClient.getInstance().getBakedModelManager().getBlockModels();
		return Arrays.asList(
			models.getModel(Blocks.LAVA.getDefaultState()).getSprite(),
			ModelLoader.LAVA_FLOW.getSprite(),
			models.getModel(Blocks.WATER.getDefaultState()).getSprite(),
			ModelLoader.WATER_FLOW.getSprite(),
			ModelLoader.WATER_OVERLAY.getSprite()
		);
	}

	public void begin(BlockPos pos) {
		// Vertex coords are ofset by the block's position within the subchunk
		int x = pos.getX() & 0xF;
		int y = pos.getY() & 0xF;
		int z = pos.getZ() & 0xF;
		this.transpose = new Vec3d(pos.subtract(this.origin)).subtract(x, y, z);
	}

	private final List<Quad> quads = new ArrayList<>();

	protected void addQuad(Quad quad) {
		this.quads.add(quad);
	}

	protected Iterable<Quad> getQuads() {
		return this.quads;
	}

	protected void clearQuads() {
		this.quads.clear();
	}
}
