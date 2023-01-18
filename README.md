# mv4fx (MultiView For FX)

## Overview

This is a library for JavaFX that provides a basic framework to organize an application using `Views`.

It comes along with two main components, one is a `ViewGroup` (which is best compared with a 
`TabPane` from standard JavaFX) that contains `View` instances (which are best compared with the
`Tabs` of a `TabPane`). A `ViewGroup` can be placed into a `ViewGroupContainer`, the counterpart of 
the standard JavaFX components is a `SplitPane`.

In opposite to the standard components, the components of this library have a quite important
addition: you may drag and drop a `View` freely around:
- Moving a `View` within its owning `ViewGroup` (similar to reordering the `Tabs` in a `TabPane`)
- Moving a `View` to a different `ViewGroup` (not supported by JavaFX)
- Moving a `View` in a way that an existing `ViewGroup` is automatically split (not supported by JavaFX) 
- Moving a `Voew` to a new window. (not supported by JavaFX)

Compared with a `TabPane` and `Tab` the components implemented here have more properties you may
customize.

A lot of settings can be customized via CSS.

If you wish to run a small demo application to play around, type:

```shell
mvn test -Dtest=DemoApp
```

[There is also a screen cast of it](doc/demo.mp4)


## Include into your project

- Currently not published to Maven 