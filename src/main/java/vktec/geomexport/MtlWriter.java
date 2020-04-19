// vim: noet

package vktec.geomexport;

import java.io.IOException;
import java.nio.file.Path;

public class MtlWriter extends WavefrontWriter {
	public MtlWriter(Path path) throws IOException { super(path); }

	public void beginMaterial(String name) throws IOException {
		this.write("newmtl", name);
	}

	public void writeDiffuseTexture(String filename) throws IOException {
		this.write("map_Kd", filename);
	}
}
