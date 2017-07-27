package scrapers

import play.api.libs.json.Json

object TestScrapers {

  def main(args: Array[String]): Unit = {
    testInfobae()
    println("---------------------------------------\n")
    testClarin()
    println("---------------------------------------\n")
    testCronista()
    println("---------------------------------------\n")
    testNacion()
  }

  private def testInfobae() : Unit = {
    val infobaeLinks : List[String] =  List("http://www.infobae.com/america/america-latina/2017/04/24/paso-a-paso-como-fue-el-meticuloso-plan-para-desvalijar-la-sede-de-prosegur-en-ciudad-del-este/",
      "http://www.infobae.com/america/america-latina/2017/04/24/como-era-el-impresionante-arsenal-utilizado-por-la-banda-que-ataco-una-empresa-de-caudales-en-paraguay/",
      "http://www.infobae.com/america/america-latina/2017/04/24/como-era-el-impresionante-arsenal-utilizado-por-la-banda-que-ataco-una-empresa-de-caudales-en-paraguay/",
      "http://www.infobae.com/politica/2017/04/24/la-familia-macri-vende-su-parte-de-autopistas-del-sol-para-evitar-conflictos-politicos/")

    val infobaeScraper : InfobaeScraper = new InfobaeScraper()
    infobaeLinks.foreach{x =>
      val news = infobaeScraper.getArticleData(x,Option("Mauricio Macri"),0)
      if(news.isDefined)
        println(Json.toJson(news.get).toString())
    }

  }

  private def testClarin() : Unit = {
    val clarinLinks : List[String] =  List("https://www.clarin.com/politica/ruta-dinero-pidieron-indagatoria-cristina_0_Hkxl-io0x.html",
      "https://www.clarin.com/politica/gobierno-alicia-kirchner-presento-denuncia-penal-danos-atentado-orden-constitucional-sedicion-violacion-domicilio_0_BJZpsvjRl.html",
      "https://www.clarin.com/policiales/ataque-comando-pueblo-brasil-20-ladrones-armas-guerra-robo-millonario_0_HyII9jsAg.html")

    val clarinScraper : ClarinScraper = new ClarinScraper()
    clarinLinks.foreach{x =>
      val news = clarinScraper.getArticleData(x,Option("Emmanuel Macron"),0)
      if(news.isDefined)
        println(Json.toJson(news.get).toString())
    }
  }

  private def testCronista() : Unit = {
    val cronistaLinks : List[String] =  List("http://www.cronista.com/economiapolitica/Efecto-Macron-Wall-Street-crece-en-linea-con-las-bolsas-del-mundo--20170424-0060.html",
      "http://www.cronista.com/economiapolitica/Asalto-con-explosivos-a-sede-de-Prosegur-en-Paraguay-para-robar-us-40-millones-20170424-0067.html",
      "http://www.cronista.com/cartelera/Como-es-Planetario-Fest-el-festival-al-aire-libre-para-los-chicos-en-Pilar-20170418-0097.html",
      "http://www.cronista.com/columnistas/El-Gobierno-jugara-a-la-politica-mientras-espera-que-menor-inflacion-y-mas-consumo-sean-noticia-en-mayo-20170424-0057.html")

    val elCronistaScraper : ElCronistaScraper = new ElCronistaScraper()
    cronistaLinks.foreach{x =>
      val news = elCronistaScraper.getArticleData(x,Option("Alicia Kirchner"),0)
      if(news.isDefined)
        println(Json.toJson(news.get).toString())
    }
  }

  private def testNacion() : Unit = {
    val laNacionLinks : List[String] =  List("http://www.lanacion.com.ar/2016294-el-gobierno-de-alicia-kirchner-denuncio-a-los-manifestantes-por-los-incidentes-frente-a-la-residencia-oficial",
      "http://www.lanacion.com.ar/2016308-lanzan-ba-taxi-la-aplicacion-para-viajar-en-taxi-en-buenos-aires",
      "http://www.lanacion.com.ar/2016301-el-fiscal-campagnoli-pidio-la-detencion-del-suspendido-jefe-de-la-policia-de-la-ciudad")

    val laNacionScraper : LaNacionScraper = new LaNacionScraper()
    laNacionLinks.foreach{x =>
      val news = laNacionScraper.getArticleData(x,Option("Ricardo Farias"),0)
      if(news.isDefined)
        println(Json.toJson(news.get).toString())
    }


  }

  private def testLinkedIn() : Unit = {
    val linkedInScraper : LinkedinUserProfileScraper= new LinkedinUserProfileScraper()
    val linkedInLinks : List[String] = List("https://ar.linkedin.com/in/ignacio-cassol-894a0935",
      "https://ar.linkedin.com/in/javier-isoldi-5a937091?trk=pub-pbmap",
      "https://ar.linkedin.com/in/andres-scoccimarro-303412",
      "https://ar.linkedin.com/in/santiagofuentes?trk=pub-pbmap",
      "https://ar.linkedin.com/in/kevstessens?trk=pub-pbmap",
      "https://ar.linkedin.com/in/ignacio-juan-nu%C3%B1ez-560183b7?trk=pub-pbmap",
      "https://ar.linkedin.com/in/joaqu%C3%ADn-bucca-04428278?trk=pub-pbmap",
      "https://ar.linkedin.com/in/sofiabraun?trk=pub-pbmap",
      "https://ar.linkedin.com/in/tmsmateus?trk=pub-pbmap")

    linkedInLinks.foreach{x=>
      val a = linkedInScraper.getLinkedinProfile(x,0)
      println(Json.toJson(a))
    }

    val scrap = linkedInScraper.getLinkedinProfile("https://ar.linkedin.com/in/ignacio-cassol-894a0935",0)
    println(Json.toJson(scrap))
  }

}


