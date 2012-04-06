package iterace

trait Stage extends Function1[RacePointerAnalysis, Function1[ProgramRaceSet, ProgramRaceSet]]