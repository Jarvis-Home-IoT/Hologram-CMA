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

package com.rurimo.jarvis_cms

import android.Manifest.permission.ACCESS_COARSE_LOCATION
import android.Manifest.permission.ACCESS_FINE_LOCATION
import android.Manifest.permission.BLUETOOTH
import android.Manifest.permission.BLUETOOTH_ADMIN
import android.Manifest.permission.RECORD_AUDIO
import android.annotation.SuppressLint
import android.content.BroadcastReceiver
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.ServiceConnection
import android.media.AudioManager
import android.os.Build
import android.os.Bundle
import android.os.IBinder
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import android.speech.tts.TextToSpeech
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import com.google.api.gax.core.FixedCredentialsProvider
import com.google.auth.oauth2.GoogleCredentials
import com.google.auth.oauth2.ServiceAccountCredentials
import com.google.cloud.dialogflow.v2beta1.DetectIntentRequest
import com.google.cloud.dialogflow.v2beta1.DetectIntentResponse
import com.google.cloud.dialogflow.v2beta1.QueryInput
import com.google.cloud.dialogflow.v2beta1.SessionName
import com.google.cloud.dialogflow.v2beta1.SessionsClient
import com.google.cloud.dialogflow.v2beta1.SessionsSettings
import com.google.cloud.dialogflow.v2beta1.TextInput
import com.google.firebase.database.ChildEventListener
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.rurimo.jarvis_data.Commands.checkColorControl
import com.rurimo.jarvis_data.Commands.commanderName
import com.rurimo.jarvis_data.Commands.deviceName
import com.rurimo.jarvis_data.Commands.getFireBaseMessage
import com.rurimo.jarvis_data.Commands.sendCommandMessage
import com.rurimo.jarvis_cms.interactions.BluetoothGattAttributes
import com.rurimo.jarvis_cms.interactions.BluetoothLeService
import com.rurimo.jarvis_cms.interactions.ColorInteraction
import com.rurimo.jarvis_data.ControlMessage
import com.rurimo.jarvis_data.sendMessage
import kotlinx.android.synthetic.main.activity_main.log
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.jetbrains.anko.toast
import java.util.Locale
import java.util.UUID

class MainActivity : AppCompatActivity(), TextToSpeech.OnInitListener {

  private val fireBaseDatabase = FirebaseDatabase.getInstance()
  private val databaseReference = fireBaseDatabase.reference

  private var recognizer = SpeechRecognizer.createSpeechRecognizer(this)
  private var sessionsClient: SessionsClient? = null
  private var session: SessionName? = null

  private val tts by lazy { TextToSpeech(this, this) }

  private var mConnected = false
  private var mBluetoothLeService: BluetoothLeService? = null

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setContentView(R.layout.activity_main)

    // request permission
    if (Build.VERSION.SDK_INT >= 23) {
      ActivityCompat.requestPermissions(this,
        arrayOf(RECORD_AUDIO, BLUETOOTH, BLUETOOTH_ADMIN, ACCESS_FINE_LOCATION,
          ACCESS_COARSE_LOCATION), 1000)
    }

    referenceFireBaseDatabase()
    initializeDialogFlow()
    initializeRecognizer()
    muteAllSounds()
    connectBluetoothService()
  }

  @SuppressLint("InlinedApi")
  private fun muteAllSounds() {
    val manager = getSystemService(AUDIO_SERVICE) as AudioManager
    manager.adjustStreamVolume(AudioManager.STREAM_MUSIC, AudioManager.ADJUST_MUTE, 0)
  }

  private fun initializeRecognizer() {
    val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
      putExtra(RecognizerIntent.EXTRA_CALLING_PACKAGE, packageName)
      putExtra(RecognizerIntent.EXTRA_LANGUAGE, "ko-KR")
    }
    this.recognizer = SpeechRecognizer.createSpeechRecognizer(this).apply {
      setRecognitionListener(recognitionListener)
      startListening(intent)
    }
  }

  override fun onInit(status: Int) {
    if (status == TextToSpeech.SUCCESS) {
      this.tts.language = Locale.KOREAN
    }
  }

  private fun restartRecognizer() {
    this.recognizer.cancel()
    this.recognizer.destroy()
    initializeRecognizer()
  }

  private val recognitionListener = object : RecognitionListener {
    override fun onReadyForSpeech(p0: Bundle?) = Unit
    override fun onRmsChanged(p0: Float) = Unit
    override fun onBufferReceived(p0: ByteArray?) = Unit
    override fun onPartialResults(p0: Bundle?) = Unit
    override fun onEvent(p0: Int, p1: Bundle?) = Unit
    override fun onBeginningOfSpeech() = Unit
    override fun onEndOfSpeech() = Unit
    override fun onError(error: Int) {
      Log.e("Test", "error code : $error")
      if (error >= 5) {
        restartRecognizer()
      }
    }

    override fun onResults(results: Bundle?) {
      val matches = results?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
      if (!matches.isNullOrEmpty()) {
        val result = matches[0]
        toast(result)
        if (result.contains(commanderName) and result.isNotEmpty()) {
          val command = result.replace(commanderName, "").trim()
          if (getFireBaseMessage(command) == command.trim()) {
            toast("query : $result")
            queryServer(command)
          } else {
            sendCommandMessage(databaseReference, command)

            // control smart bulb led color
            checkColorControl(command) {
              controlLed(ColorInteraction.getLedBytes(it))
            }
          }
        }
      }
      restartRecognizer()
    }
  }

  private fun connectBluetoothService() {
    // connection ble service
    val gattServiceIntent = Intent(this, BluetoothLeService::class.java)
    bindService(gattServiceIntent, mServiceConnection, Context.BIND_AUTO_CREATE)
  }

  private val mServiceConnection = object : ServiceConnection {
    override fun onServiceConnected(componentName: ComponentName, service: IBinder) {
      mBluetoothLeService = (service as BluetoothLeService.LocalBinder).service
      mBluetoothLeService?.let {
        if (!it.initialize()) {
          Toast.makeText(baseContext, R.string.ble_not_find, Toast.LENGTH_SHORT).show()
        } else {
          Toast.makeText(baseContext, "connected success!! to the smart led bulb",
            Toast.LENGTH_SHORT).show()
        }
        it.connect(smartBulbAddress)
      }
    }

    override fun onServiceDisconnected(componentName: ComponentName) {
      mBluetoothLeService = null
    }
  }

  private fun makeGattUpdateIntentFilter(): IntentFilter {
    val intentFilter = IntentFilter()
    intentFilter.addAction(BluetoothLeService.ACTION_GATT_SERVICES_DISCOVERED)
    intentFilter.addAction(BluetoothLeService.ACTION_GATT_CONNECTED)
    intentFilter.addAction(BluetoothLeService.ACTION_GATT_DISCONNECTED)
    return intentFilter
  }

  private val mGattUpdateReceiver = object : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
      when (intent.action) {
        BluetoothLeService.ACTION_GATT_CONNECTED -> {
          mConnected = true
          Toast.makeText(baseContext, R.string.ble_connect_success, Toast.LENGTH_SHORT).show()
        }
        BluetoothLeService.ACTION_GATT_DISCONNECTED -> {
          mConnected = false
          Toast.makeText(baseContext, R.string.ble_disconnected, Toast.LENGTH_SHORT).show()
        }
        BluetoothLeService.ACTION_GATT_SERVICES_DISCOVERED -> Unit
      }
    }
  }

  private fun speech(text: String) {
    this.tts.apply {
      setPitch(0.7f)
      setSpeechRate(1.2f)
      speak(text, TextToSpeech.QUEUE_FLUSH, null, null)
    }
  }

  private fun controlLed(rgb: ByteArray): Boolean {
    // get bluetoothGattCharacteristic
    val characteristic =
      mBluetoothLeService?.getGattCharacteristic(BluetoothGattAttributes.LED_CHARACTERISTIC)
    if (characteristic != null) {
      // check connection
      if (!mConnected) {
        Toast.makeText(this, R.string.ble_not_connected, Toast.LENGTH_SHORT).show()
        return false
      }

      // send characteristic data
      mBluetoothLeService?.sendDataCharacteristic(characteristic, rgb)
      return true
    }
    return false
  }

  private fun referenceFireBaseDatabase() {
    this.databaseReference.child("Message").addChildEventListener(object : ChildEventListener {
      override fun onChildChanged(dataSnapshot: DataSnapshot, s: String?) = Unit
      override fun onChildRemoved(dataSnapshot: DataSnapshot) = Unit
      override fun onChildMoved(dataSnapshot: DataSnapshot, s: String?) = Unit
      override fun onCancelled(databaseError: DatabaseError) = Unit
      override fun onChildAdded(dataSnapshot: DataSnapshot, s: String?) {
        val controlMessage = dataSnapshot.getValue(ControlMessage::class.java)
        controlMessage?.let {
          log.append("$it\n")
          val deviceName = it.deviceName
          // clear the message child on database - we should implement it on cms.
//          if (deviceName!!.contains("deviceName")) {
//            databaseReference.child("Message").removeValue()
//          }
        }
      }
    })
  }

  private fun initializeDialogFlow() {
    try {
      val stream = resources.openRawResource(R.raw.test_agent_credentials)
      val credentials = GoogleCredentials.fromStream(stream)
      val projectId = (credentials as ServiceAccountCredentials).projectId
      val settingsBuilder = SessionsSettings.newBuilder()
      val sessionsSettings =
        settingsBuilder.setCredentialsProvider(FixedCredentialsProvider.create(credentials)).build()
      this.sessionsClient = SessionsClient.create(sessionsSettings)
      this.session = SessionName.of(projectId, UUID.randomUUID().toString())
    } catch (e: Exception) {
      toast(e.toString())
      e.printStackTrace()
    }
  }

  private fun queryServer(message: String) {
    if (message.trim { it <= ' ' }.isEmpty()) {
      Toast.makeText(this@MainActivity, "Please fill your query!", Toast.LENGTH_LONG).show()
    } else {
      CoroutineScope(Dispatchers.IO).launch {
        val response = requestQuery(message)
        withContext(Dispatchers.Main) {
          // response to FireBase server
          response?.let {
            val result = callback(it)
            if (result.isNotEmpty()) {
              databaseReference.sendMessage(deviceName, result)
            } else {
              databaseReference.sendMessage(deviceName, message)
            }
            toast(result)
          } ?: toast("failed to connect dialog flow server!")
          restartRecognizer()
        }
      }
    }
  }

  private suspend fun requestQuery(message: String) = withContext(Dispatchers.IO) {
    val queryInput = QueryInput.newBuilder()
      .setText(TextInput.newBuilder().setText(message).setLanguageCode("en-US")).build()

    val detectIntentRequest = DetectIntentRequest.newBuilder()
      .setSession(session.toString())
      .setQueryInput(queryInput)
      .build()

    sessionsClient?.detectIntent(detectIntentRequest)
  }

  private fun callback(response: DetectIntentResponse): String {
    return response.queryResult.fulfillmentText
  }

  override fun onResume() {
    super.onResume()
    registerReceiver(mGattUpdateReceiver, makeGattUpdateIntentFilter())
  }

  override fun onPause() {
    super.onPause()
    unregisterReceiver(mGattUpdateReceiver)
  }

  override fun onDestroy() {
    with(this.recognizer) {
      cancel()
      stopListening()
      destroy()
    }.also {
      super.onDestroy()
      if (mConnected) {
        unbindService(mServiceConnection)
        mBluetoothLeService = null
      }
      this.tts.stop()
      this.tts.shutdown()
    }
  }
}
