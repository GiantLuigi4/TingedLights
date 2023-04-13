package tfc.tingedlights.util.asm.struct;

import tfc.tingedlights.util.asm.struct.template.MethodTargetStruct;

public class MethodRedirStruct extends BaseStruct {
	public String method;
	public String[] redirTarget;
	public MethodTargetStruct[] exclude;
	
	public boolean handleNull(String key) {
		if (key.equals("method")) {
			method = ".*";
			return true;
		} else if (key.equals("exclude")) {
			exclude = new MethodTargetStruct[0];
			return true;
		}
		return false;
	}
}
