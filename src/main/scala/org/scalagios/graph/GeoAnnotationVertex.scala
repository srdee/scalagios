package org.scalagios.graph

import org.scalagios.api.GeoAnnotation
import org.scalagios.graph.Constants._
import org.scalagios.graph.VertexExtensions._
import com.tinkerpop.blueprints.pgm.Vertex
import org.scalagios.graph.exception.GraphIntegrityException

/**
 * An implementation of the Pelagios <em>GeoAnnotation</em> model primitive
 * backed by a Tinkerpop Graph Vertex.
 * 
 * @author Rainer Simon <rainer.simon@ait.ac.at>
 */
class GeoAnnotationVertex(vertex: Vertex) extends GeoAnnotation {
  
  def uri: String = vertex.getPropertyAsString(ANNOTATION_URI).get
  
  def body: String = vertex.getPropertyAsString(ANNOTATION_BODY).get
  
  // TODO not clean - put this functionality in the graph reader
  // to keep this class consistent with the GeoAnnotation trait!
  def bodyAsPlace: PlaceVertex = {
    val place = vertex.getNeighbour(RELATION_HASBODY)
    if (place.isDefined)
      new PlaceVertex(place.get)
    else
      throw new GraphIntegrityException("Graph corrupt: annotation " + uri + " disconnected from body")
  }
  
  def target: GeoAnnotationTargetVertex = {
    val neighbour = vertex.getNeighbour(RELATION_HASTARGET)
    if (neighbour.isDefined)
      new GeoAnnotationTargetVertex(neighbour.get)
    else      
      throw new GraphIntegrityException("Graph corrupt: annotation " + uri + " disconnected from target")
  }

  def title: Option[String] = vertex.getPropertyAsString(ANNOTATION_TITLE)
  
}