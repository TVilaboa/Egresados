package scrapers

/*
  * Elaborado por Brian Re y Michele Re
 */

import java.util.ArrayList
import java.util.Date
import java.util.List

class LinkedinUserProfile {

  var posicionActual: String = _
  var listaEmpleos: List[Empleo] = new ArrayList()
  var listaEstudio: List[Estudio] = new ArrayList()
  var url: String = _
  var timeStamp: Date = _

  def this(posicionActual: String,
           listaEmpleos: List[Empleo],
           listaEstudio: List[Estudio],
           url: String,
           timeStamp: Date) {
    this()
    this.posicionActual = posicionActual
    this.listaEmpleos = listaEmpleos
    this.listaEstudio = listaEstudio
    this.url = url
    this.timeStamp = timeStamp
  }

  def this(posicionActual: String, url: String, timeStamp: Date) {
    this()
    this.posicionActual = posicionActual
    this.url = url
    this.timeStamp = timeStamp
    listaEmpleos = new ArrayList()
    listaEstudio = new ArrayList()
  }

  def addEmpleo(empleo: Empleo) {
    listaEmpleos.add(empleo)
  }

  def addEstudio(estudio: Estudio) {
    listaEstudio.add(estudio)
  }

  def removeEmpleo(empleo: Empleo) {
    listaEmpleos.remove(empleo)
  }

  def removeEstudio(estudio: Estudio) {
    listaEstudio.remove(estudio)
  }
}
