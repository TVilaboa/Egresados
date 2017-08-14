package enums

import play.api.data.Forms._
import play.api.data._
import play.api.data.format.{Formats, Formatter}
import play.api.libs.json._

object EnumPlayUtils {
  //------------------------------------------------------------------------
  // public
  //------------------------------------------------------------------------
  /**
    * Constructs a simple mapping for a text field (mapped as `scala.Enumeration`)
    *
    * For example:
    * {{{
    * Form("gender" -> enum(Gender))
    * }}}
    *
    * @param enum the enumeration
    */
  def enum[E <: Enumeration](enum: E): Mapping[E#Value] = of(enumFormFormat(enum))

  /**
    * Default formatter for `scala.Enumeration`
    *
    */
  def enumFormFormat[E <: Enumeration](enum: E): Formatter[E#Value] = new Formatter[E#Value] {
    def bind(key: String, data: Map[String, String]) = {
      Formats.stringFormat.bind(key, data).right.flatMap { s =>
        scala.util.control.Exception.allCatch[E#Value]
          .either(enum.withName(s))
          .left.map(e => Seq(FormError(key, "error.enum", Nil)))
      }
    }

    def unbind(key: String, value: E#Value) = Map(key -> value.toString)
  }

  def enumReads[E <: Enumeration](enum: E): Reads[E#Value] =
    new Reads[E#Value] {
      def reads(json: JsValue): JsResult[E#Value] = json match {
        case JsString(s) => {
          try {
            JsSuccess(enum.withName(s))
          } catch {
            case _: NoSuchElementException =>
              JsError(s"Enumeration expected of type: '${enum.getClass}', but it does not appear to contain the value: '$s'")
          }
        }
        case _ => JsError("String value expected")
      }
    }

  implicit def enumWrites[E <: Enumeration]: Writes[E#Value] =
    new Writes[E#Value] {
      def writes(v: E#Value): JsValue = JsString(v.toString)
    }

  implicit def enumFormat[E <: Enumeration](enum: E): Format[E#Value] = {
    Format(enumReads(enum), enumWrites)
  }
}
