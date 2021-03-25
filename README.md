## Inversoft REST Client ![semver 2.0.0 compliant](http://img.shields.io/badge/semver-2.0.0-brightgreen.svg?style=flat-square)
Inversoft's REST Client. A simple REST Client to make life simpler when calling REST APIs.

## Java REST Client

### Maven

```xml
<dependency>
  <groupId>com.inversoft</groupId>
  <artifactId>restify</artifactId>
  <version>3.5.0</version>
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

