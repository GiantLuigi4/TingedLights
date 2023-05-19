package tfc.tingedlights.util.asm.hinting;

/**
 * classes annotated with this are extremely likely to cause problems if used
 * generally, they should be avoided at pretty much all costs, and often will be more or less useless anyway
 */
// TODO: require a @AllowUnsafe annotation to be on any hook class which uses a dangerous hook
public @interface Dangerous {
}
