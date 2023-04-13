package tfc.tingedlights.util.asm.struct.template;

import org.objectweb.asm.tree.AnnotationNode;
import org.objectweb.asm.tree.MethodNode;
import tfc.tingedlights.util.asm.struct.BaseStruct;
import tfc.tingedlights.util.asm.util.AnnotationParser;

import java.util.function.Predicate;
import java.util.regex.Pattern;

public class MethodTargetStruct extends BaseStruct {
	String[] value;
	boolean regex;
	boolean includeDesc;
	AnnotationTemplateStruct[] annotations;
	boolean matchAllAnnotations;
	
	@Override
	public boolean handleNull(String key) {
		switch (key) {
			case "regex" -> {
				regex = false;
				return true;
			}
			case "annotations" -> {
				annotations = new AnnotationTemplateStruct[0];
				return true;
			}
			case "includeDesc" -> {
				includeDesc = false;
				return true;
			}
			case "matchAllAnnotations" -> {
				matchAllAnnotations = true;
				return true;
			}
		}
		return false;
	}
	
	public boolean annotationMatches(MethodNode method) {
		if (annotations.length == 0) return true;
		
		if (matchAllAnnotations) {
			for (AnnotationTemplateStruct annotation : annotations) {
				boolean matchedAny = false;
				for (AnnotationNode allAnnotation : AnnotationParser.allAnnotations(method)) {
					if (annotation.matches(allAnnotation))
						matchedAny = true;
				}
				if (!matchedAny)
					return false;
			}
			return true;
		} else {
			for (AnnotationTemplateStruct annotation : annotations) {
				for (AnnotationNode allAnnotation : AnnotationParser.allAnnotations(method)) {
					if (annotation.matches(allAnnotation)) {
						return true;
					}
				}
			}
			return false;
		}
	}
	
	public boolean matches(MethodNode method) {
		String testName = includeDesc ? method.name + method.desc : method.name;
		
		if (regex) {
			for (String s : value) {
				Predicate<String> pattern = Pattern.compile("$" + s + "^").asMatchPredicate();
				if (pattern.test(testName))
					return annotationMatches(method);
			}
		} else {
			for (String s : value) {
				if (testName.equals(s))
					return annotationMatches(method);
			}
		}
		
		return false;
	}
}
