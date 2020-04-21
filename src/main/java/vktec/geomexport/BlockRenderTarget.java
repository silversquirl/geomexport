// vim: noet

package vktec.geomexport;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.model.SpriteAtlasManager;
import net.minecraft.client.texture.Sprite;
import net.minecraft.client.texture.NativeImage;
import net.minecraft.util.math.BlockPos;

public class BlockRenderTarget extends RenderTarget {
	public BlockRenderTarget(BlockPos origin, ObjCaches cache) {
		super(origin, cache);
	}

	protected List<Sprite> initSprites() {
		SpriteAtlasManager atlasManager = MinecraftClient.getInstance().getBakedModelManager().atlasManager;
		return atlasManager
			.atlases.values().stream()
			.flatMap(atlas -> atlas.sprites.values().stream())
			.collect(Collectors.toList());
	}

	private final Map<String,Quad> quads = new HashMap<>();

	protected void addQuad(Quad newQuad) {
		Quad quad = this.quads.putIfAbsent(newQuad.getIdentifier(), newQuad);
		if (quad != null) {
			// Deref the old materials
			quad.material.refCount--;
			newQuad.material.refCount--;

			String newMatName = quad.material.name + "+" + newQuad.material.name;
			Material newMat = this.cache.material.get(newMatName);
			if (newMat == null) {
				// Merge the quad textures to create the new material
				NativeImage newTex = ImageMixer.composite(quad.material.texture, newQuad.material.texture);
				newMat = new Material(newMatName, newTex);
				this.cache.material.put(newMatName, newMat);
			} else {
				newMat.refCount++;
			}
			quad.material = newMat;
		}
	}

	protected Iterable<Quad> getQuads() {
		return this.quads.values();
	}

	protected void clearQuads() {
		this.quads.clear();
	}
}
