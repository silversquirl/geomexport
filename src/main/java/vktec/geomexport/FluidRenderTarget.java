// vim: noet

package vktec.geomexport;

import net.minecraft.block.Blocks;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.block.BlockModels;
import net.minecraft.client.render.model.ModelLoader;
import net.minecraft.client.texture.Sprite;
import net.minecraft.util.math.BlockPos;

public class FluidRenderTarget extends RenderTarget {
	public FluidRenderTarget(BlockPos origin, ObjCaches cache) {
		super(origin, cache, getSprites());
	}

	private static Sprite[] getSprites() {
		// Stolen from client.render.block.FluidRenderer
		final BlockModels models = MinecraftClient.getInstance().getBakedModelManager().getBlockModels();
		return new Sprite[]{
			models.getModel(Blocks.LAVA.getDefaultState()).getSprite(),
			ModelLoader.LAVA_FLOW.getSprite(),
			models.getModel(Blocks.WATER.getDefaultState()).getSprite(),
			ModelLoader.WATER_FLOW.getSprite(),
			ModelLoader.WATER_OVERLAY.getSprite(),
		};
	}
}
