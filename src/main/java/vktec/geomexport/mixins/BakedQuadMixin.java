// vim: noet

package vktec.geomexport.mixins;

import net.minecraft.client.render.model.BakedQuad;
import net.minecraft.client.texture.Sprite;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import vktec.geomexport.duck.BakedQuadDuck;

@Mixin(BakedQuad.class)
public abstract class BakedQuadMixin implements BakedQuadDuck {
	@Shadow protected @Final Sprite sprite;

	public Sprite getSprite() {
		return this.sprite;
	}
}
