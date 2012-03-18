package iterace

class FilterByMayAlias(pa: PointerAnalysis, helpers: PAHelpers, lockSet:LockSet) extends Function1[Set[Race], Set[Race]] {
  import pa._
  import helpers._
  
  def apply(possibleRaces: Set[Race]):Set[Race] = {
    (possibleRaces groupBy { _.l } collect {
      case (loop, potentialRaceSet) => {
        val locks = lockSet.getLocks(loop)
        val locksWithUniqueAbstractObjects = locks.filter({ pointsToUniqueAbstractObject(_) })
        val lockMap = lockSet.getLockSetMapping(loop, locksWithUniqueAbstractObjects)

        def isSafe(r: Race): Boolean = {
          val lockObjectsA = lockMap(r.a) map { _.p.pt } flatten
          val lockObjectsB = lockMap(r.b) map { _.p.pt } flatten

          lockObjectsA.size == 1 && lockObjectsB.size == 1 && (lockObjectsA & lockObjectsB).size == 1
        }

        potentialRaceSet filter { !isSafe(_) }
      }
    } flatten) toSet
  }
  
  def pointsToUniqueAbstractObject(l: Lock): Boolean = {
    l.p.pt.size == 1
  }
}