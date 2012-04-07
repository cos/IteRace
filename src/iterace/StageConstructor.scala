package iterace

trait Stage extends Function1[ProgramRaceSet, ProgramRaceSet]
trait StageConstructor extends Function1[RacePointerAnalysis,Stage]