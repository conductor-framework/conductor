[![star](http://github-svg-buttons.herokuapp.com/star.svg?user=ddavison&repo=getting-started-with-selenium)](http://github.com/ddavison/getting-started-with-selenium)
[![fork](http://github-svg-buttons.herokuapp.com/fork.svg?user=ddavison&repo=getting-started-with-selenium)](http://github.com/ddavison/getting-started-with-selenium/fork)

# Getting Started
To get right up and started,  you can [download the project (zip)](https://github.com/ddavison/getting-started-with-selenium/archive/master.zip) or you can checkout the project from github. If you don't know how, [this should help](http://git-scm.com/book/en/Git-Basics-Getting-a-Git-Repository).

**Prerequisites**
- Maven (if using eclipse, install Maven Integration for Eclipse)
- jUnit 4
- Java
- WebDriver (Chrome, Firefox, IE, etc)

### Drivers
Currently, not all drivers are not packaged with this project, but they may be in the future!
- [Chromedriver](http://chromedriver.storage.googleapis.com/index.html) (now is packaged with the project)
- Firefox driver IS actually packaged with the Selenium jar.
- [IEDriver](https://code.google.com/p/selenium/downloads/list)

Launch your IDE, and under ```src/tests/java``` you'll find a file under the ```functional``` package.  This is a very short a simple test. 
If you do not have Chromedriver installed, just switch the browser to ```FIREFOX``` and right click the file and ```Run As -> jUnit Test```

# Goals
The primary goals of this project are to...
- Take advantage of method chaining, to create a fluent interface.
- Abstract the programmer from bloated scripts resulting from using too many css selectors, and too much code.
- Provide a quick and easy framework in Selenium 2 using Java, to get started writing scripts.
- Provide a free to use framework for any starting enterprise, or individual programmer.
- Utilize the power of CSS!

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


[See a working example](https://github.com/ddavison/getting-started-with-selenium/blob/master/src/tests/java/com/company/seleniumframework/functional/SampleFunctionalTest.java) of what a test script written using this framework might look like.

# Pull requests
If you have an idea for the framework, fork it and submit a pull-request!
