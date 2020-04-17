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

public class BlocksWriter {
	private final ObjWriter objWriter;

	public BlocksWriter(String basename) throws IOException {
		this.objWriter = new ObjWriter(basename + ".obj");
	}

	public void close() throws IOException {
		this.objWriter.close();
	}

	public void writeRegion(BlockView view, BlockPos a, BlockPos b) throws IOException {
		BlockModels models = MinecraftClient.getInstance().getBakedModelManager().getBlockModels();
		Random random = new Random();
		Map<Vec3d,Integer> vertexCache = new HashMap<>();

		for (Direction dir : Direction.values()) {
			this.objWriter.writeVertexNormal(new Vec3d(dir.getVector()));
		}

		for (BlockPos pos : BlockPos.iterate(a, b)) {
			BlockState block = view.getBlockState(pos);
			BakedModel model = models.getModel(block);

			this.objWriter.beginObject(block.getBlock().getName().asString());
			for (Direction dir : Direction.values()) {
				for (BakedQuad quad : model.getQuads(block, dir, random)) {
					int[] vertices = new int[4];
					int i = 0;

					for (Vec3d vertex : BlocksWriter.decodeVertices(quad.getVertexData())) {
						Integer vertexIndex = vertexCache.putIfAbsent(vertex, vertexCache.size());
						if (vertexIndex == null) {
							this.objWriter.writeVertex(vertex);
							vertices[i++] = vertexCache.size();
						} else {
							vertices[i++] = vertexIndex;
						}
					}

					this.objWriter.writeFace(dir.getId(), vertices);
				}
			}

			vertexCache.clear();
		}
	}

	private static Vec3d[] decodeVertices(int[] vertexData) {
		final double SCALE_FACTOR = 1/16.0;
		Vec3d[] vertices = new Vec3d[vertexData.length/3];

		for (int i = 0; i < vertices.length; i++) {
			double x = vertexData[i*3 + 0] * SCALE_FACTOR;
			double y = vertexData[i*3 + 1] * SCALE_FACTOR;
			double z = vertexData[i*3 + 2] * SCALE_FACTOR;
			vertices[i] = new Vec3d(x, y, z);
		}

		return vertices;
	}
}
