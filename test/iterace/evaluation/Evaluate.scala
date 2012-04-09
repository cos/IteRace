package iterace.evaluation
import iterace.stage.RaceAbstractTest
import iterace.IteRaceOption
import iterace.stage.FilterByLockMayAlias
import iterace.stage.BubbleUp

class Evaluate(startClass: String)  extends RaceAbstractTest(startClass) {
	override val options = Set[IteRaceOption](FilterByLockMayAlias, BubbleUp)
}