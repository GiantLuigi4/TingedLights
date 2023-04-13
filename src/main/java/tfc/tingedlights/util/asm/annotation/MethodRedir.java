package tfc.tingedlights.util.asm.annotation;

import tfc.tingedlights.util.asm.annotation.template.MethodTarget;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target({ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
public @interface MethodRedir {
	//@formatter:off
	
	/**
	 * regex query to match method names
	 * $ and ^ are automatically inserted to the start and end, respectively
	 */
	String method() default ".*";
	
	/**
	 * format:
	 * L[owner/ClassName];[methodName]
	 * automatically picks descriptor based off method arguments
	 *
	 * L[owner/ClassName];[methodName]([descArgs])[descReturnType]
	 */
	String[] redirTarget();
	MethodTarget[] exclude() default {};
	//@formatter:on
}
