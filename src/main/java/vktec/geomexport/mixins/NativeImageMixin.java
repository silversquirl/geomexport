// vim: noet

package vktec.geomexport.mixins;

import java.nio.ByteBuffer;
import net.minecraft.client.texture.NativeImage;
import org.lwjgl.system.MemoryUtil;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import vktec.geomexport.duck.NativeImageDuck;

@Mixin(NativeImage.class)
public abstract class NativeImageMixin implements NativeImageDuck {
	@Shadow private long pointer;
	@Shadow private @Final long sizeBytes;

	public ByteBuffer getDataBuffer() {
		return MemoryUtil.memByteBufferSafe(this.pointer, (int)this.sizeBytes);
	}
}
