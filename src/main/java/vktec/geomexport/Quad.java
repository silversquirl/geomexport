// vim: noet

package vktec.geomexport;

import java.io.IOException;
import java.nio.file.Path;
import net.minecraft.util.math.Vec2f;
import net.minecraft.util.math.Vec3d;

public class Quad {
	public Material material;
	public final Vec3d[] vertices;
	public final Vec2f[] uvs;
	public final Vec3d[] normals;
	public final String identifier;

	public Quad(Material material, Vec3d[] vertices, Vec2f[] uvs, Vec3d[] normals) {
		this.material = material;
		this.vertices = vertices;
		this.uvs = uvs;
		this.normals = normals;

		StringBuilder identifierBuilder = new StringBuilder();
		boolean first = true;
		for (Vec3d vertex : this.vertices) {
			if (first) first = false;
			else identifierBuilder.append(' ');
			identifierBuilder.append(strVec(vertex));
		}
		for (Vec3d normal : this.normals) {
			identifierBuilder.append(' ');
			identifierBuilder.append(strVec(normal));
		}
		this.identifier = identifierBuilder.toString();
	}

	private static String strVec(Vec3d vec) {
		return String.format("%f %f %f", vec.x, vec.y, vec.z);
	}

	private static String strVec(Vec2f vec) {
		return String.format("%f %f", vec.x, vec.y);
	}

	public String getIdentifier() {
		return this.identifier;
	}

	public void write(ObjWriter w, ObjCaches cache) throws IOException {
		int[] vertexIndices = new int[this.vertices.length];
		int[] uvIndices = new int[this.uvs.length];
		int[] normalIndices = new int[this.normals.length];

		int i = 0;
		for (Vec3d vertex : this.vertices) {
			Integer vertexIndex = cache.vertex.putIfAbsent(strVec(vertex), cache.vertex.size());
			if (vertexIndex == null) {
				w.writeVertex(vertex);
				vertexIndices[i] = cache.vertex.size() - 1;
			} else {
				vertexIndices[i] = vertexIndex;
			}
			i++;
		}

		i = 0;
		for (Vec3d normal : this.normals) {
			Integer normalIndex = cache.normal.putIfAbsent(strVec(normal), cache.normal.size());
			if (normalIndex == null) {
				w.writeVertexNormal(normal);
				normalIndices[i] = cache.normal.size() - 1;
			} else {
				normalIndices[i] = normalIndex;
			}
			i++;
		}

		i = 0;
		for (Vec2f uv : this.uvs) {
			Integer uvIndex = cache.uv.putIfAbsent(strVec(uv), cache.uv.size());
			if (uvIndex == null) {
				w.writeTextureCoord(uv);
				uvIndices[i] = cache.uv.size() - 1;
			} else {
				uvIndices[i] = uvIndex;
			}
			i++;
		}

		w.useMtl(this.material.name);
		w.writeFace(vertexIndices, uvIndices, normalIndices);
	}
}
