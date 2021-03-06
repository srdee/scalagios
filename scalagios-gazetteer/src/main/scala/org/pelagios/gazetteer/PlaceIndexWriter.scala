package org.pelagios.gazetteer

import org.pelagios.api.Place
import org.apache.lucene.index.{ IndexWriter, IndexWriterConfig }
import org.apache.lucene.util.Version
import org.slf4j.Logger
import org.apache.lucene.index.Term

trait PlaceIndexWriter extends PlaceIndexReader {
    
  def addPlaces(places: Iterable[Place]) = {
    val writer = new IndexWriter(index, new IndexWriterConfig(Version.LUCENE_44, analyzer))
    
    places.foreach(place => {
      if (findByURI(GazetteerUtils.normalizeURI(place.uri)).isDefined) {
        log.warn("Place '" + place.uri + "' already in index!")
      } else {
        // Places that this place lists as closeMatch
        val closeMatchesOut = place.closeMatches.map(uri => findByURI(GazetteerUtils.normalizeURI(uri))).filter(_.isDefined).map(_.get)

        // Places in the index that list this place as closeMatch
        val closeMatchesIn = findByByCloseMatch(GazetteerUtils.normalizeURI(place.uri))
        
        val closeMatches = closeMatchesOut ++ closeMatchesIn
        
        // All closeMatches need to share the same seed URI
        val seedURI =
          if (closeMatches.size > 0) 
            closeMatches(0).seedURI
          else
            GazetteerUtils.normalizeURI(place.uri)
 
        // Update seed URIs where necessary
        updateSeedURI(closeMatches.filter(!_.seedURI.equals(seedURI)), seedURI, writer)
        
        // Add new document to index
        writer.addDocument(PlaceDocument(place, Some(seedURI)))
      }   
    })
    
    writer.close()
  }
  
  def updateSeedURI(places: Seq[PlaceDocument], seedURI: String, writer: IndexWriter) = {
    places.foreach(place => {
      // Delete doc from index
      writer.deleteDocuments(new Term(PlaceIndex.FIELD_URI, place.uri))
      
      // Update seed URI and re-add
      writer.addDocument(PlaceDocument(place, Some(seedURI)))
    })
  }

}