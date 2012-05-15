package iterace.stage
import iterace.pointeranalysis.RacePointerAnalysis
import iterace.datastructure.ProgramRaceSet
import iterace.datastructure.Lock
import iterace.datastructure.LockSets
import iterace.datastructure.Race
import iterace.datastructure.MayAliasLockConstructor
import iterace.IteRaceOption

/**
 * read: "filter by locks based on may-alias"
 */
class FilterByLockMayAlias(pa: RacePointerAnalysis, lockMapping: LockSets) extends Stage {
  import pa._
  
  def apply(races: ProgramRaceSet): ProgramRaceSet = {
    new ProgramRaceSet(races.children map (loopRaceSet => {
      val locks = lockMapping.getLocks(loopRaceSet.l)
      val lockMap = lockMapping.getLockSetMapping(loopRaceSet.l)

      def isSafe(r: Race): Boolean = {
        val aLocks = lockMap(r.a); r.a.lockset = Option(aLocks)
        val bLocks = lockMap(r.b); r.b.lockset = Option(bLocks)

        (aLocks & bLocks).size > 0
      }
      loopRaceSet.filter { !isSafe(_) }
    }) filter { _.size > 0})
  }
}