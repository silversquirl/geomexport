// vim: noet

package vktec.geomexport;

import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.util.Random;
import java.util.Map;
import java.util.HashMap;
import net.minecraft.block.BlockState;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.block.BlockModels;
import net.minecraft.client.render.model.BakedModel;
import net.minecraft.client.render.model.BakedQuad;
import net.minecraft.client.texture.NativeImage;
import net.minecraft.client.texture.Sprite;
import net.minecraft.world.BlockView;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;
import vktec.geomexport.duck.SpriteDuck;

public class BlocksWriter implements AutoCloseable {
	private final MtlWriter mtlWriter;
	private final ObjWriter objWriter;
	private final Path textureDir;

	private static final Direction[] directions = new Direction[Direction.values().length + 1];
	static {
		directions[0] = null;
		System.arraycopy(Direction.values(), 0, directions, 1, Direction.values().length);
	}

	public BlocksWriter(String basename) throws IOException {
		this.mtlWriter = new MtlWriter(basename + ".mtl");
		this.objWriter = new ObjWriter(basename + ".obj");
		this.objWriter.addMtl(basename + ".mtl");
		this.textureDir = FileSystems.getDefault().getPath(basename + "_textures");
	}

	public void close() throws IOException {
		this.objWriter.close();
	}

	public void writeRegion(BlockView view, BlockPos a, BlockPos b) throws IOException {
		BlockModels models = MinecraftClient.getInstance().getBakedModelManager().getBlockModels();
		Random random = new Random();

		Map<String,Integer> vertexCache = new HashMap<>();
		Map<String,Integer> normalCache = new HashMap<>();
		Map<Identifier,String> materialCache = new HashMap<>();

		Vec3d vecOrigin = new Vec3d(a.getX(), a.getY(), a.getZ());

		for (BlockPos pos : BlockPos.iterate(a, b)) {
			BlockState block = view.getBlockState(pos);
			BakedModel model = models.getModel(block);

			Vec3d relPos = new Vec3d(pos.getX(), pos.getY(), pos.getZ()).subtract(vecOrigin);

			boolean writtenObj = false;

			for (Direction dir : BlocksWriter.directions) {
				Vec3d normal = null;
				if (dir != null) normal = new Vec3d(dir.getVector());

				for (BakedQuad quad : model.getQuads(block, dir, random)) {
					// Prevent writing the object if there's no quads in it
					if (!writtenObj) {
						this.objWriter.beginObject(block.getBlock().getName().asString());
						writtenObj = true;
					}

					// Material data
					Sprite sprite = model.getSprite();
					String materialName = materialCache.get(sprite.getId());
					if (materialName == null) {
						materialName = sprite.getId().toString();

						NativeImage texture = ((SpriteDuck)sprite).getImages()[0];
						Path texturePath = this.textureDir.resolve(materialName + ".png");
						texture.writeFile(texturePath);

						this.mtlWriter.beginMaterial(materialName);
						this.mtlWriter.writeAmbientTexture(texturePath.toString());

						materialCache.put(sprite.getId(), materialName);
					}

					// Vertex data
					Vec3d[] vertices = BlocksWriter.decodeVertices(quad.getVertexData());
					int[] vertexIndices = new int[vertices.length];
					int i = 0;

					if (dir == null) {
						normal = calcNormal(vertices[0], vertices[1], vertices[2], vertices[3]);
					}

					String normalStr = String.format("%f %f %f", normal.x, normal.y, normal.z);

					Integer normalIndex = normalCache.putIfAbsent(normalStr, normalCache.size());
					if (normalIndex == null) {
						this.objWriter.writeVertexNormal(normal);
						normalIndex = normalCache.size() - 1;
					}

					for (Vec3d vertex : vertices) {
						Vec3d relVertex = vertex.add(relPos);

						String vertexStr = String.format("%f %f %f", relVertex.x, relVertex.y, relVertex.z);

						Integer vertexIndex = vertexCache.putIfAbsent(vertexStr, vertexCache.size());
						if (vertexIndex == null) {
							this.objWriter.writeVertex(relVertex);
							vertexIndices[i++] = vertexCache.size() - 1;
						} else {
							vertexIndices[i++] = vertexIndex;
						}
					}

					this.objWriter.useMtl(materialName);
					this.objWriter.writeFaceNormal(normalIndex, vertexIndices);
				}
			}
		}
	}

	private static Vec3d[] decodeVertices(int[] vertexData) {
		Vec3d[] vertices = new Vec3d[vertexData.length/8];

		for (int i = 0; i < vertices.length; i++) {
			float x = Float.intBitsToFloat(vertexData[i*8 + 0]);
			float y = Float.intBitsToFloat(vertexData[i*8 + 1]);
			float z = Float.intBitsToFloat(vertexData[i*8 + 2]);
			int brightness = vertexData[i*8 + 3];
			float u = Float.intBitsToFloat(vertexData[i*8 + 4]);
			float v = Float.intBitsToFloat(vertexData[i*8 + 5]);
			vertices[i] = new Vec3d(x, y, z);
		}

		return vertices;
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
