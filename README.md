## Inversoft REST Client ![semver 2.0.0 compliant](http://img.shields.io/badge/semver-2.0.0-brightgreen.svg?style=flat-square)
Inversoft's REST Client. A simple REST Client to make life simpler when calling REST APIs. This library contains both a Java version and a C# version.

## Java REST Client

### Maven

```xml
<dependency>
  <groupId>com.inversoft</groupId>
  <artifactId>restify</artifactId>
  <version>3.2.1</version>
</dependency>
```

### Building with Savant

**Note:** This project uses the Savant build tool. To compile using using Savant, follow these instructions:

```bash
$ mkdir ~/savant
$ cd ~/savant
$ wget http://savant.inversoft.org/org/savantbuild/savant-core/1.0.0/savant-1.0.0.tar.gz
$ tar xvfz savant-1.0.0.tar.gz
$ ln -s ./savant-1.0.0 current
$ export PATH=$PATH:~/savant/current/bin/
```

Then, perform an integration build of the project by running:
```bash
$ sb int
```

For more information, checkout [savantbuild.org](http://savantbuild.org/).

## C# REST Client

**Note:** This project uses a slightly different file layout than other C# and Visual Studio projects. This is a Java convention that we carried over to all of our libraries to keep consistency.

**Note:** This project is using .Net 2.0 and versions of various libraries that are compatible with .Net 2.0. This is done so that this library can be used with Unity.

Find me on NuGet [Inversoft.Restify](https://www.nuget.org/packages/Inversoft.Restify/)

To get started, you can import the C# project into your solution. The C# project file is located here:

```bash
src/main/csharp/Restify.csproj
```

This project file contains the necessary references to the DLLs required to use the C# REST client. If you need to rebuild or refresh any of the packages, here's the list of dependencies:

* Newtonsoft.Json (version 8.0.3)
* System
* System.Web

