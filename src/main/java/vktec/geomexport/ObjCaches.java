// vim: noet

package vktec.geomexport;

import java.util.HashMap;
import java.util.Map;

public class ObjCaches {
	public final Map<String,Integer> vertex = new HashMap<>();
	public final Map<String,Integer> normal = new HashMap<>();
	public final Map<String,Integer> uv = new HashMap<>();
	public final Map<String,Material> material = new HashMap<>();
}
