// vim: noet

package vktec.geomexport;
import java.io.IOException;

public class MtlWriter extends WavefrontWriter {
	public MtlWriter(String path) throws IOException { super(path); }

	public void beginMaterial(String name) throws IOException {
		this.write("newmtl", name);
	}

	public void writeAmbientTexture(String filename) throws IOException {
		this.write("map_Ka", filename);
	}
}
