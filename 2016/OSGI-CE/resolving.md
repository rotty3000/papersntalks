## Modularity

Creating a modular system leads to a number of benefits, from technical improvements (like replacability) to organizational improvements (like manpower scalability). However, at scale modularity can require a significant amount of mental energy. Building and assembling large systems from lots of modules gets complex pretty fast. *This is likely to have been the reason why **monoliths** happened.*

Given hundreds or thousands of modules the sheer volumn of information can make reasoning about them difficult.

## Resolving Dependencies

The fundamentally hard task when it comes to highly modular and granular systems is resolving dependencies and it's a critically important aspect of software development which is most often taken for granted.

There are of course different stages of dependency resolution.

- **Compile** At compile time we need to resolve the contract dependencies of our code. Most often we do this manually by deciding which libraries we will use. Ideally we compile against pure contracts rather than implementations.

- **Test** While executing tests you need to resolve test frameworks, stubs and mocks. etc.

- **Deployment** When preparing for deployment we resolve all the run time dependencies in order to produce a runnable product.

## The Artifact Identity Model

Likely the most common form of resolution mechanism we've encountered is based on the **Artifact Identity Model**. This model centers around the concept of unique identity broken into two main parts; **artifact id** and **artifact version**. This allows dependencies to be expressed in a very simple way.

## The Artifact Identity Repository Model

**Artifact Identity Model** paired with a simple HTTP based URI model has proven so usefull that it has formed the basis of most software distrobution systems to date. Forgetting the fact that software repository definitions are blurred between what might be considered pre-packaged software repositories and software development repositories, some of which are (but not exclusively) CPAN, PECL, NuGet, CRAN, PyPI, RAA, etc.

Oh sorry! Did I forget to mention Maven and NPM? Or things like Debian Repos, RPM Repos, Homebrew, Fink, App Store, Google Play, Windows Store, Steam,Docker Hub.

## Limitations of the Identity Model

Along the way however, in pretty much all cases, including those above, we realized that this simple pair of **artifact id** and **artifact version** was too limited. We needed to have more information about the artifacts! We needed to put more information into the repositories. We really needed to classify artifacts along typically several axes; system architecture, base platform version, executable vs. source code, vendor, mutual exclusivity, license, packaging, and on and on.

Dependency by artifact id and version alone leads to accidental and incidental coupling if not extremely meticulous about declaring dependencies.

Coupling is technical dept, dept is risk, and CTOs try to avoid risk whenever possible.

## What does A CTO want?

They want to **express requirements** to their team and **get results fast**!

As systems become more and more complex we need to consider evolving the expression of our dependencies from *simple* identity to that of **requirements**.

## As a developer what do I want?

I want to **provide capabilities** which **satisfy the requirements** and I want to **keep my process agile** and **avoid technical dept**!

Now the OSGi model is fundamentally based on the idea of inserting knowledge into artifacts. OSGi frameworks use this knowledge to great effect. 

## The Requirements and Capabilities Model

Around 2005 the trend of defining metadata for OSGi bundles was formalized into a *constraint model* called **Requirements and Capabilities**. This model is a general expression language used for defining contracts and all relevant metadata already defined in OSGi was re-expressed in terms of it. This was the first demonstration of it's power and later it proved simple to express any additional contracts using the simple language.

*It should be noted that the **Requirements and Capabilities Model** is not tied to OSGi or to the Java language specifically. With that in mind it's possible to envision other uses for the model.* 

Examples of expressions:
- **Requirement** - an XML parser for Java which implements the W3C DOM, Level 3 API.
```
Require-Capability: osgi.wiring.package;filter:='(&(osgi.wiring.package=org.w3c.dom)(version=1.0))'
```
- **Capability**
```
Provide-Capability: osgi.wiring.package;osgi.wiring.package='org.w3c.dom';version:Version='1.0'
```

- **Requirement** - a Debian battery monitor which runs on ARM and messages over D-Bus.
```
Require-Capability: \
    debian.package; \
        filter:='(&(debian.package=upower)(debian.architecture=armhf)(debian.depends=dbus))'
```
- **Capability**
```
Provide-Capability: \
    debian.package; \
        debian.package='upower'; \
        debian.version='0.9.17-1'; \
        debian.architecture='armhf'; \
        debian.recommends='policykit-1';\
        debian.section='admin';\
        debian.priority='optional';\
        debian.depends:List<String>='libc6,libdbus-1-3,libdbus-glib-1-2,libgcc1,libglib2.0-0,libgudev-1.0-0,libimobiledevice2,libplist1,libpolkit-gobject-1-0,libupower-glib1,libusb-1.0-0,udev,dbus,pm-utils'
```

- **Requirement:** - a key-value store which provides an asynchronous client API & driver for Haskel.
```
Require-Capability: \
```
- **Capability**
```
Provide-Capability: \
```

## Standardizing Expressions
