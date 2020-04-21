// vim: noet

package vktec.geomexport;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.texture.Sprite;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;

public abstract class RenderTarget implements VertexConsumer {
	protected final BlockPos origin;
	protected Vec3d transpose;
	protected final ObjCaches cache;

	private Vec3d[] vertexBuffer;
	private UV[] uvBuffer;
	private Vec3d[] normalBuffer;
	private int vertexCount;
	private int tint;

	private final List<Sprite> sprites;

	public RenderTarget(BlockPos origin, ObjCaches cache) {
		this.origin = origin;
		this.cache = cache;
		this.sprites = this.initSprites();
		this.resetBuffers();
	}

	protected abstract List<Sprite> initSprites();

	protected void resetBuffers() {
		this.vertexBuffer = new Vec3d[4];
		this.uvBuffer = new UV[4];
		this.vertexCount = 0;
	}

	public void begin(BlockPos pos) {
		this.transpose = new Vec3d(pos.subtract(this.origin));
	}

	protected abstract void addQuad(Quad quad);
	protected abstract Iterable<Quad> getQuads();
	protected abstract void clearQuads();

	public void writeQuads(String name, ObjWriter objWriter, ObjCaches cache) throws IOException {
		boolean begun = false;
		for (Quad outQuad : this.getQuads()) {
			if (!begun) {
				objWriter.beginObject(name);
				begun = true;
			}

			outQuad.write(objWriter, cache);
		}
		this.clearQuads();
	}

	public VertexConsumer vertex(double x, double y, double z) {
		this.vertexBuffer[vertexCount] = new Vec3d(x, y, z).add(this.transpose);
		return this;
	}

	public VertexConsumer color(int r, int g, int b, int a) {
		this.tint = r<<16 | g<<8 | b;
		return this;
	}

	public VertexConsumer texture(float u, float v) {
		this.uvBuffer[vertexCount] = new UV(u, v);
		return this;
	}

	public VertexConsumer normal(float x, float y, float z) {
		if (this.normal == null) {
			this.normal = new Vec3d(x, y, z);
		}
		return this;
	}

	// Check if a point vec is within the rect from (rx1, ry1) to (rx2, ry2)
	// Argument constraints: rx1 <= rx2, ry1 <= ry2
	private static boolean inRect(UV vec, float rx1, float ry1, float rx2, float ry2) {
		if (vec.x < rx1 || vec.x > rx2) return false;
		if (vec.y < ry1 || vec.y > ry2) return false;
		return true;
	}

	public void next() {
		if (++vertexCount >= 4) {
			Sprite sprite = null;
			spriteFinder: for (Sprite search : this.sprites) {
				for (UV uv : this.uvBuffer) {
					if (!inRect(uv, search.getMinU(), search.getMinV(), search.getMaxU(), search.getMaxV())) {
						continue spriteFinder;
					}
				}
				sprite = search;
				break;
			}

			if (sprite == null) {
				// wtf
				// TODO: logging for this very very rare occurrence
				return;
			}

			for (int i = 0; i < this.uvBuffer.length; i++) {
				this.uvBuffer[i] = UV.spriteUV(sprite, this.uvBuffer[i]);
			}

			Material mat = Material.create(sprite, this.tint, this.cache);
			Quad quad = new Quad(mat, this.vertexBuffer, this.uvBuffer, this.normal);
			this.addQuad(quad);

			this.resetBuffers();
		}
	}

	// Don't know, don't care
	public VertexConsumer overlay(int a, int b) { return this; }
	public VertexConsumer light(int a, int b) { return this; }
}
