package tfc.tingedlights.util.asm.struct.template;

import org.objectweb.asm.tree.AnnotationNode;
import tfc.tingedlights.util.asm.struct.BaseStruct;
import tfc.tingedlights.util.asm.util.AnnotationParser;

import java.util.HashMap;
import java.util.Map;

public class AnnotationTemplateStruct extends BaseStruct {
	String type;
	String[] values;
	boolean strict;
	
	@Override
	public boolean handleNull(String key) {
		if (key.equals("strict")) {
			strict = false;
			return true;
		}
		return false;
	}
	
	public boolean matches(AnnotationNode node) {
		String desc = "L" + type.replace(".", "/") + ";";
		
		if (desc.equals(node.desc)) {
			HashMap<String, String> map1 = new HashMap<>();
			for (String value : values) {
				String[] split = value.split("=", 2);
				map1.put(split[0], split[1]);
			}
			
			HashMap<String, Object> map = AnnotationParser.getValues(node);
			
			// if it is strict, then check that the two have the exact same number of defined entries
			if (strict && map.size() != map1.size())
				return false;
			
			// check that all required keys are shared
			for (String s : map1.keySet())
				if (!map.containsKey(s))
					return false;
			
			// check matches
			for (Map.Entry<String, String> stringStringEntry : map1.entrySet()) {
				Object o = map1.get(stringStringEntry.getKey());
				if (!o.toString().equals(stringStringEntry.getValue()))
					return false;
			}
			
			return true;
		}
		
		return false;
	}
}
