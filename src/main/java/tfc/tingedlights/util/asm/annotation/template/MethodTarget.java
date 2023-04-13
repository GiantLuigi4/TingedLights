package tfc.tingedlights.util.asm.annotation.template;

public @interface MethodTarget {
	String[] value();
	
	boolean regex() default false;
	
	boolean includeDesc() default false;
	
	AnnotationTemplate[] annotations() default {};
	
	boolean matchAllAnnotations() default true;
}
