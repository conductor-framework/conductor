Conductor
===
[See the site](http://conductor.ddavison.io)

[![star](http://githubbadges.com/star.svg?user=conductor-framework&repo=conductor)](http://github.com/conductor-framework/conductor)
[![fork](http://githubbadges.com/fork.svg?user=conductor-framework&repo=conductor)](http://github.com/conductor-framework/conductor/fork)

# Getting Started
Using maven, include it as a dependency:
```xml
<repositories>    
    <repository>
        <id>jitpack.io</id>
        <url>https://jitpack.io</url>
    </repository>
</repositories>

<dependencies>
    <dependency>
        <groupId>com.github.willowtreeapps</groupId>
        <artifactId>conductor</artifactId>
        <version>2.5.0</version>
    </dependency>
</dependencies>
```

Create a Java Class, and extend it from `io.ddavison.conductor.Locomotive`

### Drivers
Drivers should be put in the root of your project, and be named like this:

#### Mac
chromedriver.mac

#### Windows
chromedriver.exe

#### Linux
chromedriver.linux

So as an example, your project structure could be:
```
Project
| src
|   main
|     java
|       TestClass.java
| pom.xml
| chromedriver.mac
| chromedriver.exe
| chromedriver.linux
```

Currently, six browsers are supported and they are Firefox, HTMLUnit, Chrome, Internet Explorer, Safari, and PhantomJS


# Goals
The primary goals of this project are to...
- Take advantage of method chaining, to create a fluent interface.
- Abstract the programmer from bloated scripts resulting from using too many css selectors, and too much code.
- Provide a quick and easy framework in Selenium 2 using Java, to get started writing scripts.
- Provide a free to use framework for any starting enterprise, or individual programmer.
- Utilize the power of CSS!


# Default Properties
- baseUrl = {string: base url}
- path = {string: path of the specific page (appended to baseUrl)}
- browser = {string: chrome | firefox | ie | edge | safari | phantomjs}
- timeout = {int: default equals 5 seconds per call}
- retries = {int: default equals 5 retries}
- screenshotsOnFail = {boolean: true or false}
- hub = {string: url}
- url = {string: full valid url} `@deprecated use baseUrl & path instead`

# Actions
You can perform any action that you could possibly do, using the inline actions.
- ```click(By)```
- ```setText(By, text)```
- ```getText(By)```
- ```hoverOver(By)```
- ```check(By)```
- ```uncheck(By)```
- ```navigateTo(url)```
- ```goBack()```
- ```isPresent(By)```
- ```getAttribute(By, attribute)```
- etc.

# In-line validations
This is one of the most important features that I want to _*accentuate*_.
- ```validateText```
- ```validateTextNot```
- ```validateChecked```
- ```validateUnchecked```
- ```validatePresent```
- ```validateNotPresent```
- ```validateTextPresent```
- ```validateTextNotPresent```

All of these methods are able to be called in-line, and fluently without ever having to break your tests.

# Switching Windows
Another nice feature that is offered, is the simplicity of window switching in Selenium.

- ```switchToWindow(regex)```
- ```waitForWindow(regex)```
- ```closeWindow(regex)```

All of these functions take a regular expression argument, and match either the url or title of the window that you want to interact with.

# Switching Frames
- ```switchToFrame(idOrName)```
- ```switchToDefaultContent()```

# Implicit Waiting
In addition to the Selenium 2 implicit waiting, the ```AutomationTest``` class extends on this concept by implenting a sort of ```waitFor``` functionality which ensures that an object appears before interacting with it.  This rids of most ```ElementNotFound``` exceptions that Selenium will cough up.


[See a working example](https://github.com/ddavison/conductor/blob/master/src/test/java/io/ddavison/conductor/FrameworkTest.java) of what a test script written using this framework might look like.

# Pull requests
If you have an idea for the framework, fork it and submit a pull-request!
