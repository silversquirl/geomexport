// vim: noet

package vktec.geomexport;

import java.io.IOException;
import java.nio.file.Path;
import net.minecraft.client.texture.NativeImage;
import net.minecraft.client.texture.Sprite;
import vktec.geomexport.duck.SpriteDuck;

public class Material {
	public static String genName(Sprite sprite) {
		return sprite.getId().toString();
	}

	public static String genName(Sprite sprite, int biomeTint) {
		return sprite.getId().toString() + String.format("#%X", biomeTint);
	}

	public final String name;
	public final NativeImage texture;
	public int refCount = 1;

	public Material(String name, Sprite sprite) {
		this(name, ((SpriteDuck)sprite).getImages()[0]);
	}

	public Material(String name, Sprite sprite, int biomeTint) {
		this(name, ImageMixer.tint(((SpriteDuck)sprite).getImages()[0], biomeTint));
	}

	public Material(String name, NativeImage texture) {
		this.name = name;
		this.texture = texture;
	}

	public void write(MtlWriter w, Path baseDir, Path textureDir) throws IOException {
		Path texturePath = textureDir.resolve(this.name + ".png");
		texturePath.getParent().toFile().mkdirs();
		texture.writeFile(texturePath);

		w.beginMaterial(this.name);
		w.writeDiffuseTexture(baseDir.relativize(texturePath).toString());
	}

	public static Material create(Sprite sprite, int biomeTint, ObjCaches cache) {
		String name = Material.genName(sprite, biomeTint);
		Material mat = cache.material.get(name);
		if (mat == null) {
			mat = new Material(name, sprite, biomeTint);
			cache.material.put(name, mat);
		} else {
			mat.refCount++;
		}
		return mat;
	}

	public static Material create(Sprite sprite, ObjCaches cache) {
		String name = Material.genName(sprite);
		Material mat = cache.material.get(name);
		if (mat == null) {
			mat = new Material(name, sprite);
			cache.material.put(name, mat);
		} else {
			mat.refCount++;
		}
		return mat;
	}
}
