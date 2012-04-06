import scala.collection._

package object iterace {
  type Stage = Function1[RacePointerAnalysis, Function1[ProgramRaceSet, ProgramRaceSet]]
}