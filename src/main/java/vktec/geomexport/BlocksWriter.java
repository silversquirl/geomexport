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
import net.minecraft.fluid.FluidState;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec2f;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import vktec.geomexport.duck.BakedQuadDuck;
import vktec.geomexport.duck.SpriteDuck;
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
	private FluidRenderTarget fluidRenderTarget;

	private BlockPos origin;

	public void writeRegion(World world, BlockPos a, BlockPos b) throws IOException {
		this.origin = a;
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

	private int getBiomeTint(World world, BlockPos pos, BlockState block) {
		int tint = colorMap.getColor(block, world, pos, 0);
		if (tint >= 0) return tint;

		MaterialColor color = block.getTopMaterialColor(world, pos);
		if (color == null) return -1;
		return color.color;
	}

	private void writeBlock(World world, BlockPos pos, BlockState block) throws IOException {
		BakedModel model = models.getModel(block);
		Vec3d relPos = new Vec3d(pos.subtract(this.origin));
		int biomeTintColor = this.getBiomeTint(world, pos, block);

		for (Direction dir : BlocksWriter.directions) {
			Vec3d normal = null;
			if (dir != null) normal = new Vec3d(dir.getVector());

			for (BakedQuad quad : model.getQuads(block, dir, random)) {
				Vec3d[] vertices = new Vec3d[4];
				Vec2f[] uvs = new Vec2f[vertices.length];

				// Material data
				Sprite sprite = ((BakedQuadDuck)quad).getSprite();
				Material mat;
				if (quad.hasColor() && biomeTintColor >= 0) {
					mat = Material.create(sprite, biomeTintColor, this.cache);
				} else {
					mat = Material.create(sprite, this.cache);
				}

				// Vertex data
				BlocksWriter.decodeVertices(vertices, uvs, quad.getVertexData(), sprite);
				for (int i = 0; i < vertices.length; i++) {
					vertices[i] = vertices[i].add(relPos);
				}

				if (dir == null) {
					normal = calcNormal(vertices[0], vertices[1], vertices[2], vertices[3]);
				}

				Quad outQuad = new Quad(mat, vertices, uvs, normal);
				outQuad = cache.quad.putIfAbsent(outQuad.getIdentifier(), outQuad);
				if (outQuad != null) {
					// Deref the old materials
					outQuad.material.refCount--;
					mat.refCount--;

					String newMatName = outQuad.material.name + "+" + mat.name;
					Material newMat = cache.material.get(newMatName);
					if (newMat == null) {
						// Merge the quad textures to create the new material
						NativeImage newTex = ImageMixer.composite(outQuad.material.texture, mat.texture);
						newMat = new Material(newMatName, newTex);
						cache.material.put(newMatName, newMat);
					} else {
						newMat.refCount++;
					}
					outQuad.material = newMat;
				}
			}
		}

		if (cache.quad.size() > 0) {
			this.objWriter.beginObject(block.getBlock().getName().asString());

			for (Quad outQuad : cache.quad.values()) {
				outQuad.write(this.objWriter, this.cache);
			}
			cache.quad.clear();
		}
	}

	private void writeFluid(World world, BlockPos pos, BlockState block) throws IOException {
		FluidState fluid = block.getFluidState();
		if (fluid.isEmpty()) return;

		String name = block.getBlock().getName().asString();
		if (block.getBlock() != Blocks.WATER && block.getBlock() != Blocks.LAVA) {
			name += "_waterlogged";
		}

		this.fluidRenderTarget.beginFluid(pos);
		blockRenderer.renderFluid(pos, world, this.fluidRenderTarget, fluid);
		this.fluidRenderTarget.writeQuads(name, this.objWriter, this.cache);
	}

	private static void decodeVertices(Vec3d[] vertices, Vec2f[] uvs, int[] vertexData, Sprite sprite) {
		for (int i = 0; i < vertices.length; i++) {
			float x = Float.intBitsToFloat(vertexData[i*8 + 0]);
			float y = Float.intBitsToFloat(vertexData[i*8 + 1]);
			float z = Float.intBitsToFloat(vertexData[i*8 + 2]);
			vertices[i] = new Vec3d(x, y, z);

			float u = Float.intBitsToFloat(vertexData[i*8 + 4]);
			float v = Float.intBitsToFloat(vertexData[i*8 + 5]);
			uvs[i] = UV.spriteUV(sprite, u, v);
		}
	}

	private static Vec3d calcNormal(Vec3d a, Vec3d b, Vec3d c, Vec3d d) {
		// Assuming abcd is a quad (i.e. all on one plane), we can ignore
		// one of the coordinates and instead just find the surface normal
		// of a triangle abc. Hence, we completely ignore d

		Vec3d u = b.subtract(a);
		Vec3d v = c.subtract(a);

		double x = (u.y * v.z) - (u.z * v.y);
		double y = (u.z * v.x) - (u.x * v.z);
		double z = (u.x * v.y) - (u.y * v.x);

		return new Vec3d(x, y, z);
	}
}
