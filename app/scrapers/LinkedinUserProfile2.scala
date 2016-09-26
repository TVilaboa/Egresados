package scrapers

/*
  * Elaborado por Brian Re y Michele Re
 */

import java.util
import java.util.ArrayList
import java.util.Date
import java.util.List

class LinkedinUserProfile2 {

  var posicionActual: String = _
  var listaEmpleos: util.List[Empleo] = new util.ArrayList()
  var listaEstudio: util.List[Estudio] = new util.ArrayList()
  var url: String = _
  var timeStamp: Date = _

  def this(posicionActual: String, listaEmpleos: util.List[Empleo], listaEstudio: util.List[Estudio], url: String, timeStamp: Date) {
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
    listaEmpleos = new util.ArrayList()
    listaEstudio = new util.ArrayList()
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
