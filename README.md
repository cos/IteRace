IteRace
=======

IteRace is a static race detection tool developed at University of Illinois. 
Static race detectors suffer from imprecision (due to conservative assumptions) which usually manifests itself in an unmanageable number of warnings that the programmer needs to inspect.
IteRace tackles this problem by specialization:
 - it is aware of and uses to its advantage the thread and data-flow structure of loop-parallel operations. As parallel collections are not yet available in Java (they will be in Java8 next year), IteRace analyzes a collection which mostly follows [ParallelArray's API](http://gee.cs.oswego.edu/dl/concurrency-interest/index.html)
 - reports races in application, not library, code. For example, you don't have to track down races occurring in HashSet - all it will tell you is that you have inadvertently shared a particular HashSet object.   
 - filters races based on a thread-safety model of classes. It is slightly more involved then that, but, for example, you won't get race raports on an AtomicInteger.

You can find more details in the [IteRace technical report](https://www.ideals.illinois.edu/handle/2142/42545).

We haven't yet made the tool as user-friendly as we would like to. If you want to use it in your own project, contact me and I'll assist with the setup.

[This git repo](https://github.com/cos/workspace-iterace) contains a workspace with a multi-project sbt build that grabs all dependencies, builds the tool, and has infrastructure for running benchmarks.
