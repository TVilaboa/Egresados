package scrapers

import generators.LaNacionUrlGenerator

import scala.collection.mutable.ListBuffer

/**
  * ismet-scalongo-seed
  * Created by jeronimocarlos on 9/5/16.
  */
class Main {

  def main(args: Array[String]) {
    val generator: LaNacionUrlGenerator = new LaNacionUrlGenerator()
    val links: ListBuffer[String] = generator.searchLaNacionUrl("lopez gabeiras")
    val scraper: LaNacionScraper = new LaNacionScraper()
    println("*********************************")
    for(link <- links) {
      println(link)
      println(scraper.getArticleData(link))
    }
    println("*********************************")

  }
}
