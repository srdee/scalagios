package org.pelagios.gazetteer

import org.pelagios.api.Place

/** A 'conflated place' which represents the combination of multiple places in the same place network.
  *
  * @param network the network to conflate into a single Place
  * @param prefURISpace a preferred URI prefix for the conflated place (if any place URI in the network matches the prefix, this one will be used)
  * @param prefCoordSource a preferred gazetteer to use for coordinates (if any place URI matches the prefix, this place's coords will be used)
  * @param prefDescriptionSource a preffered gazetter to use for the description (likewise)
  */
class ConflatedPlace(network: Network, 
  prefURISpace: Option[String] = None, prefLocationSource: Option[String] = None, prefDescriptionSource: Option[String] = None) extends Place {
  
  private val place = {
    if (network.places.size == 1) {
      network.places.head
    } else {
      val head = network.places.head
      val tail = network.places.tail
      
      // Pick URI from preferred gazetteer, or just use head if no preference
      val uri = {
        if (prefURISpace.isDefined) {
          val prefSource = network.places.filter(_.uri.startsWith(prefURISpace.get))
          if (prefSource.size > 0)
            prefSource.head.uri
          else
            head.uri
        } else {
          head.uri
        }
      }
    
      // Pick locations from preferred source, or just use head if no preference
      val locations = {
        if (prefLocationSource.isDefined) {
          val prefSource = network.places.filter(_.uri.startsWith(prefLocationSource.get))
          if (prefSource.size > 0)
            prefSource.head.locations
          else
            head.locations
        } else {
          head.locations
        }
      }
      
      // Pick description from preferred source, or just use head if no preference
      val descriptions = {
        if (prefDescriptionSource.isDefined) {
          val prefSource = network.places.filter(_.uri.startsWith(prefDescriptionSource.get))
          if (prefSource.size > 0)
            prefSource.head.descriptions
          else
            head.descriptions
        } else {
          head.descriptions
        }
      } 

      val names =  { head.names ++ tail.flatMap(_.names) }.toSet // Merge names and de-duplicate
      val closeMatches = head.closeMatches ++ tail.flatMap(_.closeMatches)
      
      Place(uri, head.title, descriptions, 
          names.toSeq,
          locations, 
          head.category, // TODO not sure how to handle different category definitions...
          head.subjects ++ tail.flatMap(_.subjects), // Merge all subjects,
          { head.closeMatches ++ tail.flatMap(_.closeMatches) }.toSet.filter(!_.equals(uri)).toSeq) // Merge & de-duplicate    
    }
  }
  
  def uri = place.uri
  
  def title = place.title
  
  def descriptions = place.descriptions
  
  def names = place.names
  
  def locations = place.locations
  
  def category = place.category
  
  def subjects = place.subjects
  
  def closeMatches = place.closeMatches
  
  lazy val seedURI = network.seedURI 

}