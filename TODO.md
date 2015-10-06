# TODO list #
  * create maven pom and build scripts
  * implement more getElement and getElements usage
  * add license, this project uses Apache 2 license
  * consider to provide an API to do?
```
 new Http("http://www.google.com").get().text // maybe getText could trigger at get() by default, so it works very similar to:
 new URL("http://www.google.com").text
```
  * add the concept of base URI and support get and post to relative URI, use a Link Resolver
  * add EMC methods for Jericho HTML Parser to make it easier to use in Groovy