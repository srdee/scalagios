package org.pelagios.rdf.vocab

/** Pelagios custom metadata terms **/
object Pelagios extends BaseVocab("http://pelagios.github.io/terms#") {

  val AnnotatedThing = createURI("AnnotatedThing")
  
  val PlaceRecord = createURI("PlaceRecord")
  
  val toponym = createURI("toponym")
  
  val hasNext = createURI("hasNext")
  
  val neighbour = createURI("neighbour")
  
  val distance = createURI("distance")

}