package org.scalagios.graph.io

import com.tinkerpop.blueprints.pgm.impls.neo4j.Neo4jGraph
import org.scalagios.graph.io.readers.{Neo4jIndexReader, Neo4jNetworkQueryReader}

class PelagiosNeo4jReader(graph: Neo4jGraph) 
  extends PelagiosGraphReader(graph) with Neo4jIndexReader with Neo4jNetworkQueryReader {

}