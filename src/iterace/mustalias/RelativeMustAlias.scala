package iterace
import com.ibm.wala.ssa.SSAGetInstruction
import com.ibm.wala.ssa.SSAPhiInstruction
import com.ibm.wala.ssa.SSAInvokeInstruction
import scala.collection.JavaConversions._
import scala.collection._
import iterace.util.WALAConversions._
import com.ibm.wala.analysis.pointers.HeapGraph
import com.ibm.wala.ssa.SSAPutInstruction
import iterace.pointeranalysis.PointerAnalysis

class RelativeMustAlias(pa: PointerAnalysis) {
  import pa._

  /**
   * Check whether p1 must aliases p2 when in a particular call of n
   * i.e., \forall e \in c(N). \exists o^e.
   * 					\forall o1^e \in c^e (p1). o1^e = o^e   \land
   *  				\forall o2^e \in c^e (p2). o2^e = o^e
   *
   * we assume n is a cut vertex, otherwise we go outside the loop code
   * if needed, the implementation can be adjusted to only stick to
   * descendants of n.
   *
   */

  val cacheKnownMA = Map.empty[N, mutable.Set[(PP, PP)]] // cached ma relations
  val cacheKnownNMA = Map.empty[N, mutable.Set[(PP, PP)]] // cached ma we know we cannot prove

  def mustAlias(n: N)(q1: P, q2: P): Boolean = {

    // keeps the already computed ma relations
    val knownMA = cacheKnownMA.getOrElse(n, mutable.Set.empty[(PP, PP)])
    // lazy must alias
    def lma(q1: PP, q2: PP) = knownMA(q1, q2) || knownMA(q2, q1)

    // keeps the ma relations known failed attempts
    // it also keeps the ma realation that are currently under computation
    // this is used for eliminating cycles.
    // if a ma relation is requested but is already here, we've
    // detected a cycle and consider the objective unprovable
    val knownNMA = cacheKnownNMA.getOrElse(n, mutable.Set.empty[(PP, PP)])
    def lnma(q1: PP, q2: PP) = knownNMA(q1, q2) || knownNMA(q2, q1)

    // a wrapper over the ma relation, used for caching computation and
    // cycle elimination
    def ma(q1: PP, q2: PP): Boolean = {
      if (lma(q1, q2)) {
        //("Seen before")
        return true
      }

      // to keep the implementation simple, we don't define symmetric relations
      // and rely on the underlying symmetry of the property
      // when we cannot prove (q1,q2) we try (q2, q1)
      // so, we know we cannot prove something when both permutations failed
      if (lnma(q1, q2)) {
        //("Proved false before")
        return false
      }
      knownNMA.add((q1, q2))

      val result = innerMA(q1, q2)

      if (result == true) {
        knownMA.add((q1, q2))
        knownNMA.remove((q1, q2))
        knownNMA.remove((q2, q1))
      } else knownNMA.add((q1, q2))
      result
    }

    def innerMA(q_a: PP, q_b: PP) = {
      // By same initial param
      var result = maBySameInitialParam(q_a, q_b) // same initial param
      //if (result) ("By same initial param")

      result ||= ((q_a.getDef, q_b.getDef) match {
        // Intraprocedural
        // \exists f \in \F . \exists p_a, p_b \in \P . 
        // r(p_a, f, q_a) \land r(p_b, f, q_b) \land \ma(p_a, p_b) \land ... 
        case (i1: SSAGetInstruction, i2: SSAGetInstruction) if i1.getDeclaredField() == i2.getDeclaredField() => {
          val p_a = PP(q_a.n, i1.getRef(), q_a.cx)
          val p_b = PP(q_b.n, i2.getRef(), q_b.cx)
          ma(p_a, p_b) &&
            (for(o <- p_a.pt & p_b.pt;
            		p <- localPtTo(o);
            		i <- p.getPuts() if i.getDeclaredField() == i1.getDeclaredField()) yield {
              //(">")
              ma(q_a, PP(p.n,i.getVal()))
            }).fold(true)(_ && _)
        }
        case _ => false
      })

      result
    }

    def maBySameInitialParam(q1: PP, q2: PP) = q1.n == n && q1 == q2

    ma(PP(q1), PP(q2))
  }

  //  type AP = ((N, V), List[F])
  //
  //  def uniqueAccessPath(n: N, p: (N, V)): AP = {
  //    null
  //  }
  //
  //  def existWritesToIn(ap1: AP, n: N): Boolean = {
  //    false
  //  }
  //
  //  def sources(p: P): Iterator[P] = {
  //    val i = p.n.getDU().getDef(p.v)
  //    i match {
  //      case i: SSAGetInstruction => Iterator(P(p.n, i.getRef()))
  //      case i: SSAPhiInstruction => Iterator(0, i.getNumberOfUses()).map(uindex => P(p.n, i.getUse(uindex)))
  //      case i: SSAInvokeInstruction => {
  ////        for(n <- callGraph.getPossibleTargets(p.n, i.getCallSite()) yield {
  ////          n.getIR().gt
  ////        }
  //        null
  //      } 
  //      case null => 
  //        for(n <- callGraph.getPredNodes(p.n);
  //            s <- callGraph.getPossibleSites(n, p.n);
  //            i <- n.getIR().getCalls(s)
  //        ) yield {  
  //        	P(n, i.getUse(p.v))
  //        }
  //    }
  //  }
  case class PP(p: P, cx: List[(N,I)]) {
    def n = p.n
    def v = p.v
    def getDef() = p.getDef()
    def prettyPrint = p.prettyPrint()
    def pt = p.pt.toSet
  }
  object PP {
    def apply(n: N, v: V): PP = {
      PP(P(n, v), List.empty[(N,I)])
    }
    def apply(n: N, v: V, cx: List[(N,I)]): PP = {
      PP(P(n, v), cx)
    }
    def apply(p: P): PP = {
      PP(p, List.empty[(N,I)])
    }
  }
}