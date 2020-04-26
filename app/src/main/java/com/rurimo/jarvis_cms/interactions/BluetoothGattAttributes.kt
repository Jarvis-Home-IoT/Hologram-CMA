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

import java.util.HashMap

object BluetoothGattAttributes {
  private val attributes = HashMap<String, String>()

  // services
  private const val LED_SERVICE = "0000ffe5-0000-1000-8000-00805f9b34fb"

  // services => characteristics
  const val LED_CHARACTERISTIC = "0000ffe9-0000-1000-8000-00805f9b34fb"

  init {
    // services
    attributes[LED_SERVICE] = "Led Service"

    // characteristics
    attributes[LED_CHARACTERISTIC] = "Led Characteristic"
  }

  fun lookup(uuid: String, defaultName: String): String {
    val name = attributes[uuid]
    return name ?: defaultName
  }
}
