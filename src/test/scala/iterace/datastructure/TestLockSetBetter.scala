package iterace.datastructure
import org.junit.runner.RunWith
import org.scalatest.junit.JUnitRunner
import org.junit.Test
import org.junit.Ignore
import com.ibm.wala.ipa.callgraph.propagation.PointerAnalysis
import iterace.pointeranalysis.RacePointerAnalysis

class TestLockSetBetter extends LockSetAbstractTest("Lparticles/ParticleWithLocks") {
  
  def lockConstructor(pa: RacePointerAnalysis) = new BetterLockConstructor(pa)
  
  @Test def noLocks = assertAllLocks("{  }")
  @Test def synchronizedMethod = assertAllLocks("{ L: alpha particles.ParticleWithLocks$14.op vv1(this) }")

  @Test def oneSimpleLock = assertLockSet("xyz",
    "{ L: alpha particles.ParticleWithLocks$3.op vv3(x) }")

  @Test def anotherDumbLock = assertLockSet("xyz", "{  }")

  @Test def imbricatedLocks = assertLockSet("xyz",
    "{ L: alpha particles.ParticleWithLocks$6.op vv3(x) }")

  @Test def imbricatedTwoLocks = assertLockSet("xyz",
    "{ L: alpha particles.ParticleWithLocks$7.op vv3(x) , L: alpha particles.ParticleWithLocks$7.op vv5(y) }")

  @Test def throughMethodCall = assertLockSet("xyz",
    "{ L: alpha particles.ParticleWithLocks$8.op vv3(x) , L: alpha particles.ParticleWithLocks$8.theMethod vv3(y) }")

  @Test def checkMeetOverAllValidPathsPositive = assertLockSet("xyz",
    "{ L: alpha particles.ParticleWithLocks$9.op vv3(x) }")

  @Test def checkMeetOverAllValidPathsNegative = assertLockSet("xyz", "{  }")

  @Test def lockUsingSynchronizedBlock = assertLockSet("xyz",
    "{ L: alpha particles.ParticleWithLocks$11.op vv1(this) }")

  @Test def lockUsingSynchronizedBlockInAnotherMethod = assertLockSet("xyz",
    "{ L: alpha particles.ParticleWithLocks$12.op vv1(this) }")

  // important test
  @Test def lockFromBothSynchronizedAndUnsynchronized = assertLockSet("xyz", "{  }")

  @Test def synchronizedMethodLockSet = assertLockSet("synchronizedMethod", "xyz",
    "{ L: alpha particles.ParticleWithLocks$14.op vv1(this) }")

  @Test def synchronizedOnObjectMethod = assertLockSet("synchronizedOnObjectMethod", "origin",
    "{ L: alpha particles.Particle.safeNothing vv1(this) }")

  @Test def synchronizedStaticMethod = assertLockSet("xyz", "{ L: alpha particles.ParticleWithLocks }")

  @Ignore @Test def reenterantLock = assertAllLocks("{ L: alpha particles.ParticleWithLocks$16.op(ParticleWithLocks.java:319)-outside }")

  @Ignore @Test def reenterantLockSet = assertLockSet("reenterantLock", "xyz", "yyy")
  // @Test def xxx = assertLockSet("xyz", "yyy")
  // @Test def xxx = assertLockSet("xyz", "yyy")
}