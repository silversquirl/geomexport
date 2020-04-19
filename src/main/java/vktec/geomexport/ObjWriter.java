// vim: noet

package vktec.geomexport;

import java.io.IOException;
import net.minecraft.util.math.Vec3d;

public class ObjWriter extends WavefrontWriter {
	public ObjWriter(String path) throws IOException { super(path); }

	public void beginObject(String name) throws IOException {
		this.write("o", name);
	}

	public void beginGroup(String name) throws IOException {
		this.write("g", name);
	}

	public void writeVertex(Vec3d vertex) throws IOException {
		this.writefln("v %f %f %f", vertex.getX(), vertex.getY(), vertex.getZ());
	}

	public void writeVertexNormal(Vec3d vertexNormal) throws IOException {
		this.writefln("vn %f %f %f", vertexNormal.getX(), vertexNormal.getY(), vertexNormal.getZ());
	}

	public void writeFace(int... vertexIndices) throws IOException {
		this.file.write("f");
		for (int idx : vertexIndices) {
			this.writef(" %d", idx + 1);
		}
		this.file.newLine();
	}

	public void writeFaceNormal(int normalVertex, int... vertexIndices) throws IOException {
		this.file.write("f");
		for (int idx : vertexIndices) {
			this.writef(" %d//%d", idx + 1, normalVertex + 1);
		}
		this.file.newLine();
	}

	public void addMtl(String filename) throws IOException {
		this.write("mtllib", filename);
	}

	public void useMtl(String materialName) throws IOException {
		this.write("usemtl", materialName);
	}
}
