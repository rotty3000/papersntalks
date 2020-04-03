
##### Multiple Bundle Annotations: Caveat #1

When the _extended bundle annotation_ is meta-annotated with multiple _bundle annotation_, `@Attribute` and `@Directive` are added to the generated output from each _bundle annotation_.

```java
@Capability(namespace = "foo.namespace")
@Capability(namespace = "osgi.extender", name = "bar", version = "1.0.0")
@interface Extended {
	@Directive("x-foo")
	String value() default "bar";
}

// usage

@Extended
public class Foo {}
```

results in the manifest header:

```
Provide-Capability: \
	foo.namespace;x-foo=bar,\
	osgi.extender;osgi.extender=bar;version:Version="1.0.0";x-foo=bar
```

Notice that the directive was added to both capabilities. This may result in hard to resolve conflicts if a directive is not valid for a particular namespace, but is required for another. Furthermore, separating these details out to more than one annotation may invalidate the ease-of-use aspect we tried so hard to achieve.

##### Multiple Bundle Annotation: Use Case #1

Without expressing valid use cases it can be hard to reason out why the above caveat is a real problem.

Let's consider the [OSGi CDI](https://osgi.org/specification/osgi.enterprise/7.0.0/service.cdi.html) Portable Extension scenario. What does such a bundle need to do?

1. it **must** choose an _extension name_ (to be used later)
2. it **must** implement the interface `javax.enterprise.inject.spi.Extension`
3. it **must** publish an OSGi service with that interface having a service property `osgi.cdi.extension` whose value is the _extension name_
4. it **should** publish an `osgi.service` capability for that interface
5. it **must** require the `osgi.cdi` implementation capability
6. it **must** provide an `osgi.cdi.extension` capability whose name is the _extension name_

The goal is to provide an annotation that makes it easy for an developer to get all these details correct without having to become an expert in OSGi.

But let's throw a couple more functional requirements into the mix just to spice things up.

7. it **must** provide a `META-INF/services` descriptor so that it can be used in standard Java
8. it **should** not require any special OSGi code
9. missing `osgi.cdi` implementation capability **must** not prevent the bundle from being resolved at runtime

This complex scenario seems impossible. It also looks like it would be a perfect opportunity to use _bundle annotations_. Let's begin by creation an annotation called `@CDIExension` and obviously for reuse this annotation needs at least a parameter to hold the _extension name_.

```java
@interface CDIExtension {
    /**
     * The required extension name.
     */
    String value();
}
```

We could address item `5.` easily by using the annotation `@RequireCDIImplementation` specified in the OSGi CDI specification.

```java
@RequireCDIImplementation
@interface CDIExtension {
    /**
     * The required extension name.
     */
    String value();
}
```

However, this will block requirement `9.` due to the fact this annotation will produce a requirement which will be enforced in runtime when the bundle is resolved. Since we're only planning to write this annotation once, let's copy the necessary logic for `5.` while also addressing `9.`

```java
@Requirement(
	namespace = "osgi.implementation",
	name = "osgi.cdi",
	version = "1.0.0",
	effective = "active") // not effective at runtime, but may be at deploy time
@interface CDIExtension {
    /**
     * The required extension name.
     */
    String value();
}
```

We've solved `1.`, `5.` & `9.`
