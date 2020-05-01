// vim: noet

package vktec.geomexport;

import java.io.IOException;
import java.nio.file.Path;
import net.minecraft.util.math.Vec2f;
import net.minecraft.util.math.Vec3d;

public class ObjWriter extends WavefrontWriter {
	public ObjWriter(Path path) throws IOException { super(path); }

	public void beginObject(String name) throws IOException {
		this.write("o", name);
	}

	public void beginGroup(String name) throws IOException {
		this.write("g", name);
	}

	public void writeVertex(Vec3d vertex) throws IOException {
		this.writefln("v %f %f %f", vertex.x, vertex.y, vertex.z);
	}

	public void writeTextureCoord(Vec2f coord) throws IOException {
		this.writefln("vt %f %f", coord.x, coord.y);
	}

	public void writeVertexNormal(Vec3d vertexNormal) throws IOException {
		this.writefln("vn %f %f %f", vertexNormal.x, vertexNormal.y, vertexNormal.z);
	}

	public void writeFace(int[] vertexIndices, int[] uvIndices, int[] normalIndices) throws IOException {
		this.file.write("f");
		for (int i = 0; i < vertexIndices.length; i++) {
			int vert = vertexIndices[i] + 1;
			int uv = uvIndices[i] + 1;
			int normal = normalIndices[i] + 1;
			this.writef(" %d/%d/%d", vert, uv, normal);
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
