package vktec.geomexport;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.FileSystems;
import net.minecraft.util.math.Vec3d;

public class ObjWriter {
	private final BufferedWriter file;

	public ObjWriter(String path) throws IOException {
		this.file = Files.newBufferedWriter(FileSystems.getDefault().getPath(path), Charset.forName("utf-8"));
	}

	public void close() throws IOException {
		this.file.close();
	}

	private void write(String... fields) throws IOException {
		boolean first = true;
		for (String field : fields) {
			if (first) first = false;
			else this.file.write(' ');

			this.file.write(field);
		}
		this.file.newLine();
	}

	private void writef(String format, Object... values) throws IOException {
		this.file.write(String.format(format, values));
	}

	private void writefln(String format, Object... values) throws IOException {
		this.writef(format, values);
		this.file.newLine();
	}

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

	public void writeFace(int normalVertex, int... vertexIndices) throws IOException {
		this.file.write("f ");
		for (int idx : vertexIndices) {
			this.writef("%d//%d", idx, normalVertex);
		}
		this.file.newLine();
	}
}
