package iterace.stage
import iterace.datastructure.ProgramRaceSet
import iterace.pointeranalysis.RacePointerAnalysis

trait Stage extends Function1[ProgramRaceSet, ProgramRaceSet]
trait StageConstructor extends Function1[RacePointerAnalysis,Stage]