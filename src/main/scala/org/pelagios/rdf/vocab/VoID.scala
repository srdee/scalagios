package org.pelagios.rdf.vocab

object VoID extends BaseVocab {
  
  val NAMESPACE = "http://rdfs.org/ns/void#"
    
  val Dataset = factory.createURI(NAMESPACE, "Dataset")

  val dataDump = factory.createURI(NAMESPACE, "dataDump")
  
  val inDataset = factory.createURI(NAMESPACE, "inDataset")

  val subset = factory.createURI(NAMESPACE, "subset")  
  
  val uriRegexPattern = factory.createURI(NAMESPACE, "uriRegexPattern")
  
  val uriSpace = factory.createURI(NAMESPACE, "uriSpace")
  
}