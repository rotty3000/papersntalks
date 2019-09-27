





# Building OSGi Projects with Bnd in Maven

Raymond Augé - Sr. Software Architect

## Existing maven OSGi Tools

[Apache Felix `maven-bundle-plugin`](https://felix.apache.org/documentation/subprojects/apache-felix-maven-bundle-plugin-bnd.html)

[Eclipse Tycho](https://www.eclipse.org/tycho/)

[Bndtools.org `bnd*-maven-plugin`](https://github.com/bndtools/bnd/blob/master/maven/README.md)

*... note there are a number of gradle plugins for OSGi, including ones from bnd*

## `maven-bundle-plugin`

most widely adopted tool for working with OSGi across the Java ecosystem

still very relevant because keeping pace with underlying **bnd** dependency

supports additional functionalities like deploying bundles to OSGi repositories and **baseline**

## Eclipse Tycho

focused on a manifest-first approach

associated primarily with building Eclipse RCP applications and Eclipse plugins using PDE

## `bnd*-maven-plugin`

state of the art OSGi development

provided by a suite of plugins

each one focused on an individual aspect

designed to be composed to meet developer needs

## `bnd-maven-plugin`

the core plugin, used to generate manifest and other metadata for
projects that build an OSGi bundle

### setup

```xml
<plugin>
	<groupId>biz.aQute.bnd</groupId>
	<artifactId>bnd-maven-plugin</artifactId>
	<executions>
		<execution>
      <id>bnd-process</id>
			<goals><goal>bnd-process</goal></goals>
		</execution>
	</executions>
</plugin>
```

### configuring manifest

```xml
<plugin>
	<groupId>org.apache.maven.plugins</groupId>
	<artifactId>maven-jar-plugin</artifactId>
	<configuration>
		<archive>
			<manifestFile>
        ${project.build.outputDirectory}/META-INF/MANIFEST.MF
      </manifestFile>
		</archive>
	</configuration>
</plugin>
```

## `bnd-indexer-maven-plugin`

used to generate an OSGi repository index from a set of Maven
dependencies. The entries in the index will reference the location of
the bundles in the remote repositories to which they have been deployed

### setup

```xml
<plugin>
  <groupId>biz.aQute.bnd</groupId>
  <artifactId>bnd-indexer-maven-plugin</artifactId>
  <configuration>...</configuration>
  <executions>
    <execution>
      <id>index</id>
      <goals><goal>...</goal></goals>
    </execution>
  </executions>
</plugin>
```

### variety of indexing options

* `	index` - generates OSGi index output files which are attached as deployable artifacts
* `local-index` - generates OSGi index output files which are *not* attached as deployable artifacts
* local vs. external urls
* selection of scopes from which to select artifacts to index
* transitivity


## `bnd-baseline-maven-plugin`

plugin used to validate that a bundle correctly uses **semantic versioning** as described by the **OSGi Alliance**

*... and not semver.org*

### setup

```xml
<plugin>
  <groupId>biz.aQute.bnd</groupId>
  <artifactId>bnd-baseline-maven-plugin</artifactId>
  <configuration>...</configuration>
  <executions>
    <execution>
      <id>baseline</id>
      <goals><goal>baseline</goal></goals>
    </execution>
  </executions>
</plugin>
```

## `bnd-export-maven-plugin`

plugin to export **bndrun** files, *OOTB* as:

* bundles in a directory
* an executable jar
* an OSGi subsystem bundle

*... plugable export SPI*

### setup

```xml
<plugin>
  <groupId>biz.aQute.bnd</groupId>
  <artifactId>bnd-export-maven-plugin</artifactId>
  <configuration>...</configuration>
  <executions>
    <execution>
      <id>export</id>
      <goals><goal>export</goal></goals>
    </execution>
  </executions>
</plugin>
```

## `bnd-resolver-maven-plugin`

plugin to resolve **bndrun** files

*... verification that all requirements are satisfied*

### setup

```xml
<plugin>
  <groupId>biz.aQute.bnd</groupId>
  <artifactId>bnd-resolver-maven-plugin</artifactId>
  <configuration>...</configuration>
  <executions>
    <execution>
      <id>resolve</id>
      <goals><goal>resolve</goal></goals>
    </execution>
  </executions>
</plugin>
```

## `bnd-testing-maven-plugin`

plugin to run integration tests from **bndrun** files

### setup

```xml
<plugin>
  <groupId>biz.aQute.bnd</groupId>
  <artifactId>bnd-testing-maven-plugin</artifactId>
  <configuration>...</configuration>
  <executions>
    <execution>
      <id>testing</id>
      <goals><goal>testing</goal></goals>
    </execution>
  </executions>
</plugin>
```

## `bnd-run-maven-plugin`

plugin to *run* a **bndrun** file

### setup

```xml
<plugin>
  <groupId>biz.aQute.bnd</groupId>
  <artifactId>bnd-run-maven-plugin</artifactId>
  <configuration>...</configuration>
  <executions>
    <execution>
      <id>run</id>
      <goals><goal>run</goal></goals>
    </execution>
  </executions>
</plugin>
```

## `bnd-reporter-maven-plugin`

plugin to generate and export reports of projects

### setup

```xml
<plugin>
  <groupId>biz.aQute.bnd</groupId>
  <artifactId>bnd-reporter-maven-plugin</artifactId>
  <configuration>...</configuration>
</plugin>
```


<footer>@rotty3000</footer>

