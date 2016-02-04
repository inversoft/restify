## Inversoft Java REST Client ![semver 2.0.0 compliant](http://img.shields.io/badge/semver-2.0.0-brightgreen.svg?style=flat-square)
Inversoft's REST Client. A simple Java REST Client to make life simpler when calling REST APIs from Java.

**Note:** This project uses the Savant build tool. To compile using using Savant, follow these instructions:

```bash
$ mkdir ~/savant
$ cd ~/savant
$ wget http://savant.inversoft.org/org/savantbuild/savant-core/0.4.4/savant-0.4.4.tar.gz
$ tar xvfz savant-0.4.4.tar.gz
$ ln -s ./savant-0.4.4 current
$ export PATH=$PATH:~/savant/current/bin/
```

Then, perform an integration build of the project by running:
```bash
$ sb int
```

For more information, checkout [savantbuild.org](http://savantbuild.org/).