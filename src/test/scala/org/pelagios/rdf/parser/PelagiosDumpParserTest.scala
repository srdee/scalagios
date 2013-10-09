package org.pelagios.rdf.parser

import org.junit.runner.RunWith
import org.scalatest.FunSuite
import org.scalatest.junit.JUnitRunner
import org.pelagios.Scalagios
import java.io.File

@RunWith(classOf[JUnitRunner])
class PelagiosDumpParserTest extends FunSuite {

  val TEST_FILE = "src/test/resources/test-annotations-vicarello.ttl"
    
  test("Pelagios Data Dump Import") {
    println("Starting Pelagios data import")
    val startTime = System.currentTimeMillis
    
    val things = Scalagios.parseData(new File(TEST_FILE))
    things.foreach(thing => {
      println(thing.title)
      thing.annotations.foreach(annotation => println(annotation.motivatedBy))
    })
    println("Parsed " + things.size + " items")
  
    println("Import complete. Took " + (System.currentTimeMillis - startTime) + " milliseconds")
    assert(things.size > 0)
  }
  
}