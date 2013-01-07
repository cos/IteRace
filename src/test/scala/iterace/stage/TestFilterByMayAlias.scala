package iterace.stage

import org.junit.Test
import iterace.IteRaceOption._
import iterace.IteRaceOption


class TestFilterByMayAlias extends RaceAbstractTest {
  
  val entryClass = "Lparticles/ParticleWithLocks"
  
  override val options = Set[IteRaceOption](IteRaceOption.TwoThreads, IteRaceOption.DeepSynchronized)

  @Test def vacuouslyNoRace = expectNoRaces

  @Test def noLocks = expect(
    """
Loop: particles.ParticleWithLocks.noLocks(ParticleWithLocks.java:29)

particles.ParticleWithLocks: particles.ParticleWithLocks.noLocks(ParticleWithLocks.java:27)
 .xyz
   (a)  particles.ParticleWithLocks$2.op(ParticleWithLocks.java:32)
   (b)  particles.ParticleWithLocks$2.op(ParticleWithLocks.java:32)
""")

  @Test def oneSimpleLock = expect(
    """
Loop: particles.ParticleWithLocks.oneSimpleLock(ParticleWithLocks.java:44)

particles.ParticleWithLocks: particles.ParticleWithLocks.oneSimpleLock(ParticleWithLocks.java:42)
 .xyz
   (a)  particles.ParticleWithLocks$3.op(ParticleWithLocks.java:49)
         L: java.lang.Object: particles.ParticleWithLocks$3.op(ParticleWithLocks.java:47)-alpha
   (b)  particles.ParticleWithLocks$3.op(ParticleWithLocks.java:49)
         L: java.lang.Object: particles.ParticleWithLocks$3.op(ParticleWithLocks.java:47)-beta
""")

  @Test def oneSimpleSafeLock = expectNoRaces

  @Test def imbricatedLocks = expect("""
Loop: particles.ParticleWithLocks.imbricatedLocks(ParticleWithLocks.java:97)

particles.ParticleWithLocks: particles.ParticleWithLocks.imbricatedLocks(ParticleWithLocks.java:95)
 .xyz
   (a)  particles.ParticleWithLocks$6.op(ParticleWithLocks.java:105)
         L: java.lang.Object: particles.ParticleWithLocks$6.op(ParticleWithLocks.java:100)-alpha
   (b)  particles.ParticleWithLocks$6.op(ParticleWithLocks.java:105)
         L: java.lang.Object: particles.ParticleWithLocks$6.op(ParticleWithLocks.java:100)-beta
""")
}