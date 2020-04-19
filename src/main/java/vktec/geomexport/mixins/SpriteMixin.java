// vim: noet

package vktec.geomexport.mixins;

import net.minecraft.client.texture.NativeImage;
import net.minecraft.client.texture.Sprite;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import vktec.geomexport.duck.SpriteDuck;

@Mixin(Sprite.class)
public abstract class SpriteMixin implements SpriteDuck {
	@Shadow protected @Final NativeImage[] images;

	public NativeImage[] getImages() {
		return this.images;
	}
}
