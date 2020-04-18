// vim: noet

package vktec.geomexport;

import java.io.IOException;
import java.util.Random;
import java.util.Map;
import java.util.HashMap;
import net.minecraft.block.BlockState;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.block.BlockModels;
import net.minecraft.client.render.model.BakedModel;
import net.minecraft.client.render.model.BakedQuad;
import net.minecraft.world.BlockView;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;

public class BlocksWriter implements AutoCloseable {
	private final ObjWriter objWriter;
	private final Direction[] directions;

	public BlocksWriter(String basename) throws IOException {
		this.objWriter = new ObjWriter(basename + ".obj");
		directions = new Direction[Direction.values().length + 1];
		directions[0] = null;
		System.arraycopy(Direction.values(), 0, directions, 1, Direction.values().length);
	}

	public void close() throws IOException {
		this.objWriter.close();
	}

	public void writeRegion(BlockView view, BlockPos a, BlockPos b) throws IOException {
		BlockModels models = MinecraftClient.getInstance().getBakedModelManager().getBlockModels();
		Random random = new Random();
		Map<String,Integer> vertexCache = new HashMap<>();

		for (Direction dir : Direction.values()) {
			this.objWriter.writeVertexNormal(new Vec3d(dir.getVector()));
		}

		Vec3d vecOrigin = new Vec3d(a.getX(), a.getY(), a.getZ());

		for (BlockPos pos : BlockPos.iterate(a, b)) {
			BlockState block = view.getBlockState(pos);
			BakedModel model = models.getModel(block);

			Vec3d relPos = new Vec3d(pos.getX(), pos.getY(), pos.getZ()).subtract(vecOrigin);

			boolean writtenObj = false;

			for (Direction dir : directions) {
				for (BakedQuad quad : model.getQuads(block, dir, random)) {
					// Prevent writing the object if there's no quads in it
					if (!writtenObj) {
						this.objWriter.beginObject(block.getBlock().getName().asString());
						writtenObj = true;
					}

					Vec3d[] vertices = BlocksWriter.decodeVertices(quad.getVertexData());
					int[] vertexIndices = new int[vertices.length];
					int i = 0;

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

					if (dir == null) this.objWriter.writeFace(vertexIndices);
					else this.objWriter.writeFaceNormal(dir.getId(), vertexIndices);
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
}
