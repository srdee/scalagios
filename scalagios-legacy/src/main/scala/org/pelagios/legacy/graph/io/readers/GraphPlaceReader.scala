package org.pelagios.legacy.graph.io.readers

import scala.collection.JavaConverters._
import org.pelagios.legacy.graph.Constants._
import org.pelagios.legacy.graph.{DatasetVertex, PlaceVertex}
import org.pelagios.legacy.graph.io.PelagiosGraphIOBase
import org.pelagios.legacy.api.{Dataset, Place}

trait GraphPlaceReader extends PelagiosGraphIOBase {

  def getPlaces(): Iterable[Place] =
    placeSubreferenceNode.getOutEdges(RELATION_PLACE).iterator.asScala
      .map(edge => new PlaceVertex(edge.getInVertex)).toIterable
  
  def getPlace(uri: String): Option[Place] = {
    val idxHits = placeIndex.get(PLACE_URI, uri)
    
    if (idxHits.hasNext())
      Some(new PlaceVertex(idxHits.next()))
    else
      None
  }
  
  /**
   * Returns all datasets that reference the specified place
   */
  def getReferencingDatasets(placeUri: String): Iterable[(Dataset, Int)] = {
    val idxHits = placeIndex.get(PLACE_URI, placeUri)
    if (idxHits.hasNext())
      idxHits.next.getInEdges(RELATION_REFERENCES).asScala
        .map(edge => (new DatasetVertex(edge.getOutVertex) -> edge.getProperty(REL_PROPERTY_REFERENCECOUNT).toString.toInt))
    else
      Seq.empty[(Dataset, Int)]
  }
  
  /**
   * Returns all datasets that (i) reference the specified place and
   * (ii) are equal to or located below the specified dataset  
   */
  def getReferencingDatasets(placeUri: String, datasetUri: String): Iterable[(Dataset, Int)] =
    getReferencingDatasets(placeUri).filter{ case(dataset, count) => 
      dataset.uri.equals(datasetUri) || dataset.isChildOf(datasetUri) }
  
}