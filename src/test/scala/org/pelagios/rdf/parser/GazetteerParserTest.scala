package org.pelagios.rdf.parser

import org.scalatest.FunSuite
import org.junit.runner.RunWith
import org.scalatest.junit.JUnitRunner
import org.pelagios.Scalagios
import java.io.File

@RunWith(classOf[JUnitRunner])
class GazetteerParserTest extends FunSuite {
  
  val TEST_FILE = "src/test/resources/one-place.ttl"

  test("Gazetteer Dump Import") {
    println("Starting gazetteer data import")
    val startTime = System.currentTimeMillis
    
    val places = Scalagios.parseGazetteerFile(new File(TEST_FILE))
    places.foreach(place => {
      println(place.uri)
      println(place.title)
      place.names.foreach(name => println(name.labels))
      place.locations.foreach(location => println(location.geometry))
    })  
  }
  
}