package models

import java.util.Date

import scala.collection.mutable.ListBuffer

/**
  * ismet-scalongo-seed
  * Created by jeronimocarlos on 9/12/16.
  */
class LaNacionUserNews(val news: ListBuffer[News], val timestamp: Date) {

  def getNews: ListBuffer[News] ={
     news
  }
  def getTimestamp: Date ={
    timestamp
  }
}
