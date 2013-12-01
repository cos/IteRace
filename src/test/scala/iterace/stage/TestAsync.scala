package iterace.stage

import org.junit.Test
import iterace.IteRaceOption._
import iterace.IteRaceOption
import org.junit.Ignore

class TestAsync extends RaceAbstractTest {
  import IteRaceOption._
  val entryClass = "Lasyncsubjects/AndroidTest"
  override val options = Set[IteRaceOption](Filtering, TwoThreads, BubbleUp)
  
  override lazy val entryMethod = "onCreate(Landroid/os/Bundle;)V"

  @Test def compute = expect("""
Loop: async task: Node: < Application, Lasyncsubjects/AndroidTest$1, doInBackground([Lasyncsubjects/AndroidTest$Particle;)Ljava/lang/Void; > Context: Everywhereasyncsubjects.AndroidTest$Particle: asyncsubjects.AndroidTest.onCreate(AndroidTest.java:31)
 .x
   (a)  asyncsubjects.AndroidTest.onCreate(AndroidTest.java:34)
   (b)  asyncsubjects.AndroidTest$1.doInBackground(AndroidTest.java:23)
asyncsubjects.AndroidTest: com.ibm.wala.FakeRootClass.fakeRootMethod(FakeRootClass.java:)
 .raceOnMe
   (a)  asyncsubjects.AndroidTest.onCreate(AndroidTest.java:33)
   (b)  asyncsubjects.AndroidTest$1.doInBackground(AndroidTest.java:22)
""")
}