## Modularity

Since the early days of software development, the benefits of creating modular systems were pretty well accepted. Modular system provided a number of benefits from technical (like replacability) to organizational (like manpower scalability). However, at scale modularity can require a significant amount of mental energy. Building and assembling large systems from a large number of modules gets complex pretty fast. *This is likely to have been the reason why **monoliths** became so prolific.*

Given hundreds or thousands of modules the sheer volumn of information can make reasoning about them difficult.

A system for reasoning about all these modules had to be adopted.

The principle goal of this system?

## Resolving Dependencies

The fundamentally hard task when it comes to highly modular and granular systems is resolving dependencies and it's a critically important aspect of software development which is often taken for granted.

There are of course different stages of dependency resolution.

- **Compile** At compile time we need to resolve the dependencies of our code. Most often we do this manually by deciding which libraries we will use. Ideally we compile against pure contracts rather than implementations.

- **Test** While executing tests you need to resolve test frameworks, stubs and mocks. etc.

- **Deployment** When preparing for deployment we resolve all the run time dependencies in order to produce a runnable system.

## The Artifact Identity Model

Likely the most common form of resolution mechanism we've encountered is based on the **Artifact Identity Model**. This model centers around the concept of unique artifact identity broken into two main parts; **artifact id** and **artifact version**. This allows dependencies to be expressed in a very simple way.

## The Artifact Identity Repository Model

The **Artifact Identity Model** paired with a simple HTTP based URI model has proven so usefull that it has formed the basis of most software distribution systems to date. Forgetting the fact that software repository definitions are blurred between package management systems, binary software repositories and software development repositories. A few are (but not exclusively) CPAN, PECL, NuGet, CRAN, PyPI, RAA, etc.

Oh sorry! Did I forget to mention Maven and NPM? Or things like Debian Repos, RPM Repos, Homebrew, Fink, App Store, Google Play, Windows Store, Steam, Docker Hub.

## Limitations of the Identity Model

Along the way however it became clear that this simple pair (**artifact id** and **artifact version**) was too limited. We needed to have more information about the artifacts! We needed to put more information into the repositories. We really needed to classify artifacts along several additional axes; system architecture, base platform version, executable vs. source code, vendor, mutual exclusivity, license, packaging, dependencies, and on and on.

Identity based dependency leads to accidental and incidental coupling if we aren't extremely meticulous about dependency management. This is quite evident today when using projects which result in hundreds of retrieved dependencies, only a fraction of which are typically really used. Or when one project is used in conjunction with other projects having dependency version collisions.

These issues often lead to technical dept, dept is risk, and we want to try to avoid risk whenever possible.

## Ask yourself what your CTO wants?

At the end of the day, they want to **express requirements** to their team and **get results fast**!

## As a developer what do I want?

I want to **provide capabilities** which **satisfy the requirements** and I want to **keep my process agile** and **avoid technical dept**!

## Evolve from Identity to Requirements!

As systems become more and more complex we might want to consider evolving the expression of our dependencies from *simple* identity to that of **requirements**.

## OSGi

Since the beginning, the OSGi model has been fundamentally based on the idea of inserting knowledge into artifacts in order to express strict constraints or execution. OSGi frameworks have used this knowledge to great effect.

## The Requirements and Capabilities Model

Around 2005 the trend of defining distinct contracts for OSGi bundles was formalized into a *constraint model* called **Requirements and Capabilities**. This model is a general expression language used for defining contracts and all relevant metadata already defined in OSGi was re-expressed in terms of it. This was the first demonstration of it's power and later it proved simple yet powerfull enough to express any additional contracts which have been added to date.

*It should be noted that the **Requirements and Capabilities Model** is not tied to OSGi or to the Java language specifically. With that in mind it's possible to envision other uses for the model.*

## Standardized Expressions

- **Requirement** - an XML parser for Java which implements the W3C DOM, Level 3 API.
```
Require-Capability: \
    osgi.wiring.package; \
        filter:='(&(osgi.wiring.package=org.w3c.dom)(version=1.0))'
```
- **Capability**
```
Provide-Capability: \
    osgi.wiring.package; \
        osgi.wiring.package='org.w3c.dom'; \
        version:Version='1.0'
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
        debian.depends:List<String>='libc6,libdbus-1-3,libdbus-glib-1-2,libgcc1,libglib2.0-0,libgudev-1.0-0,libimobiledevice2,libplist1,libpolkit-gobject-1-0,libupower-glib1,libusb-1.0-0,udev,dbus,pm-utils'
        debian.recommends='policykit-1';\
        debian.section='admin';\
        debian.priority='optional';\
```

- **Requirement:** - a key-value store which provides an asynchronous client API & driver for java.
```
Require-Capability: \
    osgi.implementation; \
        filter:='(&(osgi.implementation=key.value.store)(version=1.0)(intents=async))'
```
- **Capability**
```
Provide-Capability: \
    osgi.implementation; \
        osgi.implementation='key.value.store'; \
        version='1.0'; \
        intents='async'
```
