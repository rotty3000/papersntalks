Liferay is in the initial phase of semantically versioning all it's java source code. Yay! What?

If you don't know what semantic versioning is, here are two essential references on the subject:

http://semver.org/

> *Summary*
>
> Given a version number MAJOR.MINOR.PATCH, increment the:
> 
> MAJOR version when you make incompatible API changes,
> MINOR version when you add functionality in a backwards-compatible manner, and
> PATCH version when you make backwards-compatible bug fixes.
> Additional labels for pre-release and build metadata are available as extensions to the MAJOR.MINOR.PATCH format.

http://www.osgi.org/wiki/uploads/Links/SemanticVersioning.pdf

Liferay will be applying package level semantic versioning which will allow for very granular API versioning. This is the same versioning strategy prescribed in the OSGi Semantic Versioning whitepaper. This will help developers, our support, and our documentation teams easily track API evolution and increase the stability of our APIs.

Adding semantic versioning to such a large project will be a challenge. One of the greatest difficulties will be educating all contributing developers on how to properly handle it. Hopefully, we can begin to address that here.

Leaving semantic versioning in the care of humans can easily become a nightmare. So much so that there are even theories which state that semantic versioning is not worth the effort due to the _human limitations_. The ideal scenario then is to not leave it up to humans at all. Or rather, to not _only_ leave it up to humans. Machines are much more suited to dealing with this sort of thing.

However, a big problem was that until recently there were no good libraries which could produce, with reasonable heuristics, accurate reports about how APIs had changed. This is no longer the case. The open source *bnd* library ("_the Swiss army knife of OSGi_") originally developed by OSGi grand master Peter Kriens (http://www.aqute.biz/Bnd/Bnd) and now maintained (_still with Peter, but in a more community driven effort_) under the umbrella project *BndTools* http://bndtools.org (an impressive OSGi tooling suite for the Eclipse platform) is available for doing all the necessary work programatically.

But humans don't like being told what to do by machines. So the compromise is to make the machine do the hard work, and have it report it's findings to humans, letting humans decide how to react.

Let's outline the steps involved. (_The implementation details discussed are specific to Liferay's tool built around *bnd*, but the process could be applied to any project were an equivalent build integration tool available._)

### Step 1 - Setup
Let's assume that we're starting from a pristine working copy of our source code, checked out from the repository and positioned at a _version tag_ we want to use as the foundation of our semantic versioning efforts. We'll call this the *_baseline version_*.

The next assumption is that we have enabled semantic version reporting in our build configuration. In the case of Liferay's reporting tool, this is configured by editing the `build.${user.name}.properties` file and choosing a value for the following property:

```
    baseline.jar.report.level=diff
    #baseline.jar.report.level=off
    #baseline.jar.report.level=persist
    #baseline.jar.report.level=standard
```

Several reporting levels are available, `diff` by default.

* `off` = no reporting enabled
* `standard` = report only package level changes
* `diff` = include with `standard` a granular, differential report of all changes in the package
* `persist` = in addition to `diff` persist the report to a file inside the directory identified by the property `baseline.jar.reports.dir.name` which defaults to `baseline-reports`

### Step 2 - The First Build
Before any code changes take place we will perform the _first build_. During the build java source code is compiled and then packaged into jar files. With the baseline engine enabled, just before the jar file creation process completes, it's contents are analyzed against a _previously existing version_ of the jar. This _previously existing version_ is located in what we call the _baseline repository_.

Since this is the _first build_ two distinct operations will take place.

1. The baseline engine's workspace is initialized and the baseline repository is created. This takes place in the folder identified by the property `baseline.jar.bnddir.name` which is `.bnd` by default (note the directory is hidden). Note also that if you want to restart this entire process from scratch, the only step is to delete this directory.

2. Since there is no _previously existing version_ of the jar in the _baseline repository_, the current jar is added. It effectively becomes the baseline version. Also recall that earlier we chose a well define _version tag_ as the starting point. This represents the effective version to which all subsequent changes will be compared. 

Finally, no reports should result from the _first build_.

### Step 3 - Making Changes
When we perform a change to the java source code such as developing new features, fixing bugs, etc. we'll need to execute a build with our changes, execute tests, etc.

Execute the build. We'll refer to it as a _subsequent build_ to distinguish it from the _first build_.

### Step 4 - Reporting
While the _subsequent build_ is executing, each _new jar_ (assuming of course the jar does in fact have some change) is analyzed against the _previously existing version_  jar (a.k.a. _old jar_) obtained from the _baseline repository_ (.. placed there during the _first build_).

The baseline engine performs a tree based comparison between the _new jar_ and the _old jar_. The engine will then produce output based on the current reporting level.

If no API changes were detected there should be no new output. The build should look like it always has.

### Reports
Now, let's assume that some change was in fact detected. For now, let's consider a very simple change. We'll add a new method to the concrete type:

`com.liferay.portal.kernel.cal.Duration`

In it's current form this class, and in fact it's package, are versioned based on the version of the jar in which it resides. Therefore in the case of Liferay 6.2.0, it's effective version is '6.2.0'.

Let's add a new method to this concrete class:

```java
public void newMethod() {
	System.out.println("executing the new method");
}
```

Rebuilding the jar containing this class should produce the following report:

```
[baseline-jar] portal-service (portal-service.jar) 4826
[Baseline Report] Mode: persist
[Baseline Warning] Bundle Version Change Recommended: 6.3.0
  PACKAGE_NAME                                       DELTA      CUR_VER    BASE_VER   REC_VER    WARNINGS  
= ================================================== ========== ========== ========== ========== ==========
* com.liferay.portal.kernel.cal                      MINOR      6.2.0      6.2.0      6.3.0      VERSION INCREASE REQUIRED
	<   class      com.liferay.portal.kernel.cal.Duration
		+   method     newMethod()

BUILD SUCCESSFUL
```

Let's break down the report line by line.

*Line 1:*

```
[baseline-jar] portal-service (portal-service.jar) 4826
```

This indicates the task is executing the `baseline-jar` task (a.k.a. the baseline engine) on `portal-service.jar` and there are 4826 resources in the jar (err.. that's a large jar, it's also the first hint that we should probably break it into pieces to improve maintainability, but that's a topic for another time).

*Line 2:*

```
[Baseline Report] Mode: diff
```

This indicates the current report mode or level, `diff`.

*Line 3:*

```
[Baseline Warning] Bundle Version Change Recommended: 6.3.0
```

This indicates that the report suggests a version change should be applied to the jar in question.

*Lines 4-5:*

```
  PACKAGE_NAME                                       DELTA      CUR_VER    BASE_VER   REC_VER    WARNINGS  
= ================================================== ========== ========== ========== ========== ==========
```

These are header lines and are only here to clarify the details of the details which follow. The report is broken down by package, and so the header reflects this.

The first column (un-named, 1 char width) is for indicating a *dirty* state of the package (represented as `*` = dirty, empty = not dirty).

When dirty, the package requires the attention of the developer. It usually reflects that either the changes or the package version require review and certainly that the API has been subject to a non-trivial change and the version no longer represents it accurately.

The second column (PACKAGE_NAME) indicates the package in question. Note that each sub package is treated uniquely, defining it's own version and being reported on independently.

The third column (DELTA) indicates the magnitude of the change (ADDED, CHANGED, MAJOR, MICRO, MINOR, REMOVED, UNCHANGED).

* `ADDED` = a new package which does not exist in the baseline repo (should be version 1.0)
* `CHANGED` = a none API change was detected, possibly an unwarranted API version increase, etc.
* `MAJOR` = a _breaking_, backward incompatible change
* `MICRO` = a non-API change
* `MINOR` = a backward compatible change
* `REMOVED` = the package was removed (may have been moved to own jar = MINOR, or deleted with no replacement = MAJOR)
* `UNCHANGED` = no change

The fourth column (CUR_VER) indicates the current version of the package.

The fifth column (BASE_VER) indicates the version of the package in the baseline repository.

The sixth column (REC_VER) indicates the recommended version based on the magnitude of the change.

The seventh and last column (WARNINGS) will print any warnings or verbal recommendations which are a best guess as to what step might resolve the dirty state of the package.

*Line 6:*

```
* com.liferay.portal.kernel.cal                      MINOR      6.2.0      6.2.0      6.3.0      VERSION INCREASE REQUIRED
```

This is the overall package report. Following the column descriptions above, you can see that the package is in a dirty state. The package `com.liferay.portal.kernel.cal` has incurred a `MINOR` change. Considering the current version and the baseline version, the report suggests that an increase of the package version to `6.3.0` is required to resolve the dirty state.

*Lines 7-8:*

```
	<   class      com.liferay.portal.kernel.cal.Duration
		+   method     newMethod()
```

_*Note:* These lines will only be included in report if the levels is `diff` or `persist`._

Packages often contain more than one class. To the developer making the change it's simple enough to understand why the package might be dirty. However, as the number of changes increase, or to an outsider, like a support or documentation engineer, understanding the specific nature of the change is often interesting, and quite often necessary to making life easier. Therefore, when in `diff` or `persist` level, for each package we show a differential view of every single change in the package. This is not a traditional `diff` output, but rather an API diff. For this reason the report requires a little bit of getting used to.

The first line indicates the class in question.

```
	<   class      com.liferay.portal.kernel.cal.Duration
```

The leading character (in this case `<`) indicates the magnitude of change for the specific class. It's worth noting that each class may have different degree of change, and the package will reflect only the highest degree of change.

Possible leading characters are:

* `+` = ADDED
* `~` = CHANGED
* `>` = MAJOR
* `µ` = MICRO
* `<` = MINOR
* `-` = REMOVED

Lines which follow indicate each change which occurred in the class, and may even show lower levels in cases where the change occurred on inner elements of the class. The depth increases with each inner element.

```
		+   method     newMethod()
```

In this case we can see that a method called `newMethod` was added.

This raises the question: *"How do we resolve the dirty state of the package?"*

### Reacting to Reports
There are several ways to react to these reports, each of which is dependent on a couple of different factors and so it's important to have a good understanding of them so as not to be intimidated by their granular nature.

Considering the change we already made, the first reaction we could have might be to revert the change completely. Re-running the build would clear up the report. Obviously this also wouldn't result in any progress. If we are attempting to fix a bug however, it's actually very bad practice to change public API if at all possible. So the report might be indication that we tried to solve the bug using an approach which was too aggressive to the API and we should reconsider the change.

However, it _may_ happen that a developer finds that it's impossible to solve a bug without changing an API. But such a change should be a MINOR change at most (which is backward compatible) and only when whoever is responsible for ongoing maintenance is in agreement. This type of change may require the preparation of documentation since the public API was changed, even if it was a minor change.

In this case however, the most common reaction to such a report is more often than not to simply set the package version to the new version.

#### Package Versions (packageinfo)
Granularly managing package versions is achieved in one of two ways according to the OSGi Semantic Versioning whitepaper. For our purposes, we will follow the `packageinfo` text file approach.

Locating the directory of the package `com.liferay.portal.kernel.cal` containing the `Duration.java` file, we will create a new text file called `packageinfo` (with no extension). This file should contain a single line following the pattern:

```
version <version>
```

where `<version>` is the actual version we want to assign to the package following the syntax defined for semantic versioning (see the *Summary* above).

For instance:

```
version 6.3.0
```

and re-run the build.

*Line 6* should change to:

```
  com.liferay.portal.kernel.cal                      MINOR      6.3.0      6.2.0      6.3.0      -         
```

Note that the package is no longer in dirty state and the current version matches the recommended version. Lastly, there are no warnings (represented by `-`).

The remain report lines reveal more information about the package changes:

```
	<   class      com.liferay.portal.kernel.cal.Duration
		+   method     newMethod()
	-   version    6.2.0
	+   version    6.3.0
```

As you can see, at the same level as the class, we see that the package version change is represented by the removal of the `6.2.0` version and the addition of the `6.3.0` version. We now see the sum of all changes since the baseline and also that all appropriate actions have been taken to resolve the dirty state.

#### A MAJOR change
Minor changes will and should occur relatively frequently. This is just common to pretty much all every day development. So, let's make a change which will produce a MAJOR change to see how this affects our report.

In the same class there's a method which is un-used in Liferay:

```java
	/**
	 * Method getDays
	 *
	 * @return int
	 */
	public int getDays() {
		return _days;
	}
```

Let's delete this method from the class and re-run the build.

```
[baseline-jar] portal-service (portal-service.jar) 4827
[Baseline Report] Mode: persist
[Baseline Warning] Bundle Version Change Recommended: 7.0.0
  PACKAGE_NAME                                       DELTA      CUR_VER    BASE_VER   REC_VER    WARNINGS  
= ================================================== ========== ========== ========== ========== ==========
* com.liferay.portal.kernel.cal                      MAJOR      6.3.0      6.2.0      7.0.0      VERSION INCREASE REQUIRED
	>   class      com.liferay.portal.kernel.cal.Duration
		-   method     getDays()
			-   return     int
		+   method     newMethod()
	-   version    6.2.0
	+   version    6.3.0
```

Whoa!! What happened here?

```
[Baseline Warning] Bundle Version Change Recommended: 7.0.0
```

The report is telling us that we should increase the jar version to `7.0.0`. That's aggressive.

```
* com.liferay.portal.kernel.cal                      MAJOR      6.3.0      6.2.0      7.0.0      VERSION INCREASE REQUIRED
	>   class      com.liferay.portal.kernel.cal.Duration
		-   method     getDays()
			-   return     int
		+   method     newMethod()
	-   version    6.2.0
	+   version    6.3.0
```

At the package level, we can see that once again the package is dirty indicating that we have to take action with respect to versions. If we had any intention of putting this change in the hands of developers on maintenance releases we'd have some very angry developers since this is a breaking change that is NOT backward compatible.

Deleting a method from an established API is always a breaking change. So, unless this is in fact targeted at a major release we should not make this change. At most, if we need to indicate that the method should no longer be used, we can and should deprecate during maintenance releases and warn of removal in some future major release.

### Other challenges
Several other challenges exist in very large projects like ours.

#### Over-versioning
A rule of thumb in semantic versioning is that the jar version should reflect the highest version from among all the package versions found within the jar. We can extrapolate some insight from how quickly a jar's version increases over time in relation to other packages in the jar.

For instance, if some package within the jar increases by more than one major version while others do not, this is a good indication that this API is more volatile and should probably be extracted into it's own jar, increasing maintainability and reducing it's _area of effect_.

The flip side of that is if a particular package within a jar rarely changes in relation to other packages in the same jar it's also an indication that it should be extracted and from the rest. It's stable and safe code that should not be subjected to version increases like the rest. That's good code.

#### Split packages
Split packages occur when the same packages are defined in different jars. This causes problems since package versions may not coincide across those jars. Secondly, controlling access to the API using OSGi package dependencies is more difficult.

#### Massive packages
When a package contains many, many classes, it's more likely than not that not all classes still comprise the same API. The result is over aggressive API versioning when changes occur to some classes within the package which are not related to other classes in the package.

Take for example a well known Liferay package:

`com.liferay.portal.kernel.util`

This package contains many utility classes most of which are not related to each other.

`com.liferay.portal.kernel.util.ArrayUtil`
`com.liferay.portal.kernel.util.ContentTypes`

These two classes are completely unrelated. However, if any single class within this package is subject to a version change, the change must be reflected on all the classes in the package even though they are unrelated. This is a clear indication that they should have their own packages.

### What's Next
Addressing all of the challenges above will be a considerable task. However, this tool provides a mechanism to _progressively_ apply semantic versioning to our existing largely un-versioned code base. It will also provide is with hints as to how we can continue to improve our APIs in ways other than semantic versioning, such as isolating APIs from each other to reduce API over-versioning.

Our developers will learn about semantic versioning in an almost passive way which shouldn't greatly impede their day to day activities. Furthermore, they will learn to develop a responsibility for the degree of change they cause and hopefully together we will grow to be better developers.