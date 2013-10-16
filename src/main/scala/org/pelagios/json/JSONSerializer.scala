package org.pelagios.json

import org.pelagios.api._
import net.liftweb.json._
import org.pelagios.api.layout.Link
import com.vividsolutions.jts.geom.Coordinate

object JSONSerializer {
  
  import net.liftweb.json.JsonDSL._
      
  private def placeToJSON(place: Place): JObject = {
    val names = place.names.map(name => name.labels ++ name.altLabels).flatten.map(_.label)
    
    val locations = if (place.locations.size > 0) {
      val features = place.locations.map(location => {  
        ("type" -> "Feature") ~ 
        ("geometry" -> (parse(location.geoJSON)))
      })
      Some(("type" -> "FeatureCollection") ~ ("features" -> features))
    } else {
      None
    }
    
    ("source" -> place.uri) ~
    ("title" -> place.title) ~
    ("names" -> names) ~
    ("geometry" -> locations) 
  }
  
  private def _serializeAnnotations(annotations: Seq[Annotation], 
                                   prettyPrint: Boolean = false, 
                                   geoResolutionFn: Option[String => Option[Place]] = None): String = {
    
    // First, filter out annotations that do not provide gazetteer URIs
    val annotationsWithPlaceReference = annotations.filter(_.place.size > 0)
    
    // We pick the first gazetteer URI in the list
    // The spec requires them to be identical as far as georesolution is concerned, anyway
    val data: Seq[(Annotation, Place)] = annotationsWithPlaceReference.map(annotation =>
      (annotation, geoResolutionFn.get(annotation.place(0))))
      .filter(_._2.isDefined) // We filter out all place we couldn't resolve
      .map(tuple => (tuple._1, tuple._2.get)) // And get rid of the 'Option' wrapper
      
    val json = data.map { case (annotation, place) => {
      ("transcription" -> annotation.transcription.map(_.name)) ~
      ("place" -> placeToJSON(place))
    }}
      
    if (prettyPrint) pretty(render(json)) else compact(render(json))
  }
  
  def serializePlaces(places: Seq[Place], prettyPrint: Boolean = false): String = {
    val json = places.map(placeToJSON(_)) 
    if (prettyPrint) pretty(render(json)) else compact(render(json))
  }
  
  def serializeAnnotations(annotations: Seq[Annotation]): String = 
    _serializeAnnotations(annotations, false, None)
  
  def serializeAnnotations(annotations: Seq[Annotation], pretty: Boolean): String = 
    _serializeAnnotations(annotations, pretty, None)
  
  def serializeAnnotations(annotations: Seq[Annotation], pretty: Boolean, geoResolutionFn: String => Option[Place]): String = 
    _serializeAnnotations(annotations, pretty, Some(geoResolutionFn))
    

  def serializeLinks(links: Seq[Link], prettyPrint: Boolean, geoResolutionFn: String => Option[Place]): String = {
    val lines = links.foldLeft(Seq.empty[(Coordinate, Coordinate)])((result, current) => {
      if (current.from.place.size > 0 && current.to.place.size > 0) {
        val from = geoResolutionFn(current.from.place(0))
        val to = geoResolutionFn(current.to.place(0))
      
        if (from.isDefined && to.isDefined) {
          val fromLocations = from.get.locations
          val toLocations = to.get.locations
        
          if (fromLocations.size > 0 && toLocations.size > 0) {
            val fromCoord = fromLocations(0).geometry.getCentroid.getCoordinate
            val toCoord = toLocations(0).geometry.getCentroid.getCoordinate
            (fromCoord, toCoord) +: result
          } else {
            result  
          }
        } else {
          result      
        }
      } else {
        result
      }
    })
    
    val json = lines.map { case (from, to) => {
      ("type" -> "Feature") ~ 
      ("geometry" -> 
          ("type" -> "LineString") ~ 
          ("coordinates" -> Seq(Seq(from.x, from.y), Seq(to.x, to.y)))
      )
    }}
    
    if (prettyPrint) pretty(render(json)) else compact(render(json))
  }

}