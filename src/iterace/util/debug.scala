package iterace.util

object debug {
  var active = false
  def activate = active = true

  private var detailedContexts = false
  //  def activateDetailedContexts = detailedContexts = true
  def detailContexts = detailedContexts && active

  def apply(note: Any) = { display(note) }

  def display(note: Any) {
    if (active)
      println(note)
  }
}