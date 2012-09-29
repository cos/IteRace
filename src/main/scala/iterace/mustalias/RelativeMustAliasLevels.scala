//package iterace
//import com.ibm.wala.ssa.SSAGetInstruction
//import com.ibm.wala.ssa.SSAPhiInstruction
//import com.ibm.wala.ssa.SSAInvokeInstruction
//import scala.collection.JavaConversions._
//import scala.collection._
//import wala.WALAConversions._
//import com.ibm.wala.analysis.pointers.HeapGraph
//import com.ibm.wala.ssa.SSAPutInstruction
//import iterace.pointeranalysis.RacePointerAnalysis
//
//class RelativeMustAliasLevels(pa: RacePointerAnalysis) {
//  import pa._
////
////  class MA {
////    val ps = Set.empty[PP]
////  	val parentSet = Set.empty[PP]
////    var fieldToParent: F = null
////    
////    def add(p: PP): Boolean = {
////      val i = p.getDef()
////      if(i == null) 
////        false
////      else i match {
////        case i: SSAGetInstruction => {
////          if(fieldToParent == null) fieldToParent = i.getDeclaredField() // for the initial set
////          
////          if(i.getDeclaredField() != fieldToParent) return false
////          
////          parentSet.add(PP(p.n, i.getRef()))
////          ps.add(p)
////        }
////      }
////    }
////  }
//
//  def mustAlias(n: N)(q1: P, q2: P): Boolean = {
//    false
//  }
//
//  case class PP(p: P, cx: List[(N, I)]) {
//    def n = p.n
//    def v = p.v
//    def getDef() = p.getDef()
//    def prettyPrint = p.prettyPrint()
//    def pt = p.pt.toSet
//  }
//  object PP {
//    def apply(n: N, v: V): PP = {
//      PP(P(n, v), List.empty[(N, I)])
//    }
//    def apply(n: N, v: V, cx: List[(N, I)]): PP = {
//      PP(P(n, v), cx)
//    }
//    def apply(p: P): PP = {
//      PP(p, List.empty[(N, I)])
//    }
//  }
//}
//
