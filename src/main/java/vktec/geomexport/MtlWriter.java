// vim: noet

package vktec.geomexport;

import java.io.IOException;
import java.nio.file.Path;

public class MtlWriter extends WavefrontWriter {
	public MtlWriter(Path path) throws IOException { super(path); }

	public void beginMaterial(String name) throws IOException {
		this.write("newmtl", name);
	}

	public void writeDiffuseColor(int rgb) throws IOException {
		float r = (rgb >> 16 & 0xff) / 255.0f;
		float g = (rgb >> 8 & 0xff) / 255.0f;
		float b = (rgb & 0xff) / 255.0f;
		this.writeDiffuseColor(r, g, b);
	}

	public void writeDiffuseColor(float r, float g, float b) throws IOException {
		this.writefln("Kd %f %f %f", r, g, b);
	}

	public void writeDiffuseTexture(String filename) throws IOException {
		this.write("map_Kd", filename);
	}
}
