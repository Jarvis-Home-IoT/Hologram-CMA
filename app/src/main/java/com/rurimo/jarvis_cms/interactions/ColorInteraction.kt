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

package com.rurimo.jarvis_cms.interactions

object ColorInteraction {
  fun getLedBytes(newColor: Int): ByteArray {
    val rgb = ByteArray(7)
    val color =
      java.lang.Long.parseLong(String.format("%06X", 0xFFFFFF and newColor), 16).toInt()
    rgb[0] = 0x56.toByte()
    rgb[1] = (color shr 16 and 0xFF).toByte()
    rgb[2] = (color shr 8 and 0xFF).toByte()
    rgb[3] = (color and 0xFF).toByte()
    rgb[4] = 0x00.toByte()
    rgb[5] = 0xf0.toByte()
    rgb[6] = 0xaa.toByte()
    return rgb
  }
}
