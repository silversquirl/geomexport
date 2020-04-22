// vim: noet

package vktec.geomexport;

import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.util.Random;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.color.block.BlockColors;
import net.minecraft.client.render.block.BlockModels;
import net.minecraft.client.render.block.BlockRenderManager;
import net.minecraft.client.render.model.BakedModel;
import net.minecraft.client.render.model.BakedQuad;
import net.minecraft.client.texture.NativeImage;
import net.minecraft.client.texture.Sprite;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.fluid.FluidState;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec2f;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.minecraft.block.MaterialColor;

public class BlocksWriter implements AutoCloseable {
	private final Path dir;
	private final Path textureDir;
	private final MtlWriter mtlWriter;
	private final ObjWriter objWriter;

	private static final Direction[] directions = new Direction[Direction.values().length + 1];
	static {
		directions[0] = null;
		System.arraycopy(Direction.values(), 0, directions, 1, Direction.values().length);
	}

	public BlocksWriter(Path dir) throws IOException {
		this.dir = dir;
		this.textureDir = dir.resolve("textures");
		this.textureDir.toFile().mkdirs();

		Path mtlPath = dir.resolve("material.mtl");
		this.mtlWriter = new MtlWriter(mtlPath);

		this.objWriter = new ObjWriter(dir.resolve("model.obj"));
		this.objWriter.addMtl(dir.relativize(mtlPath).toString());
	}

	public void close() throws IOException {
		this.mtlWriter.close();
		this.objWriter.close();
	}

	private static final BlockColors colorMap = MinecraftClient.getInstance().getBlockColorMap();
	private static final BlockModels models = MinecraftClient.getInstance().getBakedModelManager().getBlockModels();
	private static final BlockRenderManager blockRenderer = MinecraftClient.getInstance().getBlockRenderManager();
	private static final Random random = new Random();

	private final ObjCaches cache = new ObjCaches();
	private final MatrixStack matrixStack = new MatrixStack();
	private BlockRenderTarget blockRenderTarget;
	private FluidRenderTarget fluidRenderTarget;

	private BlockPos origin;

	public void writeRegion(World world, BlockPos a, BlockPos b) throws IOException {
		this.origin = a;
		this.blockRenderTarget = new BlockRenderTarget(this.origin, this.cache);
		this.fluidRenderTarget = new FluidRenderTarget(this.origin, this.cache);

		for (BlockPos pos : BlockPos.iterate(a, b)) {
			BlockState block = world.getBlockState(pos);
			this.writeBlock(world, pos, block);
			this.writeFluid(world, pos, block);
		}

		for (Material mat : cache.material.values()) {
			if (mat.refCount > 0) {
				mat.write(this.mtlWriter, this.dir, this.textureDir);
			}
		}
	}

	private void writeBlock(World world, BlockPos pos, BlockState block) throws IOException {
		String name = block.getBlock().getName().asString();

		this.blockRenderTarget.begin(pos);
		boolean cullHiddenFaces = false;
		this.matrixStack.push();
		blockRenderer.renderBlock(block, pos, world, this.matrixStack, this.blockRenderTarget, cullHiddenFaces, this.random);
		this.blockRenderTarget.writeQuads(name, this.objWriter, this.cache);
		this.matrixStack.pop();
	}

	private void writeFluid(World world, BlockPos pos, BlockState block) throws IOException {
		FluidState fluid = block.getFluidState();
		if (fluid.isEmpty()) return;

		String name = block.getBlock().getName().asString();
		if (block.getBlock() != Blocks.WATER && block.getBlock() != Blocks.LAVA) {
			name += "_waterlogged";
		}

		this.matrixStack.push();
		this.fluidRenderTarget.begin(pos);
		blockRenderer.renderFluid(pos, world, this.fluidRenderTarget, fluid);
		this.fluidRenderTarget.writeQuads(name, this.objWriter, this.cache);
		this.matrixStack.pop();
	}
}
