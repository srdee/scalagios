# Scalagios

Scalagios is a utility software library for working with data from the [PELAGIOS project](http://pelagios-project.blogspot.com) on
the Java Virtual Machine. Scalagios provides:

* a convenient programming API based on Pelagios' domain model primitives: _Datasets_, _AnnotatedThings_, _Annotations_, _Places_, etc.
* utilities to parse Pelagios data and gazetteer dump files into their domain model graph structure
* utilities to work with Pelagios "legacy data" (from Pelagios project phases 1 & 2)
* graph database I/O utilities based on [Tinkerpop Blueprints](http://tinkerpop.com/) 

## License

Scalagios is licensed under the [GNU General Public License v3.0](http://www.gnu.org/licenses/gpl.html).

## Developer Information

Scalagios is written in [Scala](http://www.scala-lang.org) and built with [SBT](http://www.scala-sbt.org/).

* To build the library, run `sbt package`.
* To run the unit tests, use `sbt test`
* To generate an Eclipse project, run `sbt eclipse`.
* To generate ScalaDoc, run `sbt doc`.  (Docs will be in `target/scala-2.10/api/`)

__Note:__ dependency download may take a while the first time you build the project!
