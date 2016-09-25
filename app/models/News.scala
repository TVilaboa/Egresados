package models

/**
  * Default (Template) Project
  * Created by jeronimocarlos on 9/12/16.
  */
class News(val url: String, val title: String, val date: String, val tuft:String, val author: String) {

  def getUrl: String ={
    url
  }
  def getTitle: String ={
    title
  }
  def getDate: String ={
    date
  }
  def getTuft: String ={
    tuft
  }
  def getAuthor: String ={
    author
  }

}
