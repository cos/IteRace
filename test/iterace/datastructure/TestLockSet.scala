package iterace.datastructure
import org.junit.runner.RunWith
import org.scalatest.junit.JUnitRunner

@RunWith(classOf[JUnitRunner])
class TestLockSet extends LockSetAbstractTest(List("particles", "../lib/parallelArray.mock"), "Lparticles/ParticleWithLocks") {
  testGetLocks("noLocks", "{  }")
  testGetLocks("oneSimpleLock", "{ L: particles.ParticleWithLocks$3.op v3(x) }")

  testGetLockSet("oneSimpleLock", "xyz",
    "{ L: particles.ParticleWithLocks$3.op v3(x) }")

  testGetLockSet("anotherDumbLock", "xyz", "{  }")

  testGetLockSet("imbricatedLocks", "xyz",
    "{ L: particles.ParticleWithLocks$6.op v3(x) }")

  testGetLockSet("imbricatedTwoLocks", "xyz",
    "{ L: particles.ParticleWithLocks$7.op v3(x) , L: particles.ParticleWithLocks$7.op v5(y) }")

  testGetLockSet("throughMethodCall", "xyz",
    "{ L: particles.ParticleWithLocks$8.op v3(x) , L: particles.ParticleWithLocks$8.theMethod v3(y) }")

  testGetLockSet("checkMeetOverAllValidPathsPositive",
    "xyz", "{ L: particles.ParticleWithLocks$9.op v3(x) }")

  testGetLockSet("checkMeetOverAllValidPathsNegative", "xyz", "{  }")
}