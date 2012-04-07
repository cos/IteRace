package iterace

/**
 * read: "filter by locks based on may-alias"
 */
class FilterByLockMayAlias(pa: RacePointerAnalysis) extends Stage {
  import pa._
  
  private val lockSet = new LockSet(pa)
  
  def apply(races: ProgramRaceSet): ProgramRaceSet = {
    new ProgramRaceSet(races.children map (loopRaceSet => {
      val locks = lockSet.getLocks(loopRaceSet.l)
      val locksWithUniqueAbstractObjects = locks.filter({ pointsToUniqueAbstractObject(_) })
      val lockMap = lockSet.getLockSetMapping(loopRaceSet.l, locksWithUniqueAbstractObjects)

      def isSafe(r: Race): Boolean = {
        val lockObjectsA = lockMap(r.a) map { _.p.pt } flatten
        val lockObjectsB = lockMap(r.b) map { _.p.pt } flatten

        lockObjectsA.size == 1 && lockObjectsB.size == 1 && (lockObjectsA & lockObjectsB).size == 1
      }
      loopRaceSet.filter { !isSafe(_) }
    }))
    //    races groupBy { _.l } collect {
    //      case (loop, potentialRaceSet) => {
    //        val locks = lockSet.getLocks(loop)
    //        val locksWithUniqueAbstractObjects = locks.filter({ pointsToUniqueAbstractObject(_) })
    //        val lockMap = lockSet.getLockSetMapping(loop, locksWithUniqueAbstractObjects)
    //
    //        def isSafe(r: Race): Boolean = {
    //          val lockObjectsA = lockMap(r.a) map { _.p.pt } flatten
    //          val lockObjectsB = lockMap(r.b) map { _.p.pt } flatten
    //
    //          lockObjectsA.size == 1 && lockObjectsB.size == 1 && (lockObjectsA & lockObjectsB).size == 1
    //        }
    //
    //        potentialRaceSet filter { !isSafe(_) }
    //      }
  }

  def pointsToUniqueAbstractObject(l: Lock): Boolean = {
    l.p.pt.size == 1
  }
}

object FilterByLockMayAlias extends StageConstructor {
  def apply(pa: RacePointerAnalysis) = {
    new FilterByLockMayAlias(pa)
  }
}