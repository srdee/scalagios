package org.scalagios.graph.io

import scala.collection.JavaConverters._
import com.tinkerpop.blueprints.pgm.IndexableGraph
import org.scalagios.graph.Constants._
import com.tinkerpop.blueprints.pgm.{Vertex, IndexableGraph}
import org.scalagios.api.{Place, Dataset}
import com.tinkerpop.frames.FramesManager

/**
 * Provides Pelagios-specific Graph DB I/O (read) features.
 * 
 * @author Rainer Simon <rainer.simon@ait.ac.at>
 */
class PelagiosGraphReader[T <: IndexableGraph](graph: T) extends PelagiosGraphIOBase(graph) {
  
  private val framesManager: FramesManager = new FramesManager(graph)

  def getPlaces(): Iterable[Place] = getVertices(PLACE_VERTEX).map(vertex => framesManager.frame(vertex, classOf[Place]))
  
  def getDatasets(): Iterable[Dataset] = getVertices(DATASET_VERTEX).map(vertex => framesManager.frame(vertex, classOf[Dataset]))
  
  private def getVertices(vertexType: String) = graph.getVertices().asScala.filter(_.getProperty(VERTEX_TYPE).equals(vertexType))
  
}