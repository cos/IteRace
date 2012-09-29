package iterace.datastructure
import org.junit.runner.RunWith
import org.scalatest.junit.JUnitRunner
import org.junit.Test
import org.junit.Ignore
import wala.AnalysisScope.Dependency

class TestLockSetOnjUnit extends LockSetAbstractTest(
  List(Dependency("../evaluation/junit/bin"), Dependency("../lib/parallelArray.mock")),
  "Ljunit/tests/ParallelAllTests") {

  @Test def allLocks = assertAllLocks("{ L: junit.framework.TestResult: junit.textui.TestRunner.createTestResult(TestRunner.java:105)-outside }", "main([Ljava/lang/String;)V")
}