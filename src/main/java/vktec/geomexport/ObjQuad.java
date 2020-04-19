// vim: noet

package vktec.geomexport;

import java.io.IOException;
import java.nio.file.Path;

public class ObjQuad {
	public Material material;
	public final int[] vertexIndices;
	public final int[] uvIndices;
	public final int normalIndex;
	public final String identifier;

	public ObjQuad(Material material, int[] vertexIndices, int[] uvIndices, int normalIndex) {
		this.material = material;
		this.vertexIndices = vertexIndices;
		this.uvIndices = uvIndices;
		this.normalIndex = normalIndex;

		StringBuilder identifierBuilder = new StringBuilder();
		for (int vert : vertexIndices) {
			identifierBuilder.append(vert);
			identifierBuilder.append(' ');
		}
		identifierBuilder.append(normalIndex);
		this.identifier = identifierBuilder.toString();
	}

	public String getIdentifier() {
		return this.identifier;
	}

	public void write(ObjWriter w) throws IOException {
		w.useMtl(this.material.name);
		w.writeFace(this.vertexIndices, this.uvIndices, this.normalIndex);
	}
}
