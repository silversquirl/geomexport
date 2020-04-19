// vim: noet

package vktec.geomexport;

import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import net.minecraft.block.BlockState;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.color.block.BlockColors;
import net.minecraft.client.render.block.BlockModels;
import net.minecraft.client.render.model.BakedModel;
import net.minecraft.client.render.model.BakedQuad;
import net.minecraft.client.texture.NativeImage;
import net.minecraft.client.texture.Sprite;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec2f;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import vktec.geomexport.duck.BakedQuadDuck;
import vktec.geomexport.duck.SpriteDuck;

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

	public void writeRegion(World world, BlockPos a, BlockPos b) throws IOException {
		BlockModels models = MinecraftClient.getInstance().getBakedModelManager().getBlockModels();
		BlockColors colorMap = MinecraftClient.getInstance().getBlockColorMap();
		Random random = new Random();

		Map<String,Integer> vertexCache = new HashMap<>();
		Map<String,Integer> normalCache = new HashMap<>();
		Map<String,Integer> uvCache = new HashMap<>();
		Map<String,ObjQuad> quadCache = new HashMap<>();
		Map<String,Material> materialCache = new HashMap<>();

		Vec3d vecOrigin = new Vec3d(a.getX(), a.getY(), a.getZ());

		Vec3d[] vertices = new Vec3d[4];
		Vec2f[] uvs = new Vec2f[vertices.length];
		int[] vertexIndices = new int[vertices.length];
		int[] uvIndices = new int[vertices.length];

		for (BlockPos pos : BlockPos.iterate(a, b)) {
			BlockState block = world.getBlockState(pos);
			BakedModel model = models.getModel(block);
			int biomeTintColor = colorMap.getColor(block, world, pos);

			Vec3d relPos = new Vec3d(pos.getX(), pos.getY(), pos.getZ()).subtract(vecOrigin);

			for (Direction dir : BlocksWriter.directions) {
				Vec3d normal = null;
				if (dir != null) normal = new Vec3d(dir.getVector());

				for (BakedQuad quad : model.getQuads(block, dir, random)) {
					// Material data
					Sprite sprite = ((BakedQuadDuck)quad).getSprite();
					String materialName;
					if (quad.hasColor()) {
						materialName = Material.genName(sprite, biomeTintColor);
					} else {
						materialName = Material.genName(sprite);
					}

					Material mat = materialCache.get(materialName);
					if (mat == null) {
						if (quad.hasColor()) {
							mat = new Material(materialName, sprite, biomeTintColor);
						} else {
							mat = new Material(materialName, sprite);
						}
						materialCache.put(materialName, mat);
					} else {
						mat.refCount++;
					}

					// Vertex data
					BlocksWriter.decodeVertices(vertices, uvs, quad.getVertexData(), sprite);

					if (dir == null) {
						normal = calcNormal(vertices[0], vertices[1], vertices[2], vertices[3]);
					}

					String normalStr = String.format("%f %f %f", normal.x, normal.y, normal.z);

					Integer normalIndex = normalCache.putIfAbsent(normalStr, normalCache.size());
					if (normalIndex == null) {
						this.objWriter.writeVertexNormal(normal);
						normalIndex = normalCache.size() - 1;
					}

					int i = 0;
					for (Vec3d vertex : vertices) {
						Vec3d relVertex = vertex.add(relPos);

						String vertexStr = String.format("%f %f %f", relVertex.x, relVertex.y, relVertex.z);

						Integer vertexIndex = vertexCache.putIfAbsent(vertexStr, vertexCache.size());
						if (vertexIndex == null) {
							this.objWriter.writeVertex(relVertex);
							vertexIndices[i] = vertexCache.size() - 1;
						} else {
							vertexIndices[i] = vertexIndex;
						}
						i++;
					}

					i = 0;
					for (Vec2f uv : uvs) {
						String uvStr = String.format("%f %f", uv.x, uv.y);

						Integer uvIndex = uvCache.putIfAbsent(uvStr, uvCache.size());
						if (uvIndex == null) {
							this.objWriter.writeTextureCoord(uv);
							uvIndices[i] = uvCache.size() - 1;
						} else {
							uvIndices[i] = uvIndex;
						}
						i++;
					}

					ObjQuad outQuad = new ObjQuad(mat, vertexIndices, uvIndices, normalIndex);
					String oldId = outQuad.getIdentifier();
					outQuad = quadCache.putIfAbsent(outQuad.getIdentifier(), outQuad);
					if (outQuad != null) {
						// Deref the old materials
						outQuad.material.refCount--;
						mat.refCount--;

						String newMatName = outQuad.material.name + "+" + mat.name;
						Material newMat = materialCache.get(newMatName);
						if (newMat == null) {
							// Merge the quad textures to create the new material
							NativeImage newTex = ImageMixer.composite(outQuad.material.texture, mat.texture);
							newMat = new Material(newMatName, newTex);
						} else {
							newMat.refCount++;
						}
						outQuad.material = newMat;
					}
				}
			}

			System.out.printf("%d\n", quadCache.size());
			if (quadCache.size() > 0) {
				this.objWriter.beginObject(block.getBlock().getName().asString());

				for (ObjQuad outQuad : quadCache.values()) {
					outQuad.write(this.objWriter);
				}
				quadCache.clear();
			}
		}

		for (Material mat : materialCache.values()) {
			if (mat.refCount > 0) {
				mat.write(this.mtlWriter, this.dir, this.textureDir);
			}
		}
	}

	private static void decodeVertices(Vec3d[] vertices, Vec2f[] uvs, int[] vertexData, Sprite sprite) {
		for (int i = 0; i < vertices.length; i++) {
			float x = Float.intBitsToFloat(vertexData[i*8 + 0]);
			float y = Float.intBitsToFloat(vertexData[i*8 + 1]);
			float z = Float.intBitsToFloat(vertexData[i*8 + 2]);
			vertices[i] = new Vec3d(x, y, z);

			float u = Float.intBitsToFloat(vertexData[i*8 + 4]);
			float v = Float.intBitsToFloat(vertexData[i*8 + 5]);
			float uDiff = sprite.getMaxU() - sprite.getMinU();
			float vDiff = sprite.getMaxV() - sprite.getMinV();
			u = (u - sprite.getMinU()) / uDiff;
			v = (v - sprite.getMinV()) / vDiff;
			uvs[i] = new Vec2f(u, 1-v);
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
