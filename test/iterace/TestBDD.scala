package iterace;
//package iterace;
//
//import org.junit.runner.RunWith
//import org.scalatest.{Spec,BeforeAndAfter}
//import org.scalatest.junit.JUnitRunner
//import scala.collection.JavaConversions._
//import jdd.zdd.ZDD
//import com.ibm.wala.util.intset.IntSet
//
//object Timer {
//  private var start:Long = 0L
//  private var end:Long = 0L
//  def go = {
//    start = System.currentTimeMillis
//  }
//  def stop = {
//    end = System.currentTimeMillis
//    println(">   %.2f" format ((end - start)/ 1000.0))
//  }
//}
//
//@RunWith(classOf[JUnitRunner])
//class TestBDD extends Spec with BeforeAndAfter{
//  describe("A test test") {
//    
//    Timer.go
//    val zdd = new ZDD(1000,100)
//    
//    val variables = Stream.range(0, 10000).map(i => zdd.createVar())
//    val base = zdd.base();
//    val variablesInSets = variables.map(v => zdd.change(base, v))
//    val bigSet = variablesInSets.reduce((a,b) => zdd.union(a,b))
//    
////    zdd.printSet(bigSet)
//    Timer.stop
//    Timer.go
//    
//    
//    Timer.stop
//    
//    
////    val v1 = zdd.createVar();
////    val v2 = zdd.createVar();
////    
////    val a = zdd.empty();
////    val b = zdd.base();
////    val c = zdd.change(b, v1);
////    val d = zdd.change(b, v2);
////    val e = zdd.union(c,d);
////    val f = zdd.union(b,e);
////    val g = zdd.diff(f,c);
////    zdd.print(g)
////    zdd.printSet(g)
//  }
//}