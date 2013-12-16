# IteRace

IteRace is a static race detection tool developed at University of Illinois. 
Static race detectors suffer from imprecision (due to conservative assumptions) which usually manifests itself in an unmanageable number of warnings that the programmer needs to inspect.
IteRace tackles this problem by specialization:
 - it is aware of and uses to its advantage the thread and data-flow structure of loop-parallel operations. As parallel collections are not yet available in Java (they will be in Java8 next year), IteRace analyzes a collection which mostly follows [ParallelArray's API](http://gee.cs.oswego.edu/dl/concurrency-interest/index.html)
 - reports races in application, not library, code. For example, you don't have to track down races occurring in HashSet - all it will tell you is that you have inadvertently shared a particular HashSet object.   
 - filters races based on a thread-safety model of classes. It is slightly more involved then that, but, for example, you won't get race reports on an AtomicInteger.

You can find more details in our [ISSTA '13 paper](http://publish.illinois.edu/cos/files/2013/08/IteRace-ISSTA-13.pdf).

## Getting started

IteRace is implemented in Scala, relies on [WALA](http://wala.sourceforge.net/wiki/index.php/Main_Page) as the underlying analysis engine, and uses [SBT](http://www.scala-sbt.org) for building.

### Steps:

1. Make sure you have [Scala 2.10](http://www.scala-lang.org/download/), [Maven](http://maven.apache.org/download.cgi), [Apache Ivy](http://ant.apache.org/ivy/download.cgi), and [SBT 0.13](http://www.scala-sbt.org/release/docs/Getting-Started/Setup.html) 

2. Clone WALA and install it to your local Maven repo
    - `export JAVA_HOME="<java's home on your system>"` (on OS X: `/usr/libexec/java_home`)
    - `git clone https://github.com/wala/WALA.git`
    - `cd WALA`
    - `mvn clean install -DskipTests=true` 
    
3. Clone WALAFacade and cos/Util and install them to your local Ivy repo
    - `git clone https://github.com/cos/WALAFacade.git`
    - `cd WALAFacade`
    - `sbt publishLocal`
    - `cd ..`
    - `git clone https://github.com/cos/Util.git`
    - `cd Util`
    - `sbt publishLocal`
    
4. Clone IteRace and compile
    - `git clone https://github.com/cos/IteRace.git`
    - `cd IteRace`
    - `sbt compile`

5. Edit `/src/main/resources/local.config` to point to your Java library jar
  
If you use Eclipse, you can `sbt eclipse` and import the project into your workspace (dependencies will be linked to the Ivy repo).

## Analyzing a program

SBT puts the compiled binary in the `/target` directory. You can play with IteRace using its Interactive tool (`iterace.Interactive` main class). It accepts configuration options as arguments, or you can provide it a configuration file via the `configFile=...` argument.

### Configuration options examples:

- `wala.entry.class="NBodySimulation"`
- `wala.entry.method = "main([Ljava/lang/String;)V"` (also the default)
- `iterace.races-file="test.races"`
- `iterace.log-file="verbose.log"`
- `wala.dependencies.binary+="path/to/class/folder"`
- `wala.dependencies.jar+="path/to/a/jar"`
- `wala.dependencies.source+="path/to/sources"` (WALA is sometimes fickle about loading sources)
- `wala.exclussions="javax\/.*"`

See `src/main/resources` for the default option values.


### Also:

We haven't yet made the tool as user-friendly as we would like to. If you encounter any problem with the setup or use of the tool, report it [here](https://github.com/cos/IteRace/issues) and I'll try to solve it quickly.

[This git repo](https://github.com/cos/workspace-iterace) contains the workspace we used for running the benchmarks and gathering the results presented in the paper. It is not as user friendly as we would like but we'll improve it shortly.
