package tfc.tingedlights.utils.config.annoconfg.annotation.value;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@Retention(RetentionPolicy.RUNTIME)
public @interface FloatRange {
	float minV();
	float maxV();
}
