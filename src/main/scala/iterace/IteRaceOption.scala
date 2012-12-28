package iterace

import scala.collection._
import com.typesafe.config.Config
import scala.collection.JavaConverters._

sealed trait IteRaceOption {
  val hyphenized = "[A-Z]".r.replaceAllIn(toString, l => "-" + l.matched.toLowerCase).substring(1)
}

object IteRaceOption {
  case object TwoThreads extends IteRaceOption
  case object Filtering extends IteRaceOption
  case object BubbleUp extends IteRaceOption
  case object DeepSynchronized extends IteRaceOption
  case object Synchronized extends IteRaceOption

  val values = Set[IteRaceOption](TwoThreads, Filtering, BubbleUp, DeepSynchronized, Synchronized)
  val mapping = values map { v => (v.hyphenized, v) } toMap
  
  def apply(s: String): IteRaceOption = mapping(s)

  def apply(config: Config): immutable.Set[IteRaceOption] = {
    val opt = config.getObject("iterace.options")
    (opt.asScala filter { case (_, v) => v.unwrapped().asInstanceOf[Boolean] } keys) map { IteRaceOption(_) } toSet
  }
}