abstract-forever-j
================

Annotation library to enable a class or interface to enforce 
that methods marked must be explicitly overridden by every 
concrete subclass of the marking class or concrete implementation 
of a marking interface.

The annotation may be placed on an abstract method prescribed by 
an interface or abstract class, or in a concrete class.

Can be specified at any level in a type hierarchy but only 
propagates its requirement downward.

v1.1 : 

    Removed @SupportedSourceVersion from processor as it needlessly
    limits using the library in Java 6 environments. This annotation is
    documented as specifying the "latest" version of Java that would be
    supported by the annotation. Due to the enum that is used not 
    having entries for future versions of Java, the effect is that the
    specified version is actually the "only" version that is supported.

    ie: @SupportedSourceVersion(SourceVersion.RELEASE_7) really means only
    Java 7 can use this annotation. 

