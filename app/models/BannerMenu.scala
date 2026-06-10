/*
 * Copyright 2026 HM Revenue & Customs
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

package models

import play.api.libs.json.{Json, OFormat}

final case class MenuLink(
    id: String,
    text: String,
    href: String,
    external: Boolean = false
)

object MenuLink {
  implicit val format: OFormat[MenuLink] =
    Json.format[MenuLink]
}

final case class MenuDropdown(
    id: String,
    text: String,
    items: Seq[MenuLink]
)

object MenuDropdown {
  implicit val format: OFormat[MenuDropdown] =
    Json.format[MenuDropdown]
}

final case class BannerMenu(
    brand: MenuLink,
    topLevelLinks: Seq[MenuLink],
    dropdowns: Seq[MenuDropdown]
)

object BannerMenu {
  implicit val format: OFormat[BannerMenu] =
    Json.format[BannerMenu]
}
