package org.pelagios.api

import java.util.Date
import scala.collection.mutable.ListBuffer

/** 'AnnotatedThing' model entity.
  * 
  * @author Rainer Simon <rainer.simon@ait.ac.at>
  */
trait AnnotatedThing extends AnnotationTarget {
  
  /** A URI for the annotated thing.
    *  
    * The URI should be in the namespace of the institution or project providing the
    * data, and should resolve to RDF. The easiest way to achieve this is to use a
    * hash URI, i.e. by appending a fragment identifier to the HTTP URI of the dump 
    * file. Examples:
    * 
    * http://example.com/pelagios.rdf#item  
    * http://example.com/pelagios.rdf#items/01
    */
  def uri: String
  
  /** dcterms:title **/
  def title: String
  
  /** frbr:realizationOf
    *  
    * An annotated thing may be a "Work" (such as "The Vicarello Beakers") or
    * an "Expression" (such as "the fourth Vicarello Beaker, discovered in 1863"). 
    * To quote the FRBR definition, a Work is "an abstract notion of an 
    * artistic or intellectual creation", whereas an Expression is "a realization
    * of a single work usually in a physical form."
    *  
    * http://en.wikipedia.org/wiki/Functional_Requirements_for_Bibliographic_Records
    * http://vocab.org/frbr/core.html
    * 
    * If the annotated thing represents a Expression, frbr:realizationOf links
    * to the work it realizes.
    */
  def realizationOf: Option[AnnotatedThing]  
  
  /** dcterms:identifier
    *  
    * An unambiguous reference for the annotated thing. We recommend
    * the use of a Wikidata URI!
    */ 
  def identifier: Option[String]

  /** dcterms:description **/
  def description: Option[String]
  
  /** foaf:homepage 
    *
    * In Pelagios, we use this property exclusively to describe an official homepage
    * for an annotated thing. E.g. if the thing is an archaeological artefact held in
    * a museum, the homepage would be the item's page on the museum's online database.
    * For related pages, such as pages about the item from other institutions, or 
    * a Wikipedia page, use foaf:primaryTopicOf.  
    */
  def homepage: Option[String]
  
  /** dcterms:source
    *
    * According to the DCMI definition: "a related resource from which the described 
    * resource is derived". In Pelagios, use this field to provide references to Web 
    * URIs from where digital source material was obtained (e.g. a Web page with toponym 
    * lists, etc.)   
    */
  def sources: Seq[String]
  
  /** foaf:primaryTopicOf
    *  
    * According to the FOAF definition: "a document that this thing is the primary topic of".
    * In Pelagios, we use this property to provide references to Web URIs that are about
    * this annotated thing.
    */
  def primaryTopicOf: Seq[String]
  
  /** dcterms:temporal
    * 
    * According to the DCMI definition the "temporal coverage" or "temporal
    * characteristics of the resource". We recommend using the DCMI
    * Period Encoding Scheme: http://dublincore.org/documents/dcmi-period/
    */
  def temporal: Option[PeriodOfTime]
  
  /** dcterms:creator
    *
    * According to the DCMI definition "an entity primarily responsible for making the 
    * resource".
    */
  def creator: Option[Agent]
  
  /** dcterms:contributor
    * 
    * According to the DCMI definition "an entity responsible for making contributions to the
    * resource".
    */
  def contributors: Seq[Agent]
  
  /** dcterms:language 
    *
    * Use ISO 639-2 language codes - see http://en.wikipedia.org/wiki/List_of_ISO_639-2_codes
    */
  def languages: Seq[String]
  
  /** foaf:thumbnails **/
  def thumbnails: Seq[String]
  
  /** foaf:depiction
    *
    * TODO we may want to make this an object rather than a string in the future,
    * in order to hold creator and license information along with the image URL
    * as well.   
    */
  def depictions: Seq[String]
  
  /** dcterms:biblographicCitation
    *  
    * A (list of) bibliographic citation(s) in free text format.    
    */
  def bibliographicCitations: Seq[String]
  
  /** dcterms:subjects 
    * 
    * According to the DCMI definition: "the topic of the resource"
    */
  def subjects: Seq[String]  
 
  /** Expressions (as defined by through frbr:realizationOf)
    *   
    * If the annotated thing represents a Work, this method returns the expressions
    * linked to it.
    */ 
  def expressions: Seq[AnnotatedThing]
  
  /** The annotations on the annotated thing (if any). **/
  def annotations: Seq[Annotation]
  
  /** Convenience method to list tags used in annotations on this thing **/
  def getTags: Seq[Tag] = annotations.map(_.tags).flatten.toSet.toSeq
  
}

/** A default POJO-style implementation of AnnotatedThing. **/
private[api] class DefaultAnnotatedThing(
    
  val uri: String, 
    
  val title: String,
  
  val realizationOf: Option[AnnotatedThing] = None,
    
  val identifier: Option[String] = None,
  
  val description: Option[String] = None,
  
  val homepage: Option[String] = None,
  
  val sources: Seq[String] = Seq.empty[String],
  
  val primaryTopicOf: Seq[String] = Seq.empty[String],
  
  val temporal: Option[PeriodOfTime] = None,

  val creator: Option[Agent] = None,
  
  val contributors: Seq[Agent] = Seq.empty[Agent],
  
  val languages: Seq[String] = Seq.empty[String],
  
  val thumbnails: Seq[String] = Seq.empty[String],
  
  val depictions: Seq[String] = Seq.empty[String],
  
  val bibliographicCitations: Seq[String] = Seq.empty[String],
  
  val subjects: Seq[String] = Seq.empty[String]
      
) extends AnnotatedThing {

  // If this thing is an expression, create 'downwards' relation Work->Expression
  if (realizationOf.isDefined) {
    if (realizationOf.get.isInstanceOf[DefaultAnnotatedThing])
      realizationOf.get.expressions.asInstanceOf[ListBuffer[AnnotatedThing]].append(this)
    else
      throw new RuntimeException("cannot mix different model impelementation types - requires instance of DefaultAnnotatedThing")
  }
  
  val expressions: ListBuffer[AnnotatedThing] =  ListBuffer.empty[AnnotatedThing]
  
  val annotations: ListBuffer[Annotation] = ListBuffer.empty[Annotation]
  
}

/** Companion object with a pimped apply method for generating DefaultAnnotatedThing instances **/
object AnnotatedThing extends AbstractApiCompanion {
  
  def apply(uri: String, title: String,
      
            realizationOf: ObjOrOption[AnnotatedThing] = new ObjOrOption(None),
            
            identifier: ObjOrOption[String] = new ObjOrOption(None),
            
            description: ObjOrOption[String] = new ObjOrOption(None),
            
            homepage: ObjOrOption[String] = new ObjOrOption(None),
            
            sources: ObjOrSeq[String] = new ObjOrSeq(Seq.empty),
            
            primaryTopicOf: Seq[String] = Seq.empty[String],
            
            temporal: PeriodOfTime = null,
            
            creator: Agent = null,
            
            contributors: Seq[Agent] = Seq.empty[Agent],
            
            languages: ObjOrSeq[String] = new ObjOrSeq(Seq.empty),
            
            thumbnails: ObjOrSeq[String] = new ObjOrSeq(Seq.empty),
            
            depictions: ObjOrSeq[String] = new ObjOrSeq(Seq.empty),
            
            bibliographicCitations: ObjOrSeq[String] = new ObjOrSeq(Seq.empty),
            
            subjects: ObjOrSeq[String] = new ObjOrSeq(Seq.empty)): AnnotatedThing = {
    
    new DefaultAnnotatedThing(uri, title, realizationOf.option, identifier.option, description.option, homepage.option, sources.seq, primaryTopicOf,
                              temporal, creator, contributors, languages.seq, thumbnails.seq, depictions.seq,
                              bibliographicCitations.seq, subjects.seq)
  }
  
}
