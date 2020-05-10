/*
 * Designed and developed by 2020 rurimo
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.rurimo.jarvis_data

import com.google.firebase.database.DatabaseReference

object Commands {
  val deviceName = "JarvisMobile"
  const val commanderName = "엘사"
  private const val on = "일어나"
  private const val off = "들어가"
  private const val onLights = "불켜"
  private const val onLights2 = "불 켜"
  private const val offLights = "불꺼"
  private const val offLights2 = "불 꺼"
  private const val dancing = "춤춰"
  private const val dancing2 = "춤 춰"
  private const val sunny = "맑음"
  private const val rainy = "비"
  private const val cloudy = "구름"

  private const val bulb = "전구"
  private const val bulb2 = "전 구"
  private const val bulb3 = "정구"
  private const val red = "빨강"
  private const val red2 = "빨간"
  private const val orange = "주황"
  private const val yellow = "노랑"
  private const val yellow2 = "노란"
  private const val green = "초록"
  private const val blue = "파랑"
  private const val blue2 = "파란"
  private const val purple = "보라"

  fun getFireBaseMessage(message: String): String {
    return when {
      message.contains(on) -> "turn on"
      message.contains(off) -> "turn off"
      message.contains(onLights) or message.contains(
        onLights2
      ) -> "lights on"
      message.contains(offLights) or message.contains(
        offLights2
      ) -> "lights off"
      message.contains(dancing) or message.contains(
        dancing2
      ) -> "dancing"
      message.contains(bulb) or message.contains(
        bulb2
      ) or message.contains(bulb3) -> "bulb"
      message.contains(sunny) -> "sunny"
      message.contains(rainy) -> "rainy"
      message.contains(cloudy) -> "cloudy"
      else -> message
    }
  }

  fun checkColorControl(message: String, block: (Int) -> Unit) {
    if (message.contains(bulb) or message.contains(
        bulb2
      ) or message.contains(bulb3)) {
      var color = -469762303
      when {
        message.contains(red) or message.contains(
          red2
        ) -> color = -620820478
        message.contains(orange) -> color = -37886
        message.contains(yellow) or message.contains(
          yellow2
        ) -> color = -469762303
        message.contains(green) -> color = 872470553
        message.contains(blue) or message.contains(
          blue2
        ) -> color = -16251137
        message.contains(purple) -> color = -64826
      }
      block(color)
    }
  }

  fun sendCommandMessage(reference: DatabaseReference, message: String) {
    reference.sendMessage(
      deviceName,
      getFireBaseMessage(message)
    )
    when {
      message.contains(onLights) or message.contains(
        onLights2
      ) -> reference.ledTurnOn()
      message.contains(offLights) or message.contains(
        offLights2
      ) -> reference.ledTurnOff()
    }
  }
}
