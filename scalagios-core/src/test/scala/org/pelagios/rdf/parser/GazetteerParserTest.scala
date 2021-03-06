package org.pelagios.rdf.parser

import java.io.File
import org.junit.runner.RunWith
import org.scalatest.FunSuite
import org.scalatest.junit.JUnitRunner
import org.pelagios.Scalagios

@RunWith(classOf[JUnitRunner])
class GazetteerParserTest extends FunSuite {
  
  val TEST_FILE = "src/test/resources/test-places-pleiades.ttl"

  test("Gazetteer Dump Import") {
    val places = Scalagios.readPlaces(new File(TEST_FILE))
    assert(places.size == 483, "invalid number of places")
    places.foreach(place => {
      assert(place.title != null, "title is null")
      assert(place.uri.startsWith("http://pleiades.stoa.org/places/") ||
             place.uri.startsWith("http://atlantides.org/capgrids/"), "invalid place URI - " + place.uri)
    })
    
    val placesWithLocations = places.filter(_.locations.size > 0)
    assert(placesWithLocations.size == 377, "invalid number of places with locations (" + placesWithLocations.size + ")")
    placesWithLocations.foreach(p => { 
      assert(p.locations.size == 1, "place has more than one location")
      assert(p.locations(0).geometry != null, "place has null geometry")
    })
    
    val placesWithNames = places.filter(_.names.size > 0)
    assert(placesWithNames.size == 449, "invalid number of places with names")
  }
  
}