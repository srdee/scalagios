package org.pelagios.rdf.parser

import org.pelagios.api._
import org.pelagios.rdf.vocab._
import org.openrdf.model.{ Literal, URI, Value }
import org.openrdf.model.vocabulary.{ RDF, RDFS }

/** An implementation of [[org.pelagios.rdf.parser.ResourceCollector]] to handle Gazetteer dump files.
  * 
  * @author Rainer Simon <rainer.simon@ait.ac.at>
  */
class GazetteerParser extends ResourceCollector {
  
  /** The Places collected by the parser.
   *  
    * @return the list of Places
    */
  def places: Iterable[Place] = {
    // All RDF resources, grouped by their type (Place, Name, Location) 
    val typedResources = groupByType(        
      // We're looking for PlaceRecords, Names and Locations
      Seq(Pelagios.PlaceRecord, PleiadesPlaces.Name, PleiadesPlaces.Location),
      
      Seq(
        // Identify resources that have asWKT, asGeoJSON or lat predicates as Location
        (resource => if (resource.hasAnyPredicate(Seq(OSGeo.asWKT, OSGeo.asGeoJSON, W3CGeo.lat))) Some(PleiadesPlaces.Location) else None),
        
        // Identify resources that have a skos:label as place name
        (resource => if (resource.hasPredicate(SKOS.label)) Some(PleiadesPlaces.Name) else None)
      ))
    
    // Just the Names
    val allNames = typedResources.get(PleiadesPlaces.Name).getOrElse(Map.empty[String, Resource])
    
    // Just the Locations
    val allLocations = typedResources.get(PleiadesPlaces.Location).getOrElse(Map.empty[String, Resource])
    
    // Places, with Names and Locations in-lined 
    typedResources.get(Pelagios.PlaceRecord).getOrElse(Map.empty[String, Resource]).map { case (uri, resource) => 
      val names = resource.get(PleiadesPlaces.hasName).map(uri => allNames.get(uri.stringValue).map(new NameResource(_))).toSeq.flatten
      val locations = resource.get(PleiadesPlaces.hasLocation).map(uri => allLocations.get(uri.toString).map(new LocationResource(_))).toSeq.flatten
      new PlaceResource(resource, names, locations)
    }
  }  

}

/** Wraps a pelagios:PlaceRecord resource as a Place domain model primitive, with Names and Locations in-lined.
 *  
 *  @constructor create a new PlaceResource
 *  @param resource the RDF resource to wrap
 *  @param names the names connected to the resource
 *  @param locations the locations connected to the resource
 */
private[parser] class PlaceResource(resource: Resource, val names: Seq[NameResource], val locations: Seq[LocationResource]) extends Place {

  def uri = resource.uri
  
  def title = resource.getFirst(DCTerms.title).map(_.stringValue).getOrElse("[NO TITLE]") // 'NO TITLE' should never happen!
  
  def descriptions = (resource.get(RDFS.COMMENT) ++ resource.get(DCTerms.description)).map(ResourceCollector.toLabel(_))
  
  // TODO
  def subjects = Seq.empty[String]
  
  def closeMatches = resource.get(SKOS.closeMatch).map(_.stringValue)

}

/** Wraps a pleiades:Name RDF resource as a Name domain model primitive.
  *
  * @constructor create a new NameResource
  * @param resource the RDF resource to wrap   
  */
private[parser] class NameResource(resource: Resource) extends Name {
  
  def labels: Seq[Label] = resource.get(SKOS.label).map(ResourceCollector.toLabel(_))
  
  def altLabels: Seq[Label] = resource.get(SKOS.altLabel).map(ResourceCollector.toLabel(_))
    
}

/** Wraps a pleiades:Location RDF resource as a Location domain model primitive.
  *  
  * @constructor create a new LocationResource
  * @param resource the RDF resource to wrap
  */
private[parser] class LocationResource(resource: Resource) extends Location {

  def wkt: Option[String] = resource.getFirst(OSGeo.asWKT).map(_.stringValue)
  
  def geoJson: Option[String] = resource.getFirst(OSGeo.asGeoJSON).map(_.stringValue)
  
  def lonlat: Option[(Double, Double)] = {
    val lon = resource.getFirst(W3CGeo.long).map(_.asInstanceOf[Double])
    val lat = resource.getFirst(W3CGeo.lat).map(_.asInstanceOf[Double])
    if (lon.isDefined && lat.isDefined)
      Some((lon.get, lat.get))
    else
      None
  }
  
  def descriptions: Seq[Label] =
    (resource.get(DCTerms.description) ++ resource.get(RDFS.LABEL)).map(ResourceCollector.toLabel(_))
  
}

