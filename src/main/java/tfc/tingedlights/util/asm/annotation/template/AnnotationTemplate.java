package tfc.tingedlights.util.asm.annotation.template;

public @interface AnnotationTemplate {
	/**
	 * the type of the annotation
	 * should be written in either dotted.class.Notation, or slashed/class/Notation
	 */
	String type();
	
	/**
	 * the list of values
	 * should be written in a properties-like format: "key=value", "key1=value1", etc
	 */
	String[] values();
	
	/**
	 * whether or not it should match exactly the set of values
	 * "exactly" as in, the annotation being checked has the exact same amount of values as this template
	 */
	boolean strict() default false;
}
