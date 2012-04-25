package org.scalagios.graph.io.writers

import scala.collection.JavaConverters._

import com.tinkerpop.blueprints.pgm.{IndexableGraph, TransactionalGraph}
import com.tinkerpop.blueprints.pgm.TransactionalGraph.Conclusion

import org.scalagios.api.{GeoAnnotation, Place}
import org.scalagios.graph.Constants._
import org.scalagios.graph.GeoAnnotationVertex
import org.scalagios.graph.exception.GraphIOException
import org.scalagios.graph.io.PelagiosGraphIOBase

trait GraphPlaceWriter extends PelagiosGraphIOBase {
  
  def insertPlaces(places: Iterable[Place]) = {    
    if (graph.isInstanceOf[TransactionalGraph]) {
      val tGraph = graph.asInstanceOf[TransactionalGraph]
      tGraph.setMaxBufferSize(0)
      tGraph.startTransaction()
    }

    places.foreach(place => {
      val normalizedURL = normalizeURL(place.uri)
      
      val vertex = graph.addVertex(null)
      vertex.setProperty(VERTEX_TYPE, PLACE_VERTEX)
      vertex.setProperty(PLACE_URI, normalizedURL)
      if (place.label.isDefined) vertex.setProperty(PLACE_LABEL, place.label.get)
      if (place.comment.isDefined) vertex.setProperty(PLACE_COMMENT, place.comment.get)
      if (place.altLabels.size > 0) vertex.setProperty(PLACE_ALTLABELS, place.altLabels.get)
      if (place.coverage.isDefined) vertex.setProperty(PLACE_COVERAGE, place.coverage.get)
      if (place.geometryWKT.isDefined) vertex.setProperty(PLACE_GEOMETRY, place.geometryWKT.get)    
      vertex.setProperty(PLACE_LON, place.lon)
      vertex.setProperty(PLACE_LAT, place.lat)
      
      // Add to index
      placeIndex.put(PLACE_URI, normalizedURL, vertex)
      if (place.label.isDefined) placeIndex.put(PLACE_LABEL, place.label.get, vertex)
      if (place.comment.isDefined) placeIndex.put(PLACE_COMMENT, place.comment.get, vertex)
      if (place.altLabels.isDefined) placeIndex.put(PLACE_ALTLABELS, place.altLabels.get, vertex)
      if (place.coverage.isDefined) placeIndex.put(PLACE_COVERAGE, place.coverage.get, vertex)
    })
    
    // Create PLACE -- within --> PLACE relations
    places.filter(place => place.within.isDefined).foreach(place => {
      val normalizedURL = normalizeURL(place.uri)
      val normalizedWithin = normalizeURL(place.within.get.uri)
      
      val origin =
        if (placeIndex.count(PLACE_URI, normalizedURL) > 0) placeIndex.get(PLACE_URI, normalizedURL).next()
        else null
        
      val destination = 
        if (placeIndex.count(PLACE_URI, normalizedWithin) > 0) placeIndex.get(PLACE_URI, normalizedWithin).next()
        else null
        
      if (origin == null || destination == null)
        throw GraphIOException("Could not create relation: " + normalizedURL + " WITHIN " + normalizedWithin)
      else
        graph.addEdge(null, origin, destination, RELATION_WITHIN)      
    })
    
    // If there are annotations in the DB already, re-wire them
    var floatingAnnotations = List.empty[GeoAnnotation]
    graph.getVertices().asScala.filter(_.getProperty(VERTEX_TYPE).equals(ANNOTATION_VERTEX)).foreach(annotation => {
      val hasBody = annotation.getProperty(ANNOTATION_BODY)
      
      if (placeIndex.count(PLACE_URI, hasBody) > 0) {
        val place = placeIndex.get(PLACE_URI, hasBody).next()
        graph.addEdge(null, annotation, place, RELATION_HASBODY)
      } else {
        floatingAnnotations ::= new GeoAnnotationVertex(annotation)
      }
    })
    if (floatingAnnotations.size > 0)
      throw new GraphIOException("Could not re-wire all annotations after Place import:\n" +
      	floatingAnnotations.mkString("\n"))
    
    if (graph.isInstanceOf[TransactionalGraph])
      graph.asInstanceOf[TransactionalGraph].stopTransaction(Conclusion.SUCCESS)
  }
  
}