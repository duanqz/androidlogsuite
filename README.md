#Android Log Suite

## Documentation

`androidlogsuite` is a tool for analysing android log with [graphic output](html/logreport.html).

- Written in JAVA, build with GRADLE, output to HTML, naturely corss-platform
- Configurated by XML, readable and extensible
- Supporting analysing log from connected device or existing log file

[![Build Status](https://travis-ci.org/duanqz/androidlogsuite.svg?branch=master)](https://github.com/duanqz/androidlogsuite)


## Quick Start

### Building

1. Clone repository `git clone https://github.com/duanqz/androidlogsuite.git`
2. Type `./gradlew` in the console, waiting for a few seconds, all the output are put under `$PROJECT/build/`
3. The distribution is put under `$PROJECT/build/install/androidlogsuite`, you could copy the file tree anywhere you want.

### Running

***Analysing Log via USB*** make sure the device is connected to PC via USB cable if you want to analysis the output log immediately. 

***Anaylysing Log in File*** if you would like to analysis existing log in a local file, make sure you have configurate `configuration/configurecenter.xml` correctly.

In the terminal, type the following commands:

    cd $PROJECT/build/install/androidlogsuite
    ./bin/androidlogsuite

If you are on Windows, type `./bin/androidlogsuite.bat` instead.

### Displaying

`androidlogsuite` outputs the result as HTML, open [html/logreport.html](html/logreport.html) with a browser to see many diagrams by differents categories.

### Contributers

[fanping.deng@gmail.com](fanping.deng@gmail.com)

[duanqz@gmail.com](duanqz@gmail.com)

Hope some day you will join us.