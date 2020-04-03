## Configuring the Liferay workspace with a single Liferay "DXP" version

There are several properties in Liferay workspace where the version of Liferay has effect:

- `liferay.workspace.bundle.url`
- `liferay.workspace.target.platform.version`
- `liferay.workspace.docker.image.liferay`

In order to bring uniformity it is important to keep these values aligned.

### Workspace Bundle (`liferay.workspace.bundle.url`)

Working on Liferay workspace projects is rather tedious if you do not have a runtime with necessary configurations. This is important for local development and debugging and for implementing and executing integration tests, particularly in CI. Luckily this is easily handle in the workspace.

The `initBundle` gradle task is used to extract the bundle specified by the `liferay.workspace.bundle.url` property to the `liferay.workspace.home.dir` directory (default is the `bundles` directory in the workspace.) It will then overlay it with _environment_ configurations defined in the `configs` directory (`local` is the default environment).

You can have any number of _environments_ defined and a few are predefined. You can manually specify the environment to build with the `-Pliferay.workspace.environment=` command line argument to the `initBundle` task.

To obtaining a URL for DXP bundles:

1. begin by navigating to https://customer.liferay.com/en/downloads
2. select the desired **Product**
   *for this example we will choose `DXP 7.1`*
3. select the **File Type**
   *for this example we will choose `Fix Packs`*
4. scroll to the desired version in the resulting list
   *for this example we will choose [Liferay DXP 7.1 Fix Pack 16](https://customer.liferay.com/downloads/-/download/liferay-dxp-7-1-fix-pack-16)*
5. from the download selection list choose one of the **Bundle** options
   *for this example we will choose `Slim Fix Pack Bundle with Tomcat (tar.gz)`*
6. copy the url of the `Download` button by right clicking and choosing:
   * ***Copy link address*** in Chrome
   * ***Copy link*** in Edge
   * ***Copy Link Location*** in FireFox
7. paste the copied url into a text file temporarily
   e.g.`https://customer.liferay.com/group/customer/downloads?p_p_id=3_WAR_osbportlet&p_p_lifecycle=1&p_p_state=maximized&_3_WAR_osbportlet_fileName=/portal/7.1.10-dxp-16/liferay-dxp-tomcat-7.1.10-dxp-16-slim-20191218135106729.tar.gz`
8. in your `gradle.properties` file, start a new property with the following snippet `liferay.workspace.bundle.url=https://api.liferay.com/downloads`
9. from the previously copied URL extract the portion beginning with `/portal/` (e.g. `/portal/7.1.10-dxp-16/liferay-dxp-tomcat-7.1.10-dxp-16-slim-20191218135106729.tar.gz`)
10. append this portion to the ``liferay.workspace.bundle.url` started in _6._
    *the result should look similar to: `liferay.workspace.bundle.url=https://api.liferay.com/downloads/portal/7.1.10-dxp-16/liferay-dxp-tomcat-7.1.10-dxp-16-slim-20191218135106729.tar.gz`*

Finish by executing the `initBundle` task.

​	e.g. `./gradlew initBundle`

### Target Platform Version (`liferay.workspace.target.platform.version`)

Now that you have a bundle configured you can start creating custom modules in the workspace. You will quickly notice the need to specify dependencies for the many APIs you plan to use.

In order to simplify and greatly reduce risks involved with managing these dependencies and their versions you should enable the **Target Platform** feature which adds the Liferay BOMs for the target version of **Liferay DXP**.

1. begin by navigating to  https://search.maven.org/artifact/com.liferay.portal/release.dxp.bom

2. select the specific version which matches the bundle you've selected for **Workspace Bundle** 

   *for this example the matching version is`7.1.10.fp16`*

3. copy and paste this version as the value for `liferay.workspace.target.platform.version`
   e.g. `liferay.workspace.target.platform.version=7.1.10.fp16`

This enables the Target Platform workspace feature and applies the 3 following Liferay Platform BOMs:

- `com.liferay.portal:release.dxp.bom`
- `com.liferay.portal:release.dxp.bom.compile.only`
- `com.liferay.portal:release.dxp.bom.third.party`

To see exactly which BOMs are applied execute the following task at the root of the workspace:
`./gradlew dependencies --configuration default`

Finally, to see **ALL** the dependencies in the BOMs you can execute this task:
`./gradlew dependencies --configuration targetPlatformIDE`

### Docker Image (`liferay.workspace.docker.image.liferay`)

The final piece of the workspace affected by Liferay version is docker support.

Liferay workspace defines the `buildDockerImage` which creates a docker image starting from a base image defined by the `liferay.workspace.docker.image.liferay` property. Luckily Liferay DXP docker images are listed on Docker Hub.

1. begin by navigating to https://hub.docker.com/r/liferay/dxp/tags
2. select the tag that matches the version selected in **Workspace Bundle**
   *for this example the matching tag is `liferay/dxp:7.1.10-dxp-16`*
3. copy and paste the full tag name as the value for `liferay.workspace.docker.image.liferay`
   e.g. `liferay.workspace.docker.image.liferay=liferay/dxp:7.1.10-dxp-16`

Finally, you can build the docker image by executing the `buildDockerImage` task:

`./gradlew buildDockerImage`

The docker image is cached locally and can be started using either pure docker commands or other docker tasks provided by the workspace (see `./gradlew tasks`).