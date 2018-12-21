/*
 * Copyright 2018 HM Revenue & Customs
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package connectors

import connectors.cache.MongoCacheConnector
import play.api.Play
import play.api.libs.json.Format
import uk.gov.hmrc.http.cache.client.CacheMap
import uk.gov.hmrc.http.{HeaderCarrier}
import uk.gov.hmrc.play.frontend.auth.AuthContext

import scala.concurrent.Future

trait DataCacheConnector {

  def cacheConnector: MongoCacheConnector

  def fetch[T](cacheId: String)(implicit authContext: AuthContext, hc: HeaderCarrier, formats: Format[T]): Future[Option[T]] =
    cacheConnector.fetch(cacheId)

  def save[T](cacheId: String, data: T)(implicit authContext: AuthContext, hc: HeaderCarrier, format: Format[T]): Future[CacheMap] =
    cacheConnector.save(cacheId, data)

  def updateCacheEntity[T](
                            targetCache: Option[CacheMap],
                            cacheId: String,
                            data: T
                          )
                          (
                            implicit authContext: AuthContext,
                            hc: HeaderCarrier,
                            format: Format[T]
                          ): CacheMap =
    cacheConnector.updateCacheEntity(targetCache, cacheId, data)

  def fetchAll(implicit hc: HeaderCarrier, authContext: AuthContext): Future[Option[CacheMap]] =
    cacheConnector.fetchAll

  def remove(implicit hc: HeaderCarrier, ac: AuthContext): Future[Boolean] =
    cacheConnector.remove

  def update[T](cacheId: String)(f: Option[T] => T)(implicit ac: AuthContext, hc: HeaderCarrier, fmt: Format[T]): Future[Option[T]] =
    cacheConnector.update(cacheId)(f)

  def saveAll(cacheMap: Future[CacheMap])(implicit hc: HeaderCarrier, ac: AuthContext): Future[CacheMap] =
    cacheConnector.saveAll(cacheMap)
}

object DataCacheConnector extends DataCacheConnector {
  def cacheConnector: MongoCacheConnector = Play.current.injector.instanceOf[MongoCacheConnector]
}