package iterace.datastructure
import org.junit.runner.RunWith
import org.scalatest.junit.JUnitRunner
import org.junit.Test
import org.junit.Ignore
import iterace.util.log

class TestLockSetOnjUnit extends 
LockSetAbstractTest(
    List("../evaluation/junit/bin", "../lib/parallelArray.mock"),
    "Ljunit/tests/ParallelAllTests") {
  
  log.activate
  
  @Test def allLocks = assertAllLocks("{ L: junit.framework.TestResult: junit.textui.TestRunner.createTestResult(TestRunner.java:105)-outside }","main([Ljava/lang/String;)V")
}