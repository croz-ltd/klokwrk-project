# Taking a Groovy on GraalVM native image journey
* **Author:** Damir Murat
* **Created:** 24.01.2021.
* **Updated:** 06.04.2021.

[GraalVM](https://www.graalvm.org/) [1] is a fascinating [open-source project](https://github.com/oracle/graal) [2]. It started as an effort to provide a
[high-performance polyglot](https://www.youtube.com/watch?v=9oHpAhgkNAY) [3] virtual machine. However, in the JVM ecosystem, it looks like most of the community interest comes from GraalVM ability to
create [high-performance, low-footprint, Ahead-Of-Time (AOT) compiled native images](https://www.youtube.com/watch?v=j9jIny7HsSo) [4] (provided as part of
[SubstrateVM](https://github.com/oracle/graal/blob/master/substratevm/README.md) [5] sub-project).

Many popular JVM frameworks (for example, [Micronaut](https://guides.micronaut.io/micronaut-creating-first-graal-app/guide/index.html) [6],
[Quarkus](https://quarkus.io/guides/building-native-image) [7], [Helidon](https://helidon.io/docs/latest/#/se/guides/36_graalnative) [8], and even
[Spring Framework](https://github.com/spring-projects-experimental/spring-native) [9]) adopted GraalVM native image support for statically compiled languages like Java and Kotlin. Unfortunately, some
other popular JVM languages, like Groovy, are left-out from out-of-the-box support.

This article will explore how to convert the Micronaut CLI application written in statically compiled Groovy into a GraalVM native image. It is expected that readers are familiar with Java and Groovy
languages and have some exposure to the ideas behind GraalVM, Ahead-Of-Time (AOT) compilation, and compilation into native images.

My interest in compiling Groovy native images was triggered and inspired by [Szymon Stepniak's](https://e.printstacktrace.blog/)
[work](https://e.printstacktrace.blog/graalvm-and-groovy-how-to-start/) [10] [in this](https://www.youtube.com/watch?v=BjO_vBzaB4c) [11] [area](https://www.youtube.com/watch?v=RPdugI8eZgo) [12].
As nothing comes in a vacuum, it coincided with a need for creating a [small command-line utility](../../../../tool/klokwrk-tool-gradle-source-repack/README.md) [13] and
[important improvements](https://www.graalvm.org/release-notes/20_3) [14] in GraalVM native image functionality. Those circumstances created a perfect environment for trying out GraalVM
native image compilation for Groovy.

## Introduction
Here at [CROZ](https://croz.net/), we recently started working on [Project Klokwrk](https://github.com/croz-ltd/klokwrk-project). One of many premises that we are trying to follow is returning as
often as possible to the open-source community. An essential part of this is [issue reporting](../../misc/klokwrkRelatedIssuesInTheWild.md) for tools and libraries that we use.

As Klokwrk uses Gradle as a build tool, exploratory debugging of Gradle build scripts, 3rd party plugins, and internal classes is a prerequisite for any issue report related to Gradle or its plugins.
To help with this process, we created the [klokwrk-tool-gradle-source-repack](../../../../tool/klokwrk-tool-gradle-source-repack/README.md) submodule. To explore its role in the process of debugging
Gradle from IntelliJ IDEA, please take a look at the "[Debugging Gradle internals from IntelliJ IDEA](../debugging-gradle-from-idea/debugging-gradle-from-idea.md)" [15] article.

As `klokwrk-tool-gradle-source-repack` is a CLI (command-line) utility build on top of Groovy, Micronaut, and [picocli](https://picocli.info/), it seemed natural to see if it is possible to package
it as a GraalVM native image. With Micronaut's [support](https://docs.micronaut.io/latest/guide/index.html#graal) [16] for GraalVM native images and provided
[integration](https://micronaut-projects.github.io/micronaut-picocli/latest/guide/) [17] with picocli, we already have a great starting point. We "just" need to add Groovy in the picture.

Therefore, this article will use [klokwrk-tool-gradle-source-repack](../../../../tool/klokwrk-tool-gradle-source-repack/README.md) as a working example. For a more comfortable
following of discussion and examples, it might be useful to clone/fork the [klokwrk-project](https://github.com/croz-ltd/klokwrk-project). For creating native images, you need to set up GraalVM and
its `native-image` tool. The easiest way for installing GraalVM is using [SDKMAN](https://sdkman.io/) as described at the beginning of Szymon Stepniak's
["GraalVM native-image - from 2.1s to 0.013s startup time | Groovy Tutorial"](https://www.youtube.com/watch?v=RPdugI8eZgo) video.

For this article we are using the following setup and assumptions:
- GraalVM Community 21.0.0 for OpenJDK 11 (SDKMAN identifier - `21.0.0.r11-grl`). GraalVM 21.0.0 OpenJDK 8 variant should also work.
- [gdub](https://github.com/gdubw/gdub) [18] utility for easier working with Gradle wrapper via `gw` command.
- macOS Catalina version 10.15.7.
- All demonstrated commands are executed from `klokwrk-project/tool/klokwrk-tool-gradle-source-repack` directory.

## Easy way to the Groovy native image
GraalVM native image support and tooling have come a long way over the past years. It always worked for Java, but for other languages, what once looked too
[complicated](https://melix.github.io/blog/2019/03/simple-http-server-graal.html) [19], becomes much [more straightforward](https://www.youtube.com/watch?v=RPdugI8eZgo) [12].

### Adapting Groovy Micronaut CLI application
In the case of the Groovy CLI Micronaut application, you can use [Micronaut Launch](https://micronaut.io/launch/) [20] for creating it. However, if you try to add the GraalVM feature, you will
discover that Micronaut does not support the Groovy/GraalVM combination. Fortunately, using the Diff and Preview options on the equivalent Java application, you can find that you only need to add a
single dependency in the `build.gradle` file:

```
...
dependencies {
  compileOnly("org.graalvm.nativeimage:svm")
  ...
}
```
Generated Micronaut Groovy CLI application includes [Micronaut Gradle Plugin](https://github.com/micronaut-projects/micronaut-gradle-plugin) [21], which supports compiling native images, among many
other excellent features. However, I encountered [[some]](https://github.com/micronaut-projects/micronaut-gradle-plugin/issues/92)
[[problems]](https://github.com/micronaut-projects/micronaut-gradle-plugin/issues/93) with the `nativeImage` task and decided to create a simplified version - `kwrkNativeImage`. Besides resolving
issues, it also allows slightly more direct control over native image compilation. To get more details, take a closer look at the `klokwrk-tool-gradle-source-repack's`
[build.gradle](../../../../tool/klokwrk-tool-gradle-source-repack/build.gradle) file.

### Generating GraalVM native image configuration files
GraalVM native image building relies on static analysis for detecting all reachable code paths. This might be problematic with any technology that uses reflection. Although
[native images support reflection](https://www.graalvm.org/reference-manual/native-image/Reflection/) [22], static analysis of reflective calls is simplified. It boils down to the cases where
parameters of reflective calls can be reduced to constants. Any elaborate logic that prepares reflective API parameters can cause static analysis to miss reflective API usage.

For cases that are not covered by the native image static analysis, GraalVM has a way for
[specifying configuration files](https://www.graalvm.org/reference-manual/native-image/BuildConfiguration/) [23] that can provide all missing elements. You can create configuration files manually,
but commonly they are produced with the help of the `native-image-agent`.

GraalVM [native-image-agent](https://github.com/oracle/graal/blob/master/substratevm/src/com.oracle.svm.agent/src/com/oracle/svm/agent/NativeImageAgent.java) is a Java agent intended to be used
with a running Java application. It [[intercepts]](https://github.com/oracle/graal/blob/master/substratevm/src/com.oracle.svm.agent/src/com/oracle/svm/agent/BreakpointInterceptor.java)
[[all reflective API calls]](https://github.com/oracle/graal/blob/master/substratevm/src/com.oracle.svm.agent/src/com/oracle/svm/agent/BreakpointInterceptor.java#L1327),
and based on those, [creates configuration files](https://github.com/oracle/graal/blob/master/substratevm/src/com.oracle.svm.configure/src/com/oracle/svm/configure/trace/ReflectionProcessor.java#L73)
for native image builder.

In the case of `klokwrk-tool-gradle-source-repack`, you can engage `native-image-agent` with a commands similar to the following:
```
gw clean assemble

mkdir build/native-image-agent

java -agentlib:native-image-agent=config-output-dir=build/native-image-agent \
-jar build/libs/klokwrk-tool-gradle-source-repack-0.0.4-SNAPSHOT-all.jar \
--loggingLevels=ROOT=INFO,org.klokwrk.tool.gradle.source.repack=DEBUG --cleanup=true 6.8.1
```

Here we are running `klokwrk-tool-gradle-source-repack-0.0.4-SNAPSHOT-all.jar` with corresponding application parameters (`--loggingLevels`, `--cleanup`, and `6.8.1` for Gradle version).
Simultaneously, we use `native-image-agent` with the `config-output-dir` parameter that specifies the directory where configuration files will be written. After running, in the
`build/native-image-agent` directory, we'll get a set of configuration files: `jni-config.json`, `proxy-config.json`, `reflect-config.json`, `resource-config.json`, and `serialization-config.json`.

To get the complete content of configuration files, we have to run our application with additional supported parameters and merge the configuration files' content. For this purpose,
`native-image-agent` supports `config-merge-dir` parameter (additional application parameter is `--version`):
```
java -agentlib:native-image-agent=config-merge-dir=build/native-image-agent \
-jar build/libs/klokwrk-tool-gradle-source-repack-0.0.4-SNAPSHOT-all.jar \
--version --loggingLevels=ROOT=INFO,org.klokwrk.tool.gradle.source.repack=DEBUG --cleanup=true 6.8.1
```

It is important to realize that `native-image-agent` and static analysis of the native image builder use completely different mechanisms. Thus, generated configuration files will contain many entries
that can be discovered by static analysis. Still, they will also include multiple entries that are not really needed for running a native image. Without any modification and filtering of generated
configuration files, this will result in images that are too big in their size since they contain classes, methods, and fields that are not actually used at the native image runtime.

### Building the native image
After the execution of previous commands, configuration files should be complete, and we can use them for creating the correct and complete native image. To do this, we must copy configuration files
at the location where `klokwrk-tool-gradle-source-repack`'s custom Gradle task, `kwrkNativeImage`, expects them:

```
cp build/native-image-agent/*.json src/main/graal
gw kwrkNativeImage
```

During the building of a native image, you will see several warnings similar to the following:
```
WARNING: Could not register reflection metadata for groovy.grape.GrapeIvy. Reason: java.lang.NoClassDefFoundError: org/apache/ivy/util/MessageLogger.
```

Those are a consequence of `native-image-agent` including every reflection call, even those expected to fail with catched exceptions. If a warning bothers you, edit `reflect-config.json` and remove
configuration entry mentioning `groovy.grape.GrapeIvy` class. We'll later explore how to filter configuration files more consistently.

After the building, the native image can be executed with a command similar to the following:
```
./build/native-image/klokwrk-tool-gradle-source-repack -Dmicronaut.cloud.platform=BARE_METAL \
--loggingLevels=ROOT=INFO,org.klokwrk.tool.gradle.source.repack=DEBUG --cleanup=true 6.8.1
```
Micronaut specific system property `micronaut.cloud.platform=BARE_METAL` squeezes several additional milliseconds from execution time at the startup.

### Progress so far
So far, we've achieved our primary goal, that is, creating a native image from the Groovy application through a relatively simple and straightforward process. If this is your only goal, and you don't
want to bother with further details, you can stop right here. However, before doing so, you might want to take a glance over the "Results Summary" section near the end of the article.

At this point, it is worth noting the size of the created native image. As a base, we will use the invalid image created without any configuration files. It does not work but can be useful for size
measurement:
* Size of the invalid base image created without any configuration files: 72 792 392 B (69,420 MB)
* Size of the image created with `native-image-agent` generated configuration files: 85 683 664 B (81,714 MB)

For further exploration, it is also useful to enumerate the pros and cons that we currently have:

* **pros**
  * Simple process for creating a fully functional native image of Groovy application.
* **cons**
  * Configuration files contain many unnecessary entries.
  * As a consequence of the previous point, the size of the native image is not optimal as it is larger than it needs to be.
  * We don't really know what is going on, meaning what is required to be included in configuration files, what is a surplus, and why.

## Creating native image configuration files by hand
Although we can create functional native images, we do not know much about the details yet. Our goal now is to get a more in-depth insight into what is exactly missing from static analysis.
With this knowledge, we hope to figure out a way to get smaller native images and a more maintainable configuration.

### Preparing the stage
Previously we've generated native image builder configuration files with the help of `native-image-agent`. They were generated in the `build/native-image-agent` directory. Then we copied them into
`src/main/graal` directory where `kwrkNativeImage` Gradle task expects them.

To get a clean start, in `src/main/graal` directory we need to edit the content of `reflect-config.json`, `proxy-config.json`, and `jni-config.json`, replacing them all with an empty JSON array:
```
[]
```
In the case of `resource-config.json`, we'll remove all entries that are either discoverable by static analysis or are already provided to the native image builder by libraries that we use. This
leaves us with the following content:
```
{
  "resources":{
  "includes":[
    {"pattern":"\\Qapplication.properties\\E"},
    {"pattern":"\\Qlogback.xml\\E"},
    {"pattern":"\\Qversion.properties\\E"}
  ]},
  "bundles":[]
}
```

Next, we can generate a new native image:
```
gw assemble
gw kwrkNativeImage
```

### First attempts
After generating a new image, we can run it as we did previously. We'll get an exception originating from `picocli` library:
```
./build/native-image/klokwrk-tool-gradle-source-repack -Dmicronaut.cloud.platform=BARE_METAL \
--loggingLevels=ROOT=INFO,org.klokwrk.tool.gradle.source.repack=DEBUG --cleanup=true 6.8.1

...
Exception in thread "main" picocli.CommandLine$InitializationException: Cannot instantiate org.klokwrk.tool.gradle.source.repack.cli.PropertiesVersionProvider: the class has no constructor
  at picocli.CommandLine$DefaultFactory.create(CommandLine.java:5514)
  at picocli.CommandLine$DefaultFactory.createVersionProvider(CommandLine.java:5500)
  at picocli.CommandLine$Model$CommandSpec.updateVersionProvider(CommandLine.java:7251)
  at picocli.CommandLine$Model$CommandSpec.updateCommandAttributes(CommandLine.java:7217)
  at picocli.CommandLine$Model$CommandReflection.extractCommandSpec(CommandLine.java:11392)
  at picocli.CommandLine$Model$CommandSpec.forAnnotatedObject(CommandLine.java:6202)
  at picocli.CommandLine.<init>(CommandLine.java:227)
  at picocli.CommandLine.<init>(CommandLine.java:221)
  at io.micronaut.configuration.picocli.PicocliRunner.run(PicocliRunner.java:136)
  at io.micronaut.configuration.picocli.PicocliRunner.run(PicocliRunner.java:114)
  at org.klokwrk.tool.gradle.source.repack.GradleSourceRepackCommand.main(GradleSourceRepackCommand.groovy:62)
Caused by: java.lang.NoSuchMethodException: org.klokwrk.tool.gradle.source.repack.cli.PropertiesVersionProvider.<init>()
  at java.lang.Class.getConstructor0(DynamicHub.java:3349)
  at java.lang.Class.getDeclaredConstructor(DynamicHub.java:2553)
  at picocli.CommandLine$DefaultFactory.create(CommandLine.java:5489)
  at io.micronaut.configuration.picocli.MicronautFactory.create(MicronautFactory.java:74)
  at picocli.CommandLine$DefaultFactory.create(CommandLine.java:5512)
  ... 10 more
```

To support all of its rich functionalities, `picocli` library uses reflection to inspect annotations on our custom `org.klokwrk.tool.gradle.source.repack.GradleSourceRepackCommand` command. Since
the `picocli` uses quite elaborate logic while handling reflection, native image static analysis cannot follow it and leaves out involved classes from the generated image. To alleviate the problem,
we need to add `org.klokwrk.tool.gradle.source.repack.cli.PropertiesVersionProvider` class in our `reflect-config.json`:
```
[
{
  "name":"org.klokwrk.tool.gradle.source.repack.cli.PropertiesVersionProvider",
  "allDeclaredFields":true,
  "allDeclaredMethods":true,
  "allPublicMethods":true,
  "allDeclaredConstructors":true
}
]
```
With this configuration, the native image builder will include all fields and methods of the `org.klokwrk.tool.gradle.source.repack.cli.PropertiesVersionProvider` class in the generated native image.
For more information about the exact configuration format, look at the native image builder [documentation](https://www.graalvm.org/reference-manual/native-image/Reflection/#manual-configuration) [24].

If we now regenerate the native image with the `kwrkNativeImage` task and try to execute it again, we'll get an error where `picocli` is complaining about unknown CLI options. The native image static
analysis didn't detect reflective usage of fields from `org.klokwrk.tool.gradle.source.repack.GradleSourceRepackCommand` class that `picocli` needs for generating all supported CLI options and
parameters. To resolve this, will add `GradleSourceRepackCommand` class in `reflect-config.json`:
```
[
{
  "name":"org.klokwrk.tool.gradle.source.repack.cli.PropertiesVersionProvider",
  ...
},
{
  "name":"org.klokwrk.tool.gradle.source.repack.GradleSourceRepackCommand",
  "allDeclaredFields":true,
  "allDeclaredMethods":true,
  "allPublicMethods":true,
  "allDeclaredConstructors":true
}
]
```

It is important to realize that up to this point, we've added only our custom **application classes** into native image builder configuration files.

### Default Groovy methods
After we iterate again and build and run the native image, we'll get another runtime exception:
```
./build/native-image/klokwrk-tool-gradle-source-repack -Dmicronaut.cloud.platform=BARE_METAL \
--loggingLevels=ROOT=INFO,org.klokwrk.tool.gradle.source.repack=DEBUG --cleanup=true 6.8.1

java.lang.ClassNotFoundException: org.codehaus.groovy.runtime.dgm$33
  at com.oracle.svm.core.hub.ClassForNameSupport.forName(ClassForNameSupport.java:60)
  at java.lang.ClassLoader.loadClass(ClassLoader.java:281)
  at org.codehaus.groovy.reflection.GeneratedMetaMethod$Proxy.createProxy(GeneratedMetaMethod.java:101)
  at org.codehaus.groovy.reflection.GeneratedMetaMethod$Proxy.proxy(GeneratedMetaMethod.java:93)
  at org.codehaus.groovy.reflection.GeneratedMetaMethod$Proxy.isValidMethod(GeneratedMetaMethod.java:78)
  at groovy.lang.MetaClassImpl.chooseMethodInternal(MetaClassImpl.java:3303)
  at groovy.lang.MetaClassImpl.chooseMethod(MetaClassImpl.java:3295)
  at groovy.lang.MetaClassImpl.getNormalMethodWithCaching(MetaClassImpl.java:1460)
  at groovy.lang.MetaClassImpl.getMethodWithCaching(MetaClassImpl.java:1376)
  at groovy.lang.MetaClassImpl.getMetaMethod(MetaClassImpl.java:1280)
  at groovy.lang.MetaClassImpl.invokeMethod(MetaClassImpl.java:1132)
  at groovy.lang.MetaClassImpl.invokeMethod(MetaClassImpl.java:1035)
  at org.codehaus.groovy.runtime.InvokerHelper.invokePojoMethod(InvokerHelper.java:1017)
  at org.codehaus.groovy.runtime.InvokerHelper.invokeMethod(InvokerHelper.java:1008)
  at org.codehaus.groovy.runtime.typehandling.DefaultTypeTransformation.castToBoolean(DefaultTypeTransformation.java:197)
  at org.codehaus.groovy.runtime.typehandling.DefaultTypeTransformation.booleanUnbox(DefaultTypeTransformation.java:86)
  at org.klokwrk.tool.gradle.source.repack.GradleSourceRepackCommand.configureCustomLoggingLevels(GradleSourceRepackCommand.groovy:138)
  at org.klokwrk.tool.gradle.source.repack.GradleSourceRepackCommand.run(GradleSourceRepackCommand.groovy:110)
  at picocli.CommandLine.executeUserObject(CommandLine.java:1939)
  at picocli.CommandLine.access$1300(CommandLine.java:145)
  at picocli.CommandLine$RunLast.executeUserObjectOfLastSubcommandWithSameParent(CommandLine.java:2352)
  at picocli.CommandLine$RunLast.handle(CommandLine.java:2346)
  at picocli.CommandLine$RunLast.handle(CommandLine.java:2311)
  at picocli.CommandLine$AbstractParseResultHandler.execute(CommandLine.java:2179)
  at picocli.CommandLine.execute(CommandLine.java:2078)
  at io.micronaut.configuration.picocli.PicocliRunner.run(PicocliRunner.java:137)
  at io.micronaut.configuration.picocli.PicocliRunner.run(PicocliRunner.java:114)
  at org.klokwrk.tool.gradle.source.repack.GradleSourceRepackCommand.main(GradleSourceRepackCommand.groovy:62)
  ...
```
This time it is about default Groovy methods (DGM). DGMs, or GDK as they are better known in the Groovy community, is a means through which Groovy enhances and expands the functionality of existing
standard JDK classes.

DGMs are implemented in classes like `DefaultGroovyMethods`, `StringGroovyMethods`, `IOGroovyMethods`,
[and others](https://github.com/apache/groovy/blob/GROOVY_3_0_X/src/main/java/org/codehaus/groovy/runtime/DefaultGroovyMethods.java#L214) from `org.codehaus.groovy.runtime` package. Although DGM
implementations are usually quite straightforward, the mechanism of their invocation from the user's compiled code is much more involved (as we can partially see from the stacktrace above). It
includes cooperation between the Groovy compiler, Groovy `MetaClass` mechanism, and various performance optimizations that include `org.codehaus.groovy.runtime.dgm$*` helper classes.

It is interesting to know that there are more than 1200 `dgm$*` classes in Groovy 3.0.x. All of them are generated during Groovy build and do not exist as artifacts in the
[codebase](https://github.com/apache/groovy/tree/GROOVY_3_0_X/src/main/java/org/codehaus/groovy/runtime). For our case, though, more important is that `dgm$*` classes are loaded reflectively via
complex logic that native image builder cannot track.

If we continue following the step-by-step process, we can add `org.codehaus.groovy.runtime.dgm$33` in the native image builder configuration file and regenerate the native image again. However,
in typical Groovy code, DGMs are used very frequently, and we can expect quite a few iterations that will involve adding `dgm$*` helpers. It would be great to add them all in a single step, if
possible. We wouldn't get an image of optimal size, but we will significantly reduce the number of iterations we need to take.

Fortunately, GraalVM native image builder is a Java application and can be extended via its `org.graalvm.nativeimage.hosted.Feature`
[interface](https://www.graalvm.org/sdk/javadoc/org/graalvm/nativeimage/hosted/Feature.html). Despite its unfortunate name, native image builder `Feature` might be very useful. It allows for
intercepting the native image generation and running a custom initialization code, including additions to the configuration.

This functionality is leveraged in `klokwrk-tool-gradle-source-repack` for building the
[GroovyDgmClassesRegistrationFeature](../../../../tool/klokwrk-tool-gradle-source-repack/src/main/java/org/klokwrk/tool/gradle/source/repack/graal/GroovyDgmClassesRegistrationFeature.java) extension
that includes all Groovy `dgm$*` classes in the native image. Although it adds too much stuff into the native image, it helps speed up our research. To enable the extension (it is disabled by
default), we need to edit [kwrk-graal.properties](../../../../tool/klokwrk-tool-gradle-source-repack/src/main/resources/kwrk-graal.properties):
```
kwrk-graal.registration-feature.dgm-classes.enabled = true
```
After that, the native image should be recreated again:
```
gw assemble
gw kwrkNativeImage
```
To restate, besides two application classes we've added previously, we now have all Groovy `dgm$*` classes included in the image. Consequently, we do not expect to see again any runtime exceptions
related to the default Groovy methods.

### Groovy generated closures and calling back into application classes
The next execution of the native image will greet us with the exception shown below. This time our native image executable cannot find the generated `doCall()` method of a Groovy closure.
```
./build/native-image/klokwrk-tool-gradle-source-repack -Dmicronaut.cloud.platform=BARE_METAL \
--loggingLevels=ROOT=INFO,org.klokwrk.tool.gradle.source.repack=DEBUG --cleanup=true 6.8.1

groovy.lang.MissingMethodException: No signature of method: org.klokwrk.tool.gradle.source.repack.GradleSourceRepackCommand$_configureCustomLoggingLevels_closure1.doCall() is applicable for argument types: (String) values: [ROOT=INFO]
Possible solutions: findAll(), findAll(), isCase(java.lang.Object), isCase(java.lang.Object)
  at org.codehaus.groovy.runtime.metaclass.ClosureMetaClass.invokeMethod(ClosureMetaClass.java:255)
  at groovy.lang.MetaClassImpl.invokeMethod(MetaClassImpl.java:1035)
  at groovy.lang.Closure.call(Closure.java:412)
  at groovy.lang.Closure.call(Closure.java:428)
  at org.codehaus.groovy.runtime.DefaultGroovyMethods.each(DefaultGroovyMethods.java:2318)
  at org.codehaus.groovy.runtime.DefaultGroovyMethods.each(DefaultGroovyMethods.java:2303)
  at org.codehaus.groovy.runtime.DefaultGroovyMethods.each(DefaultGroovyMethods.java:2344)
  at org.klokwrk.tool.gradle.source.repack.GradleSourceRepackCommand.configureCustomLoggingLevels(GradleSourceRepackCommand.groovy:139)
  at org.klokwrk.tool.gradle.source.repack.GradleSourceRepackCommand.run(GradleSourceRepackCommand.groovy:110)
  at picocli.CommandLine.executeUserObject(CommandLine.java:1939)
  at picocli.CommandLine.access$1300(CommandLine.java:145)
  at picocli.CommandLine$RunLast.executeUserObjectOfLastSubcommandWithSameParent(CommandLine.java:2352)
  at picocli.CommandLine$RunLast.handle(CommandLine.java:2346)
  at picocli.CommandLine$RunLast.handle(CommandLine.java:2311)
  at picocli.CommandLine$AbstractParseResultHandler.execute(CommandLine.java:2179)
  at picocli.CommandLine.execute(CommandLine.java:2078)
  at io.micronaut.configuration.picocli.PicocliRunner.run(PicocliRunner.java:137)
  at io.micronaut.configuration.picocli.PicocliRunner.run(PicocliRunner.java:114)
  at org.klokwrk.tool.gradle.source.repack.GradleSourceRepackCommand.main(GradleSourceRepackCommand.groovy:62)
```
To better understand what is going on, we first must familiarize ourselves with the way Groovy handles closures.

When the Groovy compiler encounters a closure in the source code, it will generate a class for it. To find the source of a closure we are talking about, it is useful to know that the generated
closure class name contains the surrounding class and method names. In our example, the generated closure class name is `GradleSourceRepackCommand$_configureCustomLoggingLevels_closure1` meaning
that we have a closure in `GradleSourceRepackCommand.configureCustomLoggingLevels()` method:
```
...
class GradleSourceRepackCommand implements Runnable {
  ...
  void configureCustomLoggingLevels() {
    if (loggingLevelConfigList) {
      loggingLevelConfigList.each { String loggingLevelConfig ->
        // ... closure body
      }
    }
  }
  ...
}
```
The next step is to look at the generated code. In the IDEA we can open corresponding `build/classes/groovy/main/org/klokwrk/tool/gradle/source/repack/GradleSourceRepackCommand.class` file and look
up for `configureCustomLoggingLevels()` method. It should be similar to the following:
```
  ...
  public void configureCustomLoggingLevels() {
    if (DefaultTypeTransformation.booleanUnbox(this.loggingLevelConfigList)) {
      final class _configureCustomLoggingLevels_closure1 extends Closure implements GeneratedClosure {
        public _configureCustomLoggingLevels_closure1(Object _outerInstance, Object _thisObject) {
          super(_outerInstance, _thisObject);
        }

        public Object doCall(String loggingLevelConfig) {
          // ... closure body
        }

        public Object call(String loggingLevelConfig) {
          return this.doCall(loggingLevelConfig);
        }
      }

      DefaultGroovyMethods.each(this.loggingLevelConfigList, new _configureCustomLoggingLevels_closure1(this, this));
    }
  }
  ...
```
As we can see, besides the constructor, the generated closure class contains `doCall()` and `call()` methods. If we correlate this with the exception we started with, it becomes clearer what happened.
The native image builder did detect the generated closure class and its constructor but was not able to track reflective invocation of the `doCall()` method as it involves elaborate logic implemented
in Groovy `MetaClass` mechanism.

To remedy the issue, we can add the generated closure class in the native image builder configuration file. However, we have a problem similar to the one we had with DGM classes. In typical Groovy
code, closures are used quite often, so we might end up with numerous iterations of updating configuration files and native image regenerations. For those reasons, `klokwrk-tool-gradle-source-repack`
provides another native image builder extension - [GroovyApplicationRegistrationFeature](../../../../tool/klokwrk-tool-gradle-source-repack/src/main/java/org/klokwrk/tool/gradle/source/repack/graal/GroovyApplicationRegistrationFeature.java).
It looks up and registers all Groovy generated closure classes with the native image builder to include them into the created native image. To enable the extension we need to edit again
[kwrk-graal.properties](../../../../tool/klokwrk-tool-gradle-source-repack/src/main/resources/kwrk-graal.properties) file:
```
kwrk-graal.registration-feature.application.enabled = true
```
Before regenerating the native image, we have one more issue to deal with. In Groovy, a [closure](https://groovy-lang.org/closures.html) [25] can reference variables declared in its surrounding
lexical scope. However, Groovy closures also support the concept of a [delegate](https://groovy-lang.org/closures.html#_delegation_strategy) [26] that allows referencing methods and properties from
any object that was proclaimed to be the delegate of the closure instance. Such behavior means that the closure body can reference any application's class method or field, even if they are private,
for example. Suppose it happens to be the only reference to those particular private methods or fields. In that case, the native image will not be aware of it since, as we saw previously, it does not
know about closure bodies (the content of generated closure class' `doCall()` method).

To simplify, closures can call back into any part of the application. Thus, we have to make sure all those called parts are included in the native image. During the native image building, there is no
easy way to do this in a fine-grained manner. Therefore, our `GroovyApplicationRegistrationFeature` includes all application classes in the build. Consequently, we can now delete two application
classes that we added to the `reflect-config.json` file earlier.

Including all application classes may look a bit extreme. Still, it can be good enough for smaller utilities or services, especially if we compare it to the 1200 DGM classes that we added already.
However, our current goal is still learning. We are not creating optimally sized images yet. At this point, the most crucial aspect is that we just learned that generated closure classes and
called-back application methods and fields need to be included in the native image.

We can now proceed with the next iteration and regenerate the native image:
```
gw assemble
gw kwrkNativeImage
```

### Groovy closure-to-SAM conversion and dynamic proxies
The next execution will end up with the following exception:
```
./build/native-image/klokwrk-tool-gradle-source-repack -Dmicronaut.cloud.platform=BARE_METAL \
--loggingLevels=ROOT=INFO,org.klokwrk.tool.gradle.source.repack=DEBUG --cleanup=true 6.8.1

...
org.codehaus.groovy.runtime.typehandling.GroovyCastException: Cannot cast object 'org.klokwrk.tool.gradle.source.repack.downloader.GradleDownloader$_download_closure1$_closure3@676d24b3' with class 'org.klokwrk.tool.gradle.source.repack.downloader.GradleDownloader$_download_closure1$_closure3' to class 'io.reactivex.functions.Function'
  at org.codehaus.groovy.runtime.typehandling.DefaultTypeTransformation.continueCastOnSAM(DefaultTypeTransformation.java:415)
  at org.codehaus.groovy.runtime.typehandling.DefaultTypeTransformation.continueCastOnNumber(DefaultTypeTransformation.java:329)
  at org.codehaus.groovy.runtime.typehandling.DefaultTypeTransformation.castToType(DefaultTypeTransformation.java:243)
  at org.codehaus.groovy.runtime.ScriptBytecodeAdapter.castToType(ScriptBytecodeAdapter.java:615)
  at org.klokwrk.tool.gradle.source.repack.downloader.GradleDownloader$_download_closure1.doCall(GradleDownloader.groovy:78)
  at java.lang.reflect.Method.invoke(Method.java:566)
  at org.codehaus.groovy.reflection.CachedMethod.invoke(CachedMethod.java:107)
  at groovy.lang.MetaMethod.doMethodInvoke(MetaMethod.java:323)
  at org.codehaus.groovy.runtime.metaclass.ClosureMetaClass.invokeMethod(ClosureMetaClass.java:263)
  at groovy.lang.MetaClassImpl.invokeMethod(MetaClassImpl.java:1035)
  at groovy.lang.Closure.call(Closure.java:412)
  at groovy.lang.Closure.call(Closure.java:428)
  at org.codehaus.groovy.runtime.IOGroovyMethods.withCloseable(IOGroovyMethods.java:1607)
  at org.klokwrk.tool.gradle.source.repack.downloader.GradleDownloader.download(GradleDownloader.groovy:76)
  at org.klokwrk.tool.gradle.source.repack.GradleSourceRepackCommand.fetchGradleDistributionZipFile(GradleSourceRepackCommand.groovy:181)
  at org.klokwrk.tool.gradle.source.repack.GradleSourceRepackCommand.run(GradleSourceRepackCommand.groovy:116)
  at picocli.CommandLine.executeUserObject(CommandLine.java:1939)
  at picocli.CommandLine.access$1300(CommandLine.java:145)
  at picocli.CommandLine$RunLast.executeUserObjectOfLastSubcommandWithSameParent(CommandLine.java:2352)
  at picocli.CommandLine$RunLast.handle(CommandLine.java:2346)
  at picocli.CommandLine$RunLast.handle(CommandLine.java:2311)
  at picocli.CommandLine$AbstractParseResultHandler.execute(CommandLine.java:2179)
  at picocli.CommandLine.execute(CommandLine.java:2078)
  at io.micronaut.configuration.picocli.PicocliRunner.run(PicocliRunner.java:137)
  at io.micronaut.configuration.picocli.PicocliRunner.run(PicocliRunner.java:114)
  at org.klokwrk.tool.gradle.source.repack.GradleSourceRepackCommand.main(GradleSourceRepackCommand.groovy:62)
```

Since the exception message mentions `io.reactivex.functions.Function` class, let's add it into the `reflect-config.json` file before we try to figure out what is happening:
```
[
  {
    "name":"io.reactivex.functions.Function",
    "allPublicMethods":true
  }
]
```

> When running the native image at this stage, you might get the exception saying something like `"SHA-256 does not match ... Cannot continue."`. In that case, just delete `gradle-6.8.1-all.*` files
> from your working directory, and try again.

After regenerating and executing the native image, we'll end up with the following:
```
./build/native-image/klokwrk-tool-gradle-source-repack -Dmicronaut.cloud.platform=BARE_METAL \
--loggingLevels=ROOT=INFO,org.klokwrk.tool.gradle.source.repack=DEBUG --cleanup=true 6.8.1

...
Exception in thread "main" com.oracle.svm.core.jdk.UnsupportedFeatureError: Proxy class defined by interfaces [interface io.reactivex.functions.Function] not found. Generating proxy classes at runtime is not supported. Proxy classes need to be defined at image build time by specifying the list of interfaces that they implement. To define proxy classes use -H:DynamicProxyConfigurationFiles=<comma-separated-config-files> and -H:DynamicProxyConfigurationResources=<comma-separated-config-resources> options.
  at com.oracle.svm.core.util.VMError.unsupportedFeature(VMError.java:87)
  at com.oracle.svm.reflect.proxy.DynamicProxySupport.getProxyClass(DynamicProxySupport.java:113)
  at java.lang.reflect.Proxy.getProxyConstructor(Proxy.java:66)
  at java.lang.reflect.Proxy.newProxyInstance(Proxy.java:1006)
  at org.codehaus.groovy.reflection.stdclasses.CachedSAMClass.coerceToSAM(CachedSAMClass.java:81)
  at org.codehaus.groovy.reflection.stdclasses.CachedSAMClass.coerceToSAM(CachedSAMClass.java:64)
  at org.codehaus.groovy.runtime.typehandling.DefaultTypeTransformation.continueCastOnSAM(DefaultTypeTransformation.java:364)
  at org.codehaus.groovy.runtime.typehandling.DefaultTypeTransformation.continueCastOnNumber(DefaultTypeTransformation.java:329)
  at org.codehaus.groovy.runtime.typehandling.DefaultTypeTransformation.castToType(DefaultTypeTransformation.java:243)
  at org.codehaus.groovy.runtime.ScriptBytecodeAdapter.castToType(ScriptBytecodeAdapter.java:615)
  at org.klokwrk.tool.gradle.source.repack.downloader.GradleDownloader$_download_closure1.doCall(GradleDownloader.groovy:78)
  at java.lang.reflect.Method.invoke(Method.java:566)
  at org.codehaus.groovy.reflection.CachedMethod.invoke(CachedMethod.java:107)
  at groovy.lang.MetaMethod.doMethodInvoke(MetaMethod.java:323)
  at org.codehaus.groovy.runtime.metaclass.ClosureMetaClass.invokeMethod(ClosureMetaClass.java:263)
  at groovy.lang.MetaClassImpl.invokeMethod(MetaClassImpl.java:1035)
  at groovy.lang.Closure.call(Closure.java:412)
  at groovy.lang.Closure.call(Closure.java:428)
  at org.codehaus.groovy.runtime.IOGroovyMethods.withCloseable(IOGroovyMethods.java:1607)
  at org.klokwrk.tool.gradle.source.repack.downloader.GradleDownloader.download(GradleDownloader.groovy:76)
  at org.klokwrk.tool.gradle.source.repack.GradleSourceRepackCommand.fetchGradleDistributionZipSha256File(GradleSourceRepackCommand.groovy:196)
  at org.klokwrk.tool.gradle.source.repack.GradleSourceRepackCommand.run(GradleSourceRepackCommand.groovy:117)
  at picocli.CommandLine.executeUserObject(CommandLine.java:1939)
  at picocli.CommandLine.access$1300(CommandLine.java:145)
  at picocli.CommandLine$RunLast.executeUserObjectOfLastSubcommandWithSameParent(CommandLine.java:2352)
  at picocli.CommandLine$RunLast.handle(CommandLine.java:2346)
  at picocli.CommandLine$RunLast.handle(CommandLine.java:2311)
  at picocli.CommandLine$AbstractParseResultHandler.execute(CommandLine.java:2179)
  at picocli.CommandLine.execute(CommandLine.java:2078)
  at io.micronaut.configuration.picocli.PicocliRunner.run(PicocliRunner.java:137)
  at io.micronaut.configuration.picocli.PicocliRunner.run(PicocliRunner.java:114)
  at org.klokwrk.tool.gradle.source.repack.GradleSourceRepackCommand.main(GradleSourceRepackCommand.groovy:62)
```

Again, the exception mentions the` io.reactivex.functions.Function` class and the native image complains about generating proxy classes at runtime. It also advises about the possibility to configure
the proxy classes at the build time. The default location for providing such configuration is the `proxy-config.json` file. However, let's dive deeper and try to find out why we got those two
exceptions in the first place. We'll start by examining the relevant code section.

In the `download()` method of the [GradleDownloader](../../../../tool/klokwrk-tool-gradle-source-repack/src/main/groovy/org/klokwrk/tool/gradle/source/repack/downloader/GradleDownloader.groovy) class,
we have the following section. Our point of interest is the `map()` method call:
```
File download(GradleDownloaderInfo gradleDownloaderInfo) {
  ...
  new BufferedOutputStream(...).withCloseable { BufferedOutputStream fileOutputStream ->
    ...
    streamingHttpClient.exchangeStream(HttpRequest.GET(realDownloadUrl).accept(MediaType.APPLICATION_OCTET_STREAM_TYPE))
                       .map({ HttpResponse<ByteBuffer<?>> byteBufferHttpResponse ->  // <=== our point of interest
                         ...
                       })
                       .blockingSubscribe(
                         ...
                       )
  }

  return new File(gradleDownloaderInfo.downloadTargetFileAbsolutePath)
}
```
Method `map()` (defined on `io.reactivex.Flowable` class) accepts the argument of type `io.reactivex.functions.Function`. Interface `io.reactivex.functions.Function` is a functional interface,
also known as Single Abstract Method (SAM) interface, meaning it contains a single abstract method (the method without concrete implementation).

In Groovy, we can implement SAM interfaces by providing a closure as a concrete implementation
([Closure to type coercion](https://docs.groovy-lang.org/latest/html/documentation/core-semantics.html#closure-coercion) [27]). Groovy will convert the closure into the appropriate SAM type. If we do
not provide an explicit target type, the conversion is implicit. Implicit conversion is also called **coercion**. On the other hand, when the target type is listed, we have an explicit conversion,
also known as **cast**:

```
// closure coercion (implicit conversion) into SAM type
(1..10).forEach({ println it })

// closure cast (explicit conversion) into SAM type
(1..10).forEach({ println it } as java.util.function.Consumer)
```

While handling closure-to-SAM conversions, at one point, besides all other stuff, Groovy will do two things relevant in the context of creating a native image. It will create a dynamic proxy for SAM
interfaces, and it will use the `getMethods()` method on SAM interface classes. Both actions originate from `org.codehaus.groovy.reflection.stdclasses.CachedSAMClass` class, which is a part of a
mechanism for dealing with closure-to-SAM conversions.

As already mentioned, GraalVM native image builder does not allow creating dynamic proxies at runtime. However, they can be created at build time if they are listed in the appropriate configuration
file, which is `proxy-config.json` by default. This will handle the second exception that we got above. But what about the first exception?

If we have an explicit closure-to-SAM conversion in our code, we do not have to do anything. Native image builder will detect a direct reference to the SAM class and will include all its public
methods in the image. Therefore, they will be available for the `getMethods()` call on the SAM interface class. But this will not happen for implicit closure-to-SAM conversions. In that case, we have
to list SAM classes in `reflect-config.json` to make them available at the native image execution time. Since the usage of implicit conversion is much more Groovy idiomatic, it will be a prevalent
case. For this reason, we can simplify things a bit and include all interfaces from `proxy-config.json` into `reflect-config,json`.

If we now continue with native-image build iterations, we'll end up with following configuration files:
```
// proxy-config.json
[
  ["io.reactivex.functions.Action"],
  ["io.reactivex.functions.Consumer"],
  ["io.reactivex.functions.Function"],
  ["java.util.function.Consumer"],
  ["java.util.function.Function"],
  ["java.util.function.Predicate"],
  ["java.util.function.Supplier"]
]

// reflect-config.json
[
  {
    "name":"io.reactivex.functions.Action",
    "allPublicMethods":true
  },
  {
    "name":"io.reactivex.functions.Consumer",
    "allPublicMethods":true
  },
  {
    "name":"io.reactivex.functions.Function",
    "allPublicMethods":true
  },
  {
    "name":"java.util.function.Consumer",
    "allPublicMethods":true
  },
  {
    "name":"java.util.function.Function",
    "allPublicMethods":true
  },
  {
    "name":"java.util.function.Predicate",
    "allPublicMethods":true
  },
  {
    "name":"java.util.function.Supplier",
    "allPublicMethods":true
  }
]
```
After rebuilding the image, it will execute correctly, meaning that we managed to create a fully functional native image. It is worth noting that the content of `proxy-config.json` is the same as
the one we got in the first section - "Easy way to the Groovy native image", when we've run the `native-image-agent` on our CLI application. This provides us with a nice shortcut for creating the
proxy/SAM related content of corresponding `reflect-config.json` since we can just copy and expand configuration entries.

### What we learned
We just passed through pretty excessive exercise. It was demanding, but we've learned a great deal about the requirements for creating a GraalVM native image for the Groovy CLI application.
Let's summarize things a bit.

#### Our gained "knowledge-bullet-list"
If we start with empty configuration files, we'll get an invalid native image. To make it functional, we must include following things:
- Necessary parts of `$dgm*` helper classes that correspond to the default Groovy methods (DGM) used in the application.
- Necessary parts of generated closure classes corresponding to the Groovy closures used in the application.
- All application parts that are called back from generated closure classes.
- All methods of SAM classes that the application uses during Groovy handling of implicit closure-to-SAM conversions.
- The native image proxy configuration file must declare all interfaces for which the application creates dynamic proxies during Groovy handling of implicit and explicit closure-to-SAM conversions.

Hopefully, in the next step, that acquired knowledge will help us develop a streamlined process of creating the native image with optimal size.

## Getting optimally sized native image via filtering configuration files
Based on our findings, in this section, we'll try to utilize `native-image-agent` to come up with the set of configuration files for generating an optimally sized native image. For this purpose
we will leverage several additional `native-image-agent` options.

### Trace file of `native-image-agent`
For better understanding of dynamic and reflective calls during execution, `native-image-agent` provides the ability for
[creating a trace file](https://www.graalvm.org/reference-manual/native-image/BuildConfiguration/#trace-files) [28] that contains each individual access. In our case, the trace file can be created
with a commands similar to the following:

```
gw clean assemble

mkdir build/native-image-agent

java -agentlib:native-image-agent=trace-output=build/native-image-agent/agent-trace-file.json \
-jar build/libs/klokwrk-tool-gradle-source-repack-0.0.4-SNAPSHOT-all.jar --loggingLevels=ROOT=INFO,org.klokwrk.tool.gradle.source.repack=DEBUG --cleanup=true 6.8.1
```

Created `build/native-image-agent/agent-trace-file.json` is pretty big (around 13 MB for our case), and you might want to use the editor that can efficiently work with and search through large text
files. If we take a quick look into the generated trace file, we can find records that look something like this:
```
[
...
{"caller_class":"org.codehaus.groovy.reflection.stdclasses.CachedSAMClass", "args":[], "function":"getMethods", "tracer":"reflect", "class":"io.reactivex.functions.Action"},
{"caller_class":"org.codehaus.groovy.reflection.stdclasses.CachedSAMClass", "result":false, "args":["run",[]], "function":"getMethod", "tracer":"reflect", "class":"java.lang.Object"},
{"caller_class":"org.codehaus.groovy.reflection.stdclasses.CachedSAMClass", "result":true, "args":["\u0000",["io.reactivex.functions.Action"],"\u0000"], "function":"newProxyInstance", "tracer":"reflect"},
...
]
```
Here we see the caller class (`caller_class`), the target class (`class`), the method invoked on the target class (`function`), and arguments (`args`) of the invoked method. All those pieces of
information will help as guidance during creation of filtering configuration files.

### The `native-image-agent` filtering
The `native-image-agent` supports two kinds of [filtering](https://www.graalvm.org/reference-manual/native-image/BuildConfiguration/#agent-advanced-usage) [29], caller-based, and access-based. With
caller-based filtering, we can ignore a set of classes that triggered a specific reflective method call. As a result, the native image builder configuration files will not contain entries
resulted from those filtered-out method calls. Access-based filtering works from the other way around, ignoring method calls based on their target class.

Both types of filtering require a corresponding configuration file with a specific structure, similar to the following example. All filtering configuration records are processed in order as they are
specified.
```
{
  "rules": [
    ...
    { "excludeClasses": "org.codehaus.groovy.reflection.stdclasses.**" },
    { "includeClasses": "org.codehaus.groovy.reflection.stdclasses.CachedSAMClass" },
    ...
    { "excludeClasses": "io.netty.**" },
    { "excludeClasses": "java.security.**" },
    ...
  ]
}
```
If this example is used for caller-based filtering, all methods calls triggered from `org.codehaus.groovy.reflection.stdclasses` package and its subpackages are ignored, except of calls triggered
from `org.codehaus.groovy.reflection.stdclasses.CachedSAMClass` class.

### Preparing for filtering configuration
In the case of the `klokwrk-tool-gradle-source-repack` utility, filtering configuration files are `src/main/resources/graal-agent-caller-filter.json` for caller-based filtering and
`src/main/resources/graal-agent-access-filter.json` for access-based filtering.

However, before we start with filtering, we must first reset some of our previous steps. Make sure our custom registration features are disabled:
```
// kwrk-graal.properties
...
kwrk-graal.registration-feature.dgm-classes.enabled = false
...
kwrk-graal.registration-feature.application.enabled = false
```
In addition, let's remove all entries from `graal-agent-caller-filter.json` and `graal-agent-access-filter.json`:
```
{
  "rules": [
  ]
}
```

Now we can start with the first iteration:
```
gw clean assemble

mkdir build/native-image-agent

# creating trace file
java -agentlib:native-image-agent=trace-output=build/native-image-agent/agent-trace-file.json \
-jar build/libs/klokwrk-tool-gradle-source-repack-0.0.4-SNAPSHOT-all.jar --loggingLevels=ROOT=INFO,org.klokwrk.tool.gradle.source.repack=DEBUG --cleanup=true 6.8.1

# creating native image builder configuration files
java -agentlib:native-image-agent=access-filter-file=build/resources/main/graal-agent-access-filter.json,caller-filter-file=build/resources/main/graal-agent-caller-filter.json,config-output-dir=build/native-image-agent \
-jar build/libs/klokwrk-tool-gradle-source-repack-0.0.4-SNAPSHOT-all.jar --loggingLevels=ROOT=INFO,org.klokwrk.tool.gradle.source.repack=DEBUG --cleanup=true 6.8.1
```

### Iterating over caller-based filtering configuration
After the initial iteration, we'll get full native image builder configuration (`reflect-config.json`, `proxy-config.json`, etc.) as we did in the first section - "Easy way to the Groovy native
image". Based on gained information from the "Creating native image configuration files by hand" section, we'll now start iterating over the `native-image-agent` caller-based filtering
configuration - `graal-agent-caller-filter.json`. We'll do this by comparing the content of `reflect-config.json`, `agent-trace-file.json`, and our "knowledge-bullet-list" at the end of the
"Creating native image configuration files by hand" section.

For example, if we examine the content of the generated `reflect-config.json`, we can find entries for various classes from `ch.qos.logback` subpackages. By looking for them in the
`agent-trace-file.json` file, we can see they are called from the same `logback` subpackages. Since our "knowledge-bullet-list" does not include any `logback` classes, we can exclude them explicitly
in the `graal-agent-caller-filter.json`:
```
{
  "rules": [
    { "excludeClasses": "ch.qos.logback.**" }
  ]
}
```
By repeating the same exercise for other obvious cases present in the generated `reflect-config.json`, we can relatively quickly come up with the following `graal-agent-caller-filter.json` content:
```
{
  "rules": [
    { "excludeClasses": "ch.qos.logback.**" },
    { "excludeClasses": "io.micronaut.**" },
    { "excludeClasses": "io.netty.**" },
    { "excludeClasses": "java.security.**" },
    { "excludeClasses": "javax.xml.**" },
    { "excludeClasses": "picocli.**" },
    { "excludeClasses": "sun.management.**" },
    { "excludeClasses": "sun.security.**" }
  ]
}
```

With this configuration we can start the next iteration:
```
gw assemble

# regenerating native image builder configuration files
java -agentlib:native-image-agent=access-filter-file=build/resources/main/graal-agent-access-filter.json,caller-filter-file=build/resources/main/graal-agent-caller-filter.json,config-output-dir=build/native-image-agent \
-jar build/libs/klokwrk-tool-gradle-source-repack-0.0.4-SNAPSHOT-all.jar --loggingLevels=ROOT=INFO,org.klokwrk.tool.gradle.source.repack=DEBUG --cleanup=true 6.8.1

# merging configuration files for additional application options
java -agentlib:native-image-agent=access-filter-file=build/resources/main/graal-agent-access-filter.json,caller-filter-file=build/resources/main/graal-agent-caller-filter.json,config-merge-dir=build/native-image-agent \
-jar build/libs/klokwrk-tool-gradle-source-repack-0.0.4-SNAPSHOT-all.jar --version --loggingLevels=ROOT=INFO,org.klokwrk.tool.gradle.source.repack=DEBUG --cleanup=true 6.8.1
```

After examination, we can see that the generated `reflect-config.json` is reduced from around 2200 lines to less than 1000 lines. This is a very good start. At this point you might want to assure
yourself that native image is still fully functional. First, we need to copy the content of the generated `build/native-image-agent/proxy-config.json` into `src/main/graal/reflect-config.json` and
run the commands bellow.

> If you are working on the Mac with GraalVM 20.3.0, the following commands might produce an error during the linking of the native image. The workaround is to remove the `java.lang.ClassLoader`
> entry from the `src/main/graal/reflect-config.json` file. The error doesn't occur with GraalVM 21.0.0.

```
gw kwrkNativeImage

# "normal" execution
./build/native-image/klokwrk-tool-gradle-source-repack -Dmicronaut.cloud.platform=BARE_METAL \
--loggingLevels=ROOT=INFO,org.klokwrk.tool.gradle.source.repack=DEBUG --cleanup=true 6.8.1

# execution with additional options
./build/native-image/klokwrk-tool-gradle-source-repack -Dmicronaut.cloud.platform=BARE_METAL \
--version --loggingLevels=ROOT=INFO,org.klokwrk.tool.gradle.source.repack=DEBUG --cleanup=true 6.8.1
```

At this point, we are at the most challenging phase. We have to conclude which classes from the Groovy runtime can be ignored and which cannot. We have to keep an eye on our "knowledge-bullet-list",
and verify in each iteration if generated `reflect-config.json` and `proxy-config.json` contain all required entries. After some picking, digging and comparing through multiple generated files, at
the end we'll come up with caller-based filtering configuration (`graal-agent-caller-filter.json`) that should be very similar to the following:
```
{
  "rules": [
    { "excludeClasses": "org.codehaus.groovy.reflection.**" },
    { "includeClasses": "org.codehaus.groovy.reflection.CachedClass$1" },
    { "includeClasses": "org.codehaus.groovy.reflection.CachedClass$2" },
    { "includeClasses": "org.codehaus.groovy.reflection.CachedClass$3" },
    { "includeClasses": "org.codehaus.groovy.reflection.GeneratedMetaMethod$Proxy" },
    { "includeClasses": "org.codehaus.groovy.reflection.stdclasses.CachedSAMClass" },

    { "excludeClasses": "org.codehaus.groovy.runtime.**" },
    { "includeClasses": "org.codehaus.groovy.runtime.DefaultGroovyMethods" },

    { "excludeClasses": "org.codehaus.groovy.vmplugin.**" },

    ...
  ]
}
```
We've excluded all classes from `org.codehaus.groovy.reflection` and `org.codehaus.groovy.runtime` subpackages except a handful of explicitly included classes. Hopefully, this configuration is not
specific only to the `klokwrk-tool-gradle-source-repack` utility. It should work for the majority of Groovy CLI applications and maybe for other application types too.

We do not have an optimal native image builder configuration yet, as it still contains many entries that are not necessary according to the "knowledge-bullet-list". However, we have exhausted
caller-based filtering possibilities and need to turn to access-based filtering.

Before moving on, make sure that caller-based filtering configuration works as expected by repeating the above process of generating native image builder configuration files and creating and checking
native image workings. For reference, here is a full content of caller-based filtering configuration, which is our input to the next step of the process:
```
// The final content of caller-based filtering configuration - graal-agent-caller-filter.json
{
  "rules": [
    { "excludeClasses": "org.codehaus.groovy.reflection.**" },
    { "includeClasses": "org.codehaus.groovy.reflection.CachedClass$1" },
    { "includeClasses": "org.codehaus.groovy.reflection.CachedClass$2" },
    { "includeClasses": "org.codehaus.groovy.reflection.CachedClass$3" },
    { "includeClasses": "org.codehaus.groovy.reflection.GeneratedMetaMethod$Proxy" },
    { "includeClasses": "org.codehaus.groovy.reflection.stdclasses.CachedSAMClass" },

    { "excludeClasses": "org.codehaus.groovy.runtime.**" },
    { "includeClasses": "org.codehaus.groovy.runtime.DefaultGroovyMethods" },

    { "excludeClasses": "org.codehaus.groovy.vmplugin.**" },

    { "excludeClasses": "ch.qos.logback.**" },
    { "excludeClasses": "io.micronaut.**" },
    { "excludeClasses": "io.netty.**" },
    { "excludeClasses": "java.security.**" },
    { "excludeClasses": "javax.xml.**" },
    { "excludeClasses": "picocli.**" },
    { "excludeClasses": "sun.management.**" },
    { "excludeClasses": "sun.security.**" }
  ]
}
```

### Creating access-based filtering configuration
With access-based filtering, our primary goal is to remove the final set of unnecessary entries from native image builder configurations in the generated `reflect-config.json` file. While doing this,
we must continue monitoring the `reflect-config.json` against the "knowledge-bullet-list" to make sure we don't remove too much.

The current surplus in the `reflect-config.json` content is a result of explicitly included classes (`CachedSAMClass`, `CachedSAMClass$*`, etc.) that we have in our final version of caller-based
filtering configuration for `native-image-agent`. Those classes are necessary as they call target classes that need to be in the native image, but they also call many other target classes that
are not required.

By examining our current version of `reflect-config.json`, we can relatively quickly construct the first set of access-based filtering rules, which will sweep out the most apparent unneeded entries.
The content of the `graal-agent-access-filter.json` file will look like this:
```
{
  "rules": [
    { "excludeClasses": "groovy.lang.**" },
    { "excludeClasses": "org.codehaus.groovy.runtime.callsite.**" },
    { "excludeClasses": "org.codehaus.groovy.vmplugin.**" },

    { "excludeClasses": "io.micronaut.**" },
    { "excludeClasses": "picocli.**" },

    { "excludeClasses": "java.beans.**" },
    { "excludeClasses": "java.io.**" },
    { "excludeClasses": "java.lang.**" },
    { "excludeClasses": "java.net.**" },
    { "excludeClasses": "java.nio.**" }
  ]
}
```
For progressing to the next iteration, we should regenerate native image builder configuration files again:
```
gw assemble

# regenerating native image builder configuration files
java -agentlib:native-image-agent=access-filter-file=build/resources/main/graal-agent-access-filter.json,caller-filter-file=build/resources/main/graal-agent-caller-filter.json,config-output-dir=build/native-image-agent \
-jar build/libs/klokwrk-tool-gradle-source-repack-0.0.4-SNAPSHOT-all.jar --loggingLevels=ROOT=INFO,org.klokwrk.tool.gradle.source.repack=DEBUG --cleanup=true 6.8.1

# merging configuration files for additional application options
java -agentlib:native-image-agent=access-filter-file=build/resources/main/graal-agent-access-filter.json,caller-filter-file=build/resources/main/graal-agent-caller-filter.json,config-merge-dir=build/native-image-agent \
-jar build/libs/klokwrk-tool-gradle-source-repack-0.0.4-SNAPSHOT-all.jar --version --loggingLevels=ROOT=INFO,org.klokwrk.tool.gradle.source.repack=DEBUG --cleanup=true 6.8.1
```

Now, we can easily spot that `reflect-config.json` contains many unnecessary entries from `java.util` subpackages. However, we cannot exclude all `java.util` subpackages since this will also exclude
SAM classes from `java.util.function` package. As we know, some of them must be included to support dynamic Groovy closure-to-SAM conversion. Therefore, we can exclude all `java.util` subpackages
except `java.util.function` package from our access-based filtering rules:

```
{
  "rules": [
    ...
    { "excludeClasses": "java.util.**" },
    { "includeClasses": "java.util.function.**" }
  ]
}
```
We can argue here that `reflect-config.json` will not be minimal with this config. It is true. We've included all 14 classes from `java.util.function` despite the fact we need only 3. However, our
access-based rules are still pretty generic and might work for other applications as well, which might be much more valuable than squeezing few more kilobytes from the native image. It is worth
reminding ourselves that all of these 14 classes are SAM classes, which will add a minimal number of methods for each SAM class in the native image.

After regenerating native image builder configuration files again, we can look for the last leftovers in the `reflect-config.json` file. In the end, we'll come up with the final version of
access-based filtering rules:
```
// The final content of access-based filtering configuration - graal-agent-access-filter.json
{
  "rules": [
    { "excludeClasses": "groovy.lang.**" },
    { "excludeClasses": "org.codehaus.groovy.runtime.DefaultGroovyMethodsSupport" },
    { "excludeClasses": "org.codehaus.groovy.runtime.DefaultGroovyStaticMethods" },
    { "excludeClasses": "org.codehaus.groovy.runtime.GStringImpl" },
    { "excludeClasses": "org.codehaus.groovy.runtime.GeneratedClosure" },
    { "excludeClasses": "org.codehaus.groovy.runtime.callsite.**" },
    { "excludeClasses": "org.codehaus.groovy.vmplugin.**" },

    { "excludeClasses": "io.micronaut.**" },
    { "excludeClasses": "picocli.**" },

    { "excludeClasses": "java.beans.**" },
    { "excludeClasses": "java.io.**" },
    { "excludeClasses": "java.lang.**" },
    { "excludeClasses": "java.net.**" },
    { "excludeClasses": "java.nio.**" },

    { "excludeClasses": "java.util.**" },
    { "includeClasses": "java.util.function.**" }
  ]
}
```

We now have all ingredients for creating the final version of the native image. Caller-based and access-based `native-image-agent` configurations are finalized and ready for generating native image
builder configuration files `reflect-config.json`, `proxy-config.json`, `jni-config.json` and `serialization-config.json`. Generated `resource-config.json` still includes many unnecessary entries
that can be reduced manually only to the ones we actually use. In our case, we'll use a ready-made version from the `src/main/graal` directory.

For reference, here is a list of all commands for creating and testing the generated native image with optimal size:
```
# Fresh start.
gw clean assemble
mkdir build/native-image-agent

# Generating native image builder configuration files.
java -agentlib:native-image-agent=access-filter-file=build/resources/main/graal-agent-access-filter.json,caller-filter-file=build/resources/main/graal-agent-caller-filter.json,config-output-dir=build/native-image-agent \
-jar build/libs/klokwrk-tool-gradle-source-repack-0.0.4-SNAPSHOT-all.jar --loggingLevels=ROOT=INFO,org.klokwrk.tool.gradle.source.repack=DEBUG --cleanup=true 6.8.1

# Generating and merging native image builder configuration files for additional application options.
java -agentlib:native-image-agent=access-filter-file=build/resources/main/graal-agent-access-filter.json,caller-filter-file=build/resources/main/graal-agent-caller-filter.json,config-merge-dir=build/native-image-agent \
-jar build/libs/klokwrk-tool-gradle-source-repack-0.0.4-SNAPSHOT-all.jar --version --loggingLevels=ROOT=INFO,org.klokwrk.tool.gradle.source.repack=DEBUG --cleanup=true 6.8.1

# Copy generated configuration file for native image builder
cp build/native-image-agent/jni-config.json src/main/graal
cp build/native-image-agent/serialization-config.json src/main/graal
cp build/native-image-agent/proxy-config.json src/main/graal
cp build/native-image-agent/reflect-config.json src/main/graal

# Invoking native image builder.
gw kwrkNativeImage

# Native image "normal" execution.
./build/native-image/klokwrk-tool-gradle-source-repack -Dmicronaut.cloud.platform=BARE_METAL \
--loggingLevels=ROOT=INFO,org.klokwrk.tool.gradle.source.repack=DEBUG --cleanup=true 6.8.1

# Native image execution with additional options.
./build/native-image/klokwrk-tool-gradle-source-repack -Dmicronaut.cloud.platform=BARE_METAL \
--version --loggingLevels=ROOT=INFO,org.klokwrk.tool.gradle.source.repack=DEBUG --cleanup=true 6.8.1
```

### What we've achieved
We've managed to create an optimally sized GraalVM native image for the Groovy CLI application. If you look at the `build/native-image` directory, you can find that the image's size is
74907872 B (71,438 MB). It is useful to compare this size to the size of the invalid base image (69,420 MB) and the size of the image created without filtering configuration files (81,714 MB). We can
see that we got a fully functional image with only a 2 MB increase in native image size and that the size of our latest image is 10 MB smaller than the image we got without filtering.

Once we came up with the final versions of filtering configuration files for the `native-image-agent`, the process of creating the native image remains simple and straightforward.

However, probably the most valuable thing is the knowledge we've gained during the process. Now it is much more apparent what needs to be included in the image and why. We have filtering
configurations working for our case and probably for most other cases that use the same tech stack for CLI applications (Groovy, Micronaut, picocli). Hopefully, for other application types, we'll
leverage our know-how to get to the native image faster and more consistently without much guessing.

In the end, to get a better feeling about our accomplishments, let's enumerate pros and cons again as we did in the first section - "Easy way to the Groovy native image":
* **pros**
  * Simple process for creating a fully functional native image of Groovy application.
  * Native image agent filtering configuration is pretty generic. Although it might require updates, it is expected this will not happen frequently.
  * Native image builder configuration files contain a minimal set of required entries.
  * The size of the native image is optimal.
  * We now know what is going on, and what is required to be included in configuration files, what was a surplus, and why.
* **cons**
  * Automation of the process is missing, but this is a topic for some other article.

### Results Summary
This section presents a summary and comparison of sizes for images that we have created along the way. If you are mainly interested in results, given data might help you decide if tuning the image
size provides enough benefits for your case.

- "no-config": invalid base image without any native builder configuration
- "tuned-config": an optimally sized image with tuned and filtered native builder configuration
- "all-inclusive": the image with "all-inclusive" native builder configuration created by java agent

**1. Size increase relatively to the non-functional base image:**

|                |size (B)    | size increase |
|----------------|-----------:|--------------:|
|**no-config**   | 72.792.392 | -
|**tuned-config**| 74.907.872 | 2,9 %

**2. Size decrease relatively to the all-inclusive image:**

|                 |size (MB)   | size decrease  |
|-----------------|-----------:|---------------:|
|**all-inclusive**| 85.683.664 | -
|**tuned-config** | 74.907.872 | 12,6 %

## Conclusion
GraalVM native image is an exciting technology adopted and supported by many popular frameworks. Because of some limitations related to the static analysis of reflective code, that support is usually
limited on the JVM languages that do not use much reflection internally. Groovy is not in that category since it uses reflection extensively for many of its features. Fortunately, as we've
demonstrated, building a GraalVM native image for the Groovy application is still very much possible.

Depending on your requirements and/or interests, you can just use the GraalVM tools in the simplest way and do not care much about what is really happening. You can just employ the
`native-image-agent`, and use generated files for building the native image for a Groovy application and get done with it.

Alternatively, if you invest the time and effort to learn more about the internal working of the process, you can come up with optimally sized images for your Groovy application. Related GraalVM
`native-image-agent` filtering configuration might be specific for the concrete tech stack which is used. However, acquired knowledge should be of great help when dealing with different types of
Groovy applications enabling you to update configuration quickly enough.

## References
[1] GraalVM homepage - [https://www.graalvm.org](https://www.graalvm.org) <br/>
[2] GraalVM GitHub homepage - [https://github.com/oracle/graal](https://github.com/oracle/graal) <br/>
[3] Turning the JVM into a Polyglot VM with Graal, Chris Seaton - [https://www.youtube.com/watch?v=9oHpAhgkNAY](https://www.youtube.com/watch?v=9oHpAhgkNAY) <br/>
[4] Maximizing Applications Performance with GraalVM, Alina Yurenko - [https://www.youtube.com/watch?v=j9jIny7HsSo](https://www.youtube.com/watch?v=j9jIny7HsSo) <br/>
[5] GraalVM Native Image (SubstrateVM) GitHub homepage - [https://github.com/oracle/graal/blob/master/substratevm/README.md](https://github.com/oracle/graal/blob/master/substratevm/README.md) <br/>
[6] Creating your first Micronaut Graal application - [https://guides.micronaut.io/micronaut-creating-first-graal-app/guide/index.html](https://guides.micronaut.io/micronaut-creating-first-graal-app/guide/index.html) <br/>
[7] Quarkus - Building a Native Executable - [https://quarkus.io/guides/building-native-image](https://quarkus.io/guides/building-native-image) <br/>
[8] Helidon - GraalVM Native Images - [https://helidon.io/docs/latest/#/se/guides/36_graalnative](https://helidon.io/docs/latest/#/se/guides/36_graalnative) <br/>
[9] Spring Native - [https://github.com/spring-projects-experimental/spring-native](https://github.com/spring-projects-experimental/spring-native) <br/>
[10] GraalVM and Groovy - how to start? - [https://e.printstacktrace.blog/graalvm-and-groovy-how-to-start](https://e.printstacktrace.blog/graalvm-and-groovy-how-to-start) <br/>
[11] Groovy + GraalVM native-image = instant startup time! - [https://www.youtube.com/watch?v=BjO_vBzaB4c](https://www.youtube.com/watch?v=BjO_vBzaB4c) <br/>
[12] GraalVM native-image - from 2.1s to 0.013s startup time | Groovy Tutorial - [https://www.youtube.com/watch?v=RPdugI8eZgo](https://www.youtube.com/watch?v=RPdugI8eZgo) <br/>
[13] klokwrk-tool-gradle-source-repack - [https://github.com/croz-ltd/klokwrk-project/blob/feature_graalNativeImageArticleUpdate/tool/klokwrk-tool-gradle-source-repack/README.md](https://github.com/croz-ltd/klokwrk-project/blob/feature_graalNativeImageArticleUpdate/tool/klokwrk-tool-gradle-source-repack/README.md) <br/>
[14] GraalVM 20.3.x Release Notes - [https://www.graalvm.org/release-notes/20_3](https://www.graalvm.org/release-notes/20_3) <br/>
[15] Debugging Gradle internals from IntelliJ IDEA - [https://github.com/croz-ltd/klokwrk-project/blob/feature_graalNativeImageArticleUpdate/support/documentation/article/debugging-gradle-from-idea/debugging-gradle-from-idea.md](https://github.com/croz-ltd/klokwrk-project/blob/feature_graalNativeImageArticleUpdate/support/documentation/article/debugging-gradle-from-idea/debugging-gradle-from-idea.md) <br/>
[16] Micronaut for GraalVM - [https://docs.micronaut.io/latest/guide/index.html#graal](https://docs.micronaut.io/latest/guide/index.html#graal) <br/>
[17] Micronaut Picocli Integration - [https://micronaut-projects.github.io/micronaut-picocli/latest/guide/](https://micronaut-projects.github.io/micronaut-picocli/latest/guide/) <br/>
[18] gdub - [https://github.com/gdubw/gdub](https://github.com/gdubw/gdub) <br/>
[19] A simple native HTTP server with GraalVM - [https://melix.github.io/blog/2019/03/simple-http-server-graal.html](https://melix.github.io/blog/2019/03/simple-http-server-graal.html) <br/>
[20] Micronaut Launch - [https://micronaut.io/launch/](https://micronaut.io/launch/) <br/>
[21] Micronaut Gradle Plugin - [https://github.com/micronaut-projects/micronaut-gradle-plugin](https://github.com/micronaut-projects/micronaut-gradle-plugin) <br/>
[22] Reflection Use in Native Images - [https://www.graalvm.org/reference-manual/native-image/Reflection/](https://www.graalvm.org/reference-manual/native-image/Reflection/) <br/>
[23] Native Image Build Configuration - [https://www.graalvm.org/reference-manual/native-image/BuildConfiguration/](https://www.graalvm.org/reference-manual/native-image/BuildConfiguration/) <br/>
[24] Reflection on Native Image - Manual Configuration - [https://www.graalvm.org/reference-manual/native-image/Reflection/#manual-configuration](https://www.graalvm.org/reference-manual/native-image/Reflection/#manual-configuration) <br/>
[25] Groovy Closures - [https://groovy-lang.org/closures.html](https://groovy-lang.org/closures.html) <br/>
[26] Delegation Strategy - [https://groovy-lang.org/closures.html#_delegation_strategy](https://groovy-lang.org/closures.html#_delegation_strategy) <br/>
[27] Closure to type coercion - [https://docs.groovy-lang.org/latest/html/documentation/core-semantics.html#closure-coercion](https://docs.groovy-lang.org/latest/html/documentation/core-semantics.html#closure-coercion) <br/>
[28] GraalVM Agent Trace Files - [https://www.graalvm.org/reference-manual/native-image/BuildConfiguration/#trace-files](https://www.graalvm.org/reference-manual/native-image/BuildConfiguration/#trace-files) <br/>
[29] GraalVM Agent Advanced Usage - [https://www.graalvm.org/reference-manual/native-image/BuildConfiguration/#agent-advanced-usage](https://www.graalvm.org/reference-manual/native-image/BuildConfiguration/#agent-advanced-usage) <br/>
