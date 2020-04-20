// vim: noet

package vktec.geomexport;

import net.minecraft.client.texture.Sprite;
import net.minecraft.util.math.Vec2f;

public class UV extends Vec2f {
	public UV(float u, float v) {
		super(u, v);
	}

	public static UV spriteUV(Sprite sprite, UV uv) {
		return spriteUV(sprite, uv.x, uv.y);
	}

	public static UV spriteUV(Sprite sprite, float u, float v) {
		float uDiff = sprite.getMaxU() - sprite.getMinU();
		float vDiff = sprite.getMaxV() - sprite.getMinV();
		u = (u - sprite.getMinU()) / uDiff;
		v = (v - sprite.getMinV()) / vDiff;
		return new UV(u, (1-v) / sprite.getFrameCount());
	}
}
