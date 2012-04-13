package org.scalagios.rdf.vocab

/**
 * OSSpatial vocabulary terms.
 * 
 * @author Rainer Simon<rainer.simon@ait.ac.at>
 */
object OSSpatial extends BaseVocab {
  
  val NAMESPACE = "http://data.ordnancesurvey.co.uk/ontology/spatialrelations/"
    
  val within = factory.createURI(NAMESPACE, "within")

}