package org.pelagios.rdf.parser

import java.util.Date
import org.pelagios.api._
import org.pelagios.rdf.vocab._
import org.openrdf.model.vocabulary.RDFS
import org.openrdf.model.URI
import org.openrdf.model.vocabulary.RDF
import org.openrdf.model.Literal
import org.openrdf.model.BNode
import java.text.SimpleDateFormat

/** An implementation of [[org.pelagios.rdf.parser.ResourceCollector]] to handle Pelagios data dump files.
  * 
  * @author Rainer Simon <rainer.simon@ait.ac.at>
  */
class PelagiosDataParser extends ResourceCollector {   
  
  def data: Iterable[AnnotatedThing] = {
    val allAnnotations = resourcesOfType(OA.Annotation).map(new AnnotationResource(_))  
    
    // Resolve foaf:Agents
    val agents = resourcesOfType(FOAF.Organization).map(new AgentResource(_)).map(agent => (agent.uri, agent)).toMap
    println(agents)
    
    // Resolve transcriptions
    val allTranscriptions = resourcesOfType(Pelagios.Transcription, Seq(_.hasAnyType(Seq(Pelagios.Toponym, Pelagios.Metonym, Pelagios.Ethnonym))))
    allAnnotations.foreach(annotation => {
      // Transcriptions per Annotation
      val rdfTranscriptions = annotation.resource.get(OA.hasBody).filter(_.isInstanceOf[BNode])
                             .map(bnode => allTranscriptions.find(_.uri.equals(bnode.stringValue)))
                             .filter(_.isDefined).map(_.get)
        
      
      if (rdfTranscriptions.size > 0) {
        // We only allow one transcription per annotation - so we'll discard additional ones, if any
        val transcription = rdfTranscriptions(0)
        val chars = transcription.getFirst(Content.chars).map(_.stringValue).getOrElse("[NONE]")
        val transcriptionType = transcription.getFirst(RDF.TYPE).map(uri => { 
            uri match {
              case Pelagios.Metonym => TranscriptionType.Metonym
              case Pelagios.Ethnonym => TranscriptionType.Ethnonym
              case _ => TranscriptionType.Toponym
            }
          }).getOrElse(TranscriptionType.Toponym)
        annotation.transcription = Some(Transcription(chars, transcriptionType))
      }
    })
    
    // Construct Work/Expression hierarchy
    val allAnnotatedThings = resourcesOfType(Pelagios.AnnotatedThing).map(new AnnotatedThingResource(_))
    allAnnotatedThings.foreach(thing => {
      val realizationOf = thing.resource.getFirst(FRBR.realizationOf).map(_.stringValue)
      if (realizationOf.isDefined) {        
        val work = allAnnotatedThings.find(t => { t.uri.equals(realizationOf.get) })
        thing.realizationOf = work
        if (work.isDefined) {
          work.get.expressions = thing +: work.get.expressions
        }
      }
    })  
    
    // Link annotations and annotated things
    val annotationsPerThing = allAnnotations.groupBy(_.resource.getFirst(OA.hasTarget).map(_.stringValue).getOrElse("_:empty"))
    allAnnotatedThings.foreach(thing => {
      val annotations = annotationsPerThing.get(thing.uri).getOrElse(Seq.empty[AnnotationResource])
      annotations.foreach(_.hasTarget = thing)
      thing.annotations = annotations.toSeq.sortWith((a, b) => { // Sort by index number if any
        if (a.index.isDefined && b.index.isDefined)
          a.index.get < b.index.get
        else if (a.index.isDefined)
          true
        else if (b.index.isDefined)
          false
        else
          false
      })
    })
    
    // Link annotations and authors
    allAnnotatedThings.foreach(thing => {
      thing.annotations.foreach(annotation => {
        val agentURI = annotation.resource.getFirst(OA.annotatedBy)
        println(" ### " + agentURI)
        annotation.creator = agentURI.map(uri => agents.get(uri.stringValue)).flatten
      })
    })
    
    // Filter out top-level things, i.e. those that are not expressions of something else
    allAnnotatedThings.filter(thing => thing.realizationOf.isEmpty)
  }
      
}

private[parser] class AgentResource(val resource: Resource) extends Agent {
  
  val uri = resource.uri
  
  val name: Option[String] = resource.getFirst(FOAF.name).map(_.stringValue)
  
}

/** Wraps an oa:Annotation RDF resource as an Annotation domain model primitive.
  *  
  * @constructor create a new AnnotationResource
  * @param resource the RDF resource to wrap
  */
private[parser] class AnnotationResource(val resource: Resource) extends Annotation {
  
  private val DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd")
  
  val uri = resource.uri
  
  var hasTarget: AnnotationTarget = null // resource.getFirst(OA.hasTarget).map(_.stringValue).getOrElse("_:empty")
  
  val place = resource.get(OA.hasBody).filter(_.isInstanceOf[URI]).map(_.stringValue)
  
  var transcription: Option[Transcription] = None
  
  var tags: Seq[Tag] = Seq.empty[Tag]
  
  val relation: Option[Relation.Type] = 
    resource.getFirst(Pelagios.relation).map(uri => Relation.withName(uri.stringValue))
    
  // TODO 
  def annotatedBy: Option[Agent] = None

  def annotatedAt: Option[Date] = resource.getFirst(OA.annotatedAt)
    .map(literal => DATE_FORMAT.parse(literal.stringValue))
    
  def serializedBy: Option[Agent] = None
  
  var creator: Option[Agent] = None
  
  // TODO
  def created: Option[Date] = None
  
  def index: Option[Int] = resource.getFirst(PelagiosSequence.index).map(_.stringValue.toInt)
  
  // TODO
  def distanceToNext: Option[Distance] = None
  
}

/** Wraps a pelagios:AnnotatedThing RDF resource as an AnnotatedThing domain model primitive, with
 *  Annotations in-lined.
  *  
  * @constructor create a new AnnotatedThing
  * @param resource the RDF resource to wrap
  */
private[parser] class AnnotatedThingResource(val resource: Resource) extends AnnotatedThing {
    
  val uri = resource.uri
  
  lazy val title = resource.getFirst(DCTerms.title).map(_.stringValue).getOrElse("[NO TITLE]") // 'NO TITLE' should never happen!
  
  var realizationOf: Option[AnnotatedThing] = None
  
  lazy val identifier = resource.getFirst(DCTerms.identifier).map(_.stringValue)

  lazy val description = resource.getFirst(DCTerms.description).map(_.stringValue)
  
  def homepage = resource.getFirst(FOAF.homepage).map(_.stringValue)
  
  def sources = resource.get(DCTerms.source).map(_.stringValue)
  
  def primaryTopicOf = resource.get(FOAF.primaryTopicOf).map(_.stringValue)
  
  def temporal: Option[PeriodOfTime] = resource.getFirst(DCTerms.temporal).map(literal => PeriodOfTime.fromString(literal.stringValue))

  var creator: Option[Agent] = None

  // TODO    
  def contributors: Seq[Agent] = Seq.empty[Agent]
  
  def languages = resource.get(DCTerms.language).map(_.stringValue)
  
  def thumbnails = resource.get(FOAF.thumbnail).map(_.stringValue)
  
  def depictions = resource.get(FOAF.depiction).map(_.stringValue)
  
  def bibliographicCitations = resource.get(DCTerms.bibliographicCitation).map(_.stringValue)
  
  def subjects = resource.get(DCTerms.subject).map(_.stringValue)
  
  var annotations = Seq.empty[AnnotationResource]
  
  var expressions = Seq.empty[AnnotatedThingResource]
  
}



