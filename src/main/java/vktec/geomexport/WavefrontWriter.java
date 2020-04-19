// vim: noet

package vktec.geomexport;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;

public abstract class WavefrontWriter implements AutoCloseable {
	protected final BufferedWriter file;

	public WavefrontWriter(Path path) throws IOException {
		this.file = Files.newBufferedWriter(path, Charset.forName("utf-8"));
	}

	public void close() throws IOException {
		this.file.close();
	}

	protected void write(String... fields) throws IOException {
		boolean first = true;
		for (String field : fields) {
			if (first) first = false;
			else this.file.write(' ');

			this.file.write(field);
		}
		this.file.newLine();
	}

	protected void writef(String format, Object... values) throws IOException {
		this.file.write(String.format(format, values));
	}

	protected void writefln(String format, Object... values) throws IOException {
		this.writef(format, values);
		this.file.newLine();
	}
}
