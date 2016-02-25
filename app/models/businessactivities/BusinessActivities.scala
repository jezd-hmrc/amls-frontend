package models.businessactivities

case class BusinessActivities(
                               businessFranchise: Option[BusinessFranchise] = None,
                               businessFranchise1: Option[BusinessFranchise] = None
                               ) {
  def businessFranchise(p: BusinessFranchise): BusinessActivities =
    this.copy(businessFranchise = Some(p))
}

object BusinessActivities {

  import play.api.libs.functional.syntax._
  import play.api.libs.json._

  val key = "business-activities"

  implicit val reads: Reads[BusinessActivities] = (
    __.read[Option[BusinessFranchise]] and
    __.read[Option[BusinessFranchise]]
    )(BusinessActivities.apply _)

  implicit val writes: Writes[BusinessActivities] = Writes[BusinessActivities] {
  model =>
    Seq(
      Json.toJson(model.businessFranchise).asOpt[JsObject],
      Json.toJson(model.businessFranchise).asOpt[JsObject]
    ).flatten.fold(Json.obj()){
     _ ++ _
    }
  }

  implicit def default(businessActivities: Option[BusinessActivities]): BusinessActivities =
    businessActivities.getOrElse(BusinessActivities())
}