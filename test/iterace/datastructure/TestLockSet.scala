package iterace.datastructure
import org.junit.runner.RunWith
import org.scalatest.junit.JUnitRunner
import org.junit.Test

class TestLockSet extends LockSetAbstractTest(List("particles", "../lib/parallelArray.mock"), "Lparticles/ParticleWithLocks") {
  @Test def noLocks = assertAllLocks("{  }")
  @Test def synchronizedMethod = assertAllLocks("{ L: particles.ParticleWithLocks$14.op v1(this) }")

  @Test def oneSimpleLock = assertLockSet("xyz",
    "{ L: particles.ParticleWithLocks$3.op v3(x) }")

  @Test def anotherDumbLock = assertLockSet("xyz", "{  }")

  @Test def imbricatedLocks = assertLockSet("xyz",
    "{ L: particles.ParticleWithLocks$6.op v3(x) }")

  @Test def imbricatedTwoLocks = assertLockSet("xyz",
    "{ L: particles.ParticleWithLocks$7.op v3(x) , L: particles.ParticleWithLocks$7.op v5(y) }")

  @Test def throughMethodCall = assertLockSet("xyz",
    "{ L: particles.ParticleWithLocks$8.op v3(x) , L: particles.ParticleWithLocks$8.theMethod v3(y) }")

  @Test def checkMeetOverAllValidPathsPositive = assertLockSet("xyz",
    "{ L: particles.ParticleWithLocks$9.op v3(x) }")

  @Test def checkMeetOverAllValidPathsNegative = assertLockSet("xyz", "{  }")

  @Test def lockUsingSynchronizedBlock = assertLockSet("xyz", "{ L: particles.ParticleWithLocks$11.op v1(this) }")

  @Test def lockUsingSynchronizedBlockInAnotherMethod = assertLockSet("xyz", "{ L: particles.ParticleWithLocks$12.op v1(this) }")

  // important test
  @Test def lockFromBothSynchronizedAndUnsynchronized = assertLockSet("xyz", "{  }")

  @Test def synchronizedMethodLockSet = assertLockSet("synchronizedMethod", "xyz", "{ L: particles.ParticleWithLocks$14.op v1(this) }")

//  @Test def synchronizedStaticMethod = assertLockSet("xyz", "yyy")
  // @Test def xxx = assertLockSet("xyz", "yyy")
  // @Test def xxx = assertLockSet("xyz", "yyy")
  // @Test def xxx = assertLockSet("xyz", "yyy")
}