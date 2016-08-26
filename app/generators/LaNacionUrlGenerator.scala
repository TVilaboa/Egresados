package generators

import java.lang.Character

class LaNacionUrlGenerator {

    private var isTorRunning: Boolean = false

    def searchLaNacionUrl(searchName: String, startup: String, role: String): String = {
        if(searchName != null && isTorRunning) {
          var nameSplit = searchName.split(" ")
          var searcher = "laNacion"

          for(i <- 1  until (nameSplit.length-1)) {
              nameSplit(i) = nameSplit(i).filter("abcdefghijklmnopqrstvwxyz".contains(_))
          }
        }
        "asfag"
    }

}
