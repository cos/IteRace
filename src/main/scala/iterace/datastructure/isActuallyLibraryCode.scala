package iterace.datastructure

import wala.WALAConversions._

object isActuallyLibraryCode extends SelectorOfClassesAndMethods {
  /**
   * this are classes that are marked as "application" by WALA by I want them to be library
   * quick hack, could be made more elegantly
   */
  def classes = Set(

    // for coref
    "LBJ2.nlp.coref.Document",
    "LBJ2.nlp.coref.Document$Mention",
    "LBJ2.nlp.Sentence",
    "LBJ2.classify.ScoreSet",

    // for WEKA
    "weka.core.SparseInstance",
    "weka.core.DenseInstance",
    "weka.core.AbstractInstance",

    // for lucene
    "org.apache.lucene.util.PriorityQueue",
    "org.apache.lucene.index.SegmentInfo",
    "org.apache.lucene.index.SegmentInfos",
    "org.apache.lucene.analysis.CharArraySet",
    "org.apache.lucene.analysis.Token",
    "org.apache.lucene.analysis.TokenWrapper",
    "org.apache.lucene.analysis.standard.StandardTokenizerImpl",
    "org.apache.lucene.analysis.standard.StandardTokenizer",
    "org.apache.lucene.analysis.standard.StandardTokenizerImpl",
    "org.apache.lucene.search.PhrasePositions",
    "org.apache.lucene.analysis.TokenStream",
    "org.apache.lucene.analysis.standard.StandardAnalyzer",
    "org.apache.lucene.queryParser.QueryParser" // for lucene 4
    )

  def classPatterns = List(
    "LBJ2/nlp/coref/ClusterMerger.*",
    "org/apache/lucene/util/.*",
    "org/apache/lucene/index/MultipleTermPositions.*",
    "org/apache/lucene/search/\\w*Scorer.*",
    "org/apache/lucene/analysis/\\w*Filter.*",
    "org/apache/lucene/analysis/standard/\\w*Filter.*",
    "org/apache/lucene/index/IndexFileDeleter.*",
    "org/apache/lucene/search/\\w*Query.*",
    "org/apache/lucene/search/\\w*Clause.*",
    "org/apache/lucene/search/\\w*Collector.*",
    "org/apache/lucene/store/.*",
    "org/apache/lucene/index/.*",

    // for lucene4
    "org/apache/lucene/search/FieldComparator.*",
    "org/apache/lucene/search/FieldCacheImpl.*")
}

object isActuallyApplicationScope {
  def apply(c: C): Boolean = inApplicationScope(c) && !isActuallyLibraryCode(c)
  def apply(m: M): Boolean = inApplicationScope(m) && !isActuallyLibraryCode(m)
  def apply(n: N): Boolean = inApplicationScope(n) && !isActuallyLibraryCode(n)
}