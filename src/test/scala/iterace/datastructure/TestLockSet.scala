package iterace.datastructure
import org.junit.runner.RunWith
import org.scalatest.junit.JUnitRunner
import org.junit.Test
import org.junit.Ignore
import wala.Dependency

class TestLockSet extends LockSetAbstractTest("Lparticles/ParticleWithLocks") {
  @Test def noLocks = assertAllLocks("{  }")
  @Test def synchronizedMethod = assertAllLocks("{ L: particles.ParticleWithLocks$14: particles.ParticleWithLocks.synchronizedMethod(ParticleWithLocks.java:282)-outside }")

  @Test def oneSimpleLock = assertLockSet("xyz",
    "{ L: java.lang.Object: particles.ParticleWithLocks$3.op(ParticleWithLocks$3.java:47)-alpha }")

  @Test def anotherDumbLock = assertLockSet("xyz", "{  }")

  @Test def imbricatedLocks = assertLockSet("xyz",
    "{ L: java.lang.Object: particles.ParticleWithLocks$6.op(ParticleWithLocks$6.java:100)-alpha }")

  @Test def imbricatedTwoLocks = assertLockSet("xyz",
    "{ L: java.lang.Object: particles.ParticleWithLocks$7.op(ParticleWithLocks$7.java:121)-alpha , L: java.lang.Object: particles.ParticleWithLocks$7.op(ParticleWithLocks$7.java:122)-alpha }")

  @Test def throughMethodCall = assertLockSet("xyz",
    "{ L: java.lang.Object: particles.ParticleWithLocks$8.op(ParticleWithLocks$8.java:143)-alpha , L: java.lang.Object: particles.ParticleWithLocks$8.op(ParticleWithLocks$8.java:144)-alpha }")

  @Test def checkMeetOverAllValidPathsPositive = assertLockSet("xyz",
    "{ L: java.lang.Object: particles.ParticleWithLocks$9.op(ParticleWithLocks$9.java:169)-alpha }")

  @Test def checkMeetOverAllValidPathsNegative = assertLockSet("xyz", "{  }")

  @Test def lockUsingSynchronizedBlock = assertLockSet("xyz",
    "{ L: particles.ParticleWithLocks$11: particles.ParticleWithLocks.lockUsingSynchronizedBlock(ParticleWithLocks.java:215)-outside }")

  @Test def lockUsingSynchronizedBlockInAnotherMethod = assertLockSet("xyz",
    "{ L: particles.ParticleWithLocks$12: particles.ParticleWithLocks.lockUsingSynchronizedBlockInAnotherMethod(ParticleWithLocks.java:235)-outside }")

  // important test
  @Test def lockFromBothSynchronizedAndUnsynchronized = assertLockSet("xyz", "{  }")

  @Test def synchronizedMethodLockSet = assertLockSet("synchronizedMethod", "xyz",
    "{ L: particles.ParticleWithLocks$14: particles.ParticleWithLocks.synchronizedMethod(ParticleWithLocks.java:282)-outside }")

  @Test def synchronizedOnObjectMethod = assertLockSet("synchronizedOnObjectMethod", "origin",
    "{ L: particles.Particle: particles.ParticleWithLocks$15.op(ParticleWithLocks$15.java:300)-alpha }")

  @Test def synchronizedStaticMethod = assertLockSet("xyz", "{ L: particles.ParticleWithLocks }")

  @Ignore @Test def reenterantLock = assertAllLocks("{ L: particles.ParticleWithLocks$16.op(ParticleWithLocks$16.java:319)-outside }")

  @Ignore @Test def reenterantLockSet = assertLockSet("reenterantLock", "xyz", "yyy")
  // @Test def xxx = assertLockSet("xyz", "yyy")
  // @Test def xxx = assertLockSet("xyz", "yyy")
}