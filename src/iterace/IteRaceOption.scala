package iterace

import scala.collection._

trait IteRaceOption 

object IteRaceOption {
  object TwoThreadModel extends IteRaceOption {
    override def toString = "2-threads-model"
  }
  object KnownSafeFiltering extends IteRaceOption {
    override def toString = "known-safe-filtering"
  }
  object AppLibMembrane extends IteRaceOption {
    override def toString = "app-lib-membrane"
  }
  object DeepSynchronized extends IteRaceOption {
    override def toString = "deep-synchronized"
  }
  object AppLevelSynchronized extends IteRaceOption {
    override def toString = "app-level-synchronized"
  }
  object BubbleUp extends IteRaceOption {
    override def toString = "bubble-up"
  }
  
  def apply(s: String):IteRaceOption = s match {
    case "2-threads-model" => TwoThreadModel
    case "known-safe-filtering" => KnownSafeFiltering
    case "app-lib-membrane" => AppLibMembrane
    case "bubble-up" => BubbleUp
    case "deep-synchronized" => DeepSynchronized
    case "app-level-synchronized" => AppLevelSynchronized
  }
}

object IteRaceOptions {
  def apply(s: IteRaceOption*):immutable.Set[IteRaceOption] = apply(s)
  def apply(s: Traversable[IteRaceOption]): immutable.Set[IteRaceOption] = immutable.Set[IteRaceOption]() ++ s
}