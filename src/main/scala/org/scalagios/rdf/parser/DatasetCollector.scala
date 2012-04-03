package org.scalagios.rdf.parser

import scala.collection.mutable.HashMap
import org.openrdf.rio.helpers.RDFHandlerBase
import org.openrdf.model.Statement
import org.scalagios.api.{Dataset, DefaultDataset}
import org.openrdf.model.vocabulary.RDF
import org.scalagios.rdf.vocab.{VoID, DCTerms, FOAF}
import org.scalagios.rdf.vocab.Formats

/**
 * Analogous to the OpenRDF <em>StatementCollector</em>, this RDFHandler
 * implementation builds a Pelagios Dataset hierarchy from a VoID RDF file.
 * 
 * @author Rainer Simon <rainer.simon@ait.ac.at>
 */
class DatasetCollector extends RDFHandlerBase with HasStatistics with HasValidation {
  
  /**
   * Maps a Dataset's URI to the Dataset
   */
  private val datasetBuffer = new HashMap[String, DefaultDataset]
  
  /**
   * Maps a Dataset's URI to the its parent Dataset
   */
  private val parentsBuffer = new HashMap[String, DefaultDataset]
 
  private var rootDatasets: List[Dataset] = _
  
  def datasetsTotal = datasetBuffer.size
  
  def getRootDatasets: List[Dataset] = rootDatasets 

  override def handleStatement(statement: Statement): Unit = {
    triplesTotal += 1
    validateDatasets(statement)
    
    val (subj, pred, obj) = (statement.getSubject().stringValue(), statement.getPredicate(), statement.getObject())
    
    (pred, obj) match {
      case (RDF.TYPE, VoID.Dataset) => getOrCreate(subj)
      case (DCTerms.title, _) => getOrCreate(subj).title = obj.stringValue()
      case (DCTerms.description, _) => getOrCreate(subj).description = obj.stringValue()
      case (DCTerms.license, _) => getOrCreate(subj).license = obj.stringValue()
      case (FOAF.homepage, _) => getOrCreate(subj).homepage = obj.stringValue()
      case (VoID.uriSpace, _) => getOrCreate(subj).uriSpace = obj.stringValue()
      case (VoID.dataDump, _) => getOrCreate(subj).datadump = obj.stringValue()
      case (VoID.subset, _) => parentsBuffer.put(obj.stringValue(), getOrCreate(subj))
      case (VoID.feature, _) => {
        val d = getOrCreate(subj)
        val format = Formats.toRDFFormat(obj)
        if (format.isDefined)
        	d.dumpFormat = format.get        
      }
      
      case _ => triplesSkipped += 1
    }

  }
  
  override def endRDF(): Unit = {
    rootDatasets = List[Dataset]()
    
    datasetBuffer.values.foreach(dataset => {
      val parent = parentsBuffer.remove(dataset.uri)
      if (!parent.isEmpty)
        parent.get.subsets = dataset :: parent.get.subsets
      else
        rootDatasets = dataset :: rootDatasets
    })
  }
    
  private def getOrCreate(uri: String): DefaultDataset = {
    datasetBuffer.get(uri) match {
      case Some(d) => d
      case None =>  {
        val d = new DefaultDataset(uri)
        datasetBuffer.put(uri, d)
        d
      }
    }
  }
  
}