package tfc.tingedlights.util.asm.annotation;

import tfc.tingedlights.util.asm.annotation.template.MethodTarget;
import tfc.tingedlights.util.asm.hinting.Dangerous;

/**
 * removes methods from a class
 * there is pretty much no valid use case for this
 */
// TODO: require a @AllowUnsafe annotation to be on any hook class which uses a dangerous hook
@Dangerous
@Deprecated // hints IDE to warn devs who use this
public @interface RemoveMethods {
	MethodTarget[] targets();
}
