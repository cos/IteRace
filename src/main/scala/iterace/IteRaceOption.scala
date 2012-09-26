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
  object BubbleUp extends IteRaceOption {
	  override def toString = "bubble-up"
  }
  object DeepSynchronized extends IteRaceOption {
    override def toString = "deep-synchronized"
  }
  object AppLevelSynchronized extends IteRaceOption {
    override def toString = "app-level-synchronized"
  }

  def apply(s: String): IteRaceOption = s match {
    case "2-threads-model" => TwoThreadModel
    case "known-safe-filtering" => KnownSafeFiltering
    case "bubble-up" => BubbleUp
    case "deep-synchronized" => DeepSynchronized
    case "app-level-synchronized" => AppLevelSynchronized
  }
}

object IteRaceOptions {
  import IteRaceOption._

  def apply(s: IteRaceOption*): immutable.Set[IteRaceOption] = apply(s)
  def apply(s: Traversable[IteRaceOption]): immutable.Set[IteRaceOption] = immutable.Set[IteRaceOption]() ++ s

  def all = apply(TwoThreadModel, KnownSafeFiltering, BubbleUp, DeepSynchronized, AppLevelSynchronized)
  def allAsString = all map { _.toString }
  
  def powersetOfStrings = powerset(allAsString) map {_.mkString("_")}

  def powerset[T](s: Set[T]): Set[Set[T]] = {
    if (s.tail.isEmpty)
      Set(Set(), s)
    else {
      val other = powerset(s.tail)
      (other) ++ (other map { _ + s.head })
    }
  }
}