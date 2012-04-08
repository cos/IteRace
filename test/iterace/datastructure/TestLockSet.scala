package iterace.datastructure
import org.junit.runner.RunWith
import org.scalatest.junit.JUnitRunner
import org.junit.Test
import org.junit.Ignore

class TestLockSet extends LockSetAbstractTest(List("particles", "../lib/parallelArray.mock"), "Lparticles/ParticleWithLocks") {
  @Test def noLocks = assertAllLocks("{  }")
  @Test def synchronizedMethod = assertAllLocks("{ L: particles.ParticleWithLocks.synchronizedMethod(ParticleWithLocks.java:282) }")

  @Test def oneSimpleLock = assertLockSet("xyz",
    "{ L: particles.ParticleWithLocks$3.op(ParticleWithLocks$3.java:47) }")

  @Test def anotherDumbLock = assertLockSet("xyz", "{  }")

  @Test def imbricatedLocks = assertLockSet("xyz",
    "{ L: particles.ParticleWithLocks$6.op(ParticleWithLocks$6.java:100) }")

  @Test def imbricatedTwoLocks = assertLockSet("xyz",
    "{ L: particles.ParticleWithLocks$7.op(ParticleWithLocks$7.java:121) , L: particles.ParticleWithLocks$7.op(ParticleWithLocks$7.java:122) }")

  @Test def throughMethodCall = assertLockSet("xyz",
    "{ L: particles.ParticleWithLocks$8.op(ParticleWithLocks$8.java:143) , L: particles.ParticleWithLocks$8.op(ParticleWithLocks$8.java:144) }")

  @Test def checkMeetOverAllValidPathsPositive = assertLockSet("xyz",
    "{ L: particles.ParticleWithLocks$9.op(ParticleWithLocks$9.java:169) }")

  @Test def checkMeetOverAllValidPathsNegative = assertLockSet("xyz", "{  }")

  @Test def lockUsingSynchronizedBlock = assertLockSet("xyz",
    "{ L: particles.ParticleWithLocks.lockUsingSynchronizedBlock(ParticleWithLocks.java:215) }")

  @Test def lockUsingSynchronizedBlockInAnotherMethod = assertLockSet("xyz",
    "{ L: particles.ParticleWithLocks.lockUsingSynchronizedBlockInAnotherMethod(ParticleWithLocks.java:235) }")

  // important test
  @Test def lockFromBothSynchronizedAndUnsynchronized = assertLockSet("xyz", "{  }")

  @Test def synchronizedMethodLockSet = assertLockSet("synchronizedMethod", "xyz",
    "{ L: particles.ParticleWithLocks.synchronizedMethod(ParticleWithLocks.java:282) }")

  @Test def synchronizedStaticMethod = assertLockSet("xyz", "{ L: particles.ParticleWithLocks }")
  
  @Test def reenterantLock = assertAllLocks("{ L: particles.ParticleWithLocks$16.op(ParticleWithLocks$16.java:319) }")
  
  @Ignore @Test def reenterantLockSet = assertLockSet("reenterantLock", "xyz", "yyy")
  // @Test def xxx = assertLockSet("xyz", "yyy")
  // @Test def xxx = assertLockSet("xyz", "yyy")
}