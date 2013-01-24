package iterace.stage

import org.junit.Test
import iterace.IteRaceOption
import org.junit.Ignore

class TestThreadLocal extends RaceAbstractTest {

  val entryClass = "Lparticles/TestThreadLocal"
  override val options = {
    import iterace.IteRaceOption._
    Set[IteRaceOption](TwoThreads)
  }

  @Test def testNoRaceOnThreadLocalSet = expectNoRaces
  @Test def testNoRaceOnThreadLocalGet = expectNoRaces
  @Test def testNoRaceOnThreadLocalSetGet = expectNoRaces
  @Test def testNoRaceOnThreadLocalObject = expectNoRaces
  @Test def testNoRaceOnThreadLocalRetrivedObject = expectNoRaces
  @Test def testRaceOnThreadLocalRetrivedObject = expect("""
Loop: particles.TestThreadLocal.testRaceOnThreadLocalRetrivedObject(TestThreadLocal.java:87)

particles.Particle: particles.TestThreadLocal.testRaceOnThreadLocalRetrivedObject(TestThreadLocal.java:85)
 .x
   (a)  particles.TestThreadLocal$6.op(TestThreadLocal.java:92)
   (b)  particles.TestThreadLocal$6.op(TestThreadLocal.java:92)
""")
  
	@Test def testNoRaceOnObjectSetOutsideTheLoop = expectNoRaces
	@Test def testRaceOnThreadLocalReferredSharedObject = expect("""
Loop: particles.TestThreadLocal.testRaceOnThreadLocalReferredSharedObject(TestThreadLocal.java:116)

particles.Particle: particles.TestThreadLocal.testRaceOnThreadLocalReferredSharedObject(TestThreadLocal.java:114)
 .x
   (a)  particles.TestThreadLocal$8.op(TestThreadLocal.java:123)
   (b)  particles.TestThreadLocal$8.op(TestThreadLocal.java:123)
""")
//	@Test def  = expectNoRaces
//	@Test def  = expectNoRaces
//	@Test def  = expectNoRaces
}