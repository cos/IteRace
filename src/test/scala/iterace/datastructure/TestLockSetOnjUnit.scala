package iterace.datastructure
import org.junit.runner.RunWith
import org.scalatest.junit.JUnitRunner
import org.junit.Test
import org.junit.Ignore
import wala.Dependency

class TestLockSetOnjUnit extends LockSetAbstractTest("Ljunit/tests/ParallelAllTests") {

  @Test @Ignore("Requires junit. Link back when possible.") def allLocks = assertAllLocks("{ L: junit.framework.TestResult: junit.textui.TestRunner.createTestResult(TestRunner.java:105)-outside }", "main([Ljava/lang/String;)V")
}