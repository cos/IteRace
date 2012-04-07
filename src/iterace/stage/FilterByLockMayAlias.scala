package iterace.stage
import iterace.pointeranalysis.RacePointerAnalysis
import iterace.datastructure.ProgramRaceSet
import iterace.datastructure.Lock
import iterace.datastructure.LockSet
import iterace.datastructure.Race
import iterace.datastructure.MayAliasLockConstructor

/**
 * read: "filter by locks based on may-alias"
 */
class FilterByLockMayAlias(pa: RacePointerAnalysis) extends Stage {
  import pa._
  
  private val lockSet = new LockSet(pa, new MayAliasLockConstructor(pa))
  
  def apply(races: ProgramRaceSet): ProgramRaceSet = {
    new ProgramRaceSet(races.children map (loopRaceSet => {
      val locks = lockSet.getLocks(loopRaceSet.l)
      val lockMap = lockSet.getLockSetMapping(loopRaceSet.l, locks)

      def isSafe(r: Race): Boolean = {
        val aLocks = lockMap(r.a)
        val bLocks = lockMap(r.b)

        (aLocks & bLocks).size > 0
      }
      loopRaceSet.filter { !isSafe(_) }
    }))
  }
}

object FilterByLockMayAlias extends StageConstructor {
  def apply(pa: RacePointerAnalysis) = {
    new FilterByLockMayAlias(pa)
  }
}