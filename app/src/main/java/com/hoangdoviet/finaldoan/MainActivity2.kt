package com.hoangdoviet.finaldoan

import android.app.SearchManager
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.provider.AlarmClock
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import android.speech.tts.TextToSpeech
import android.speech.tts.Voice
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.FrameLayout
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.chaquo.python.Python
import com.chaquo.python.android.AndroidPlatform
import com.github.zagum.speechrecognitionview.adapters.RecognitionListenerAdapter
import com.google.ai.client.generativeai.GenerativeModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.hoangdoviet.finaldoan.adapter.MessageAdapter
import com.hoangdoviet.finaldoan.databinding.ActivityMain2Binding
import com.hoangdoviet.finaldoan.model.Event
import com.hoangdoviet.finaldoan.model.Message
import com.hoangdoviet.finaldoan.utils.Constants
import com.hoangdoviet.finaldoan.utils.DateScheduler
import com.hoangdoviet.finaldoan.utils.Time
import com.hoangdoviet.finaldoan.utils.showToast
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.async
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import org.json.JSONObject
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import org.jsoup.nodes.Element
import org.jsoup.select.Elements
import java.io.BufferedReader
import java.io.BufferedWriter
import java.io.File
import java.io.FileWriter
import java.io.IOException
import java.net.URL
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import java.util.UUID
import kotlin.coroutines.suspendCoroutine

class MainActivity2 : AppCompatActivity(), TextToSpeech.OnInitListener {
    lateinit var binding: ActivityMain2Binding
    lateinit var adapter: MessageAdapter
    lateinit var tts: TextToSpeech
    lateinit var speechRecognizer: SpeechRecognizer
    lateinit var titleArticle: String
    lateinit var hashMap: HashMap<Int, Triple<String, String, String>>
    var messagesList = mutableListOf<Message>()
    val generativeModel = GenerativeModel(
        // For text-only input, use the gemini-pro model
        modelName = "gemini-pro",
        apiKey = "AIzaSyB0uf8m7jwu9AjNBL1Ri6g437qJX5Q1e_s"
        // ENTER YOUR KEY
    )
    private val mAuth: FirebaseAuth by lazy { FirebaseAuth.getInstance() }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMain2Binding.inflate(layoutInflater)
        setContentView(binding.root)
        val toolbar: Toolbar = binding.toolbar
        setSupportActionBar(toolbar)
        //recyclerView()
        setup()
        binding.backActivity.setOnClickListener {
            this.onBackPressed()
        }
        //clickEvents()
        tts = TextToSpeech(this, this)
        if (!Python.isStarted()) {
            Python.start(AndroidPlatform(this))
        }
        CoroutineScope(Dispatchers.Main).launch {
            callGetArticle("https://vnexpress.net/")
        }
    }

    private fun setup() {
        // setup rcv
        adapter = MessageAdapter()
        binding.reyclerviewMessageList.adapter = adapter
        binding.reyclerviewMessageList.layoutManager = LinearLayoutManager(applicationContext)
        // layout
        binding.layoutChatbox.visibility = View.INVISIBLE
        //
        binding.buttonChatboxSend.setOnClickListener {
            tts.stop()
            val text: String = binding.edittextChatbox.text.toString()
            binding.edittextChatbox.setText("")
            sendMessage(text)
        }
        binding.btnListen.setOnClickListener {
            startRecognition()
        }
        binding.btnKeyBoard.setOnClickListener { SpeechToKeyboard() }
        binding.btnListesInChatbox.setOnClickListener {
            startRecognition()
            KeyboardToSpeech()
        }
        messagesList.add(
            Message(
                "Chào bạn, Tôi có thể giúp gì cho bạn!",
                Constants.RECEIVE_ID,
                System.currentTimeMillis().toString()
            )
        )
        //readCsvMessage()
        adapter.insertMessage(
            Message(
                "Chào bạn, Tôi có thể giúp gì cho bạn!",
                Constants.RECEIVE_ID,
                System.currentTimeMillis().toString()
            )
        )

        setUiRecognition()


    }

    private fun setUiRecognition() {
        // setup Speech Recognition
        speechRecognizer = SpeechRecognizer.createSpeechRecognizer(this)
        binding.recognitionView!!.setSpeechRecognizer(speechRecognizer)
        binding.recognitionView!!.setRecognitionListener(object : RecognitionListenerAdapter() {
            override fun onResults(results: Bundle) {
                finishRecognition()
                val matches = results.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                if (!matches.isNullOrEmpty()) {
                    val text = matches[0]
                    sendMessage(text)
                }
            }
        })


        binding.recognitionView!!.setOnClickListener {
            finishRecognition()
            speechRecognizer.stopListening()
        }
        val colors = intArrayOf(
            ContextCompat.getColor(this, R.color.color1),
            ContextCompat.getColor(this, R.color.color2),
            ContextCompat.getColor(this, R.color.color3),
            ContextCompat.getColor(this, R.color.color4),
            ContextCompat.getColor(this, R.color.color5)
        )
        val heights = intArrayOf(60, 76, 58, 80, 55)
        binding.recognitionView!!.setColors(colors)
        binding.recognitionView!!.setBarMaxHeightsInDp(heights)
        binding.recognitionView!!.setCircleRadiusInDp(6) // kich thuoc cham tron
        binding.recognitionView!!.setSpacingInDp(2) // khoang cach giua cac cham tron
        binding.recognitionView!!.setIdleStateAmplitudeInDp(8) // bien do dao dong cua cham tron
        binding.recognitionView!!.setRotationRadiusInDp(40) // kich thuoc vong quay cua cham tron
        binding.recognitionView!!.play()
    }

    private fun startRecognition() {
        tts.stop()
        binding.btnListen.setVisibility(View.GONE)
        binding.btnKeyBoard.setVisibility(View.GONE)
        binding.recognitionView!!.play()
        binding.recognitionView!!.visibility = View.VISIBLE
        val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH)
        intent.putExtra(RecognizerIntent.EXTRA_CALLING_PACKAGE, packageName)
        intent.putExtra(
            RecognizerIntent.EXTRA_LANGUAGE_MODEL,
            RecognizerIntent.LANGUAGE_MODEL_FREE_FORM
        )
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, "vi")
        speechRecognizer.startListening(intent)
    }

    private fun finishRecognition() {
        binding.btnListen.setVisibility(View.VISIBLE)
        binding.btnKeyBoard.setVisibility(View.VISIBLE)
        binding.recognitionView!!.stop()
        binding.recognitionView!!.play()
        binding.recognitionView!!.visibility = View.GONE
    }

    private fun KeyboardToSpeech() {
        binding.layoutChatbox.setVisibility(View.INVISIBLE)
        binding.layoutSpeech!!.visibility =
            View.VISIBLE // dấu !! kotlin => biến k thể NULL cho phép truy cập an toàn
        // tạo đối tượng params => dùng xđ kích thước layout_speech trong bố cục cha giả định là frameLayout
        val params = FrameLayout.LayoutParams(
            FrameLayout.LayoutParams.MATCH_PARENT, //Đặt chiều rộng của layout_speech để khớp với toàn bộ chiều rộng của bố cục cha của nó.
            FrameLayout.LayoutParams.WRAP_CONTENT //Đặt chiều cao của layout_speech để tự động điều chỉnh dựa trên nội dung của nó.
        )
        params.setMargins(0, 0, 0, 410)
        closeKeyboard()
    }

    private fun writeCsvMessage() {
        val folder = File(
            Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS),
            "SpeechApplication"
        )
        if (!folder.exists()) {
            folder.mkdirs()
        }
        val csv = File(folder, "message.csv")
        if (!csv.exists()) {
            try {
                csv.createNewFile()
            } catch (e: IOException) {
                e.printStackTrace()
            }
        }
        var data = ""
        for (m in messagesList) {
            data += (m.message + ";" + m.time + ";" + m.id
                    ) + "\n"
        }
        Log.d("writeCsvMessage: ", data)
        var fw: FileWriter? = null
        try {
            fw = FileWriter(csv.getAbsoluteFile())
            val bw = BufferedWriter(fw)
            bw.write(data)
            bw.close()
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }

    private fun readCsvMessage() {
        val folder = File(
            Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS),
            "SpeechApplication"
        ).absoluteFile

        if (folder.exists()) {
            val csv = File(folder, "message.csv")
            if (csv.exists()) {
                var br: BufferedReader? = null
                try {
                    br = csv.bufferedReader()
                    br.forEachLine { line ->
                        val ms = line.split(";").dropLastWhile { it.isEmpty() }
                        if (ms.size == 3) {
                            val message = ms[0]
                            val time = ms[1].toLongOrNull()
                            val isUser = ms[2]
                            if (message != "Chào bạn, Tôi có thể giúp gì cho bạn!" && time != null) {
                                Log.d("readCsvMessage", "$message $isUser $time")
                                messagesList.add(Message(message, isUser, time.toString()))
                            }
                        }
                    }
                    adapter.notifyDataSetChanged()
                } catch (e: IOException) {
                    e.printStackTrace()
                } finally {
                    try {
                        br?.close()
                    } catch (ex: IOException) {
                        ex.printStackTrace()
                    }
                }
            }
        }
    }


    private fun readCsvMessage1() {
        val folder = File(
            Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS),
            "SpeechApplication"
        ).absoluteFile

        if (folder.exists()) {
            val csv = File(folder, "message.csv")
            if (csv.exists()) {
                var br: BufferedReader? = null
                try {
                    br = csv.bufferedReader()
                    var line: String?
                    while (br.readLine().also { line = it } != null) {
                        val ms = line!!.split(";").dropLastWhile { it.isEmpty() }
                        if (ms.size == 3) {
                            val message = ms[0]
                            val time = ms[1].toLongOrNull()
                            val isUser = ms[2]
                            if (message != "Chào bạn, Tôi có thể giúp gì cho bạn!" && time != null) {
                                Log.d("readCsvMessage", "$message $isUser $time")
                                messagesList.add(Message(message, isUser, Time.timeStamp()))
                            }
                        }
                    }
                    adapter.notifyDataSetChanged()
                } catch (e: IOException) {
                    e.printStackTrace()
                } finally {
                    try {
                        br?.close()
                    } catch (ex: IOException) {
                        ex.printStackTrace()
                    }
                }
            }
        }
    }


    fun showKeyboard() {
        binding.edittextChatbox.requestFocus()
        val imm = getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager
        imm.showSoftInput(binding.edittextChatbox, InputMethodManager.SHOW_IMPLICIT)
    }

    fun closeKeyboard() {
        val imm = getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(binding.edittextChatbox.getWindowToken(), 0)
    }

    private fun SpeechToKeyboard() {
        showKeyboard()
        finishRecognition()

        binding.layoutChatbox.setVisibility(View.VISIBLE)
        binding.layoutSpeech.visibility = View.INVISIBLE
        val params = FrameLayout.LayoutParams(
            FrameLayout.LayoutParams.MATCH_PARENT,
            FrameLayout.LayoutParams.WRAP_CONTENT
        )
        params.setMargins(0, 0, 0, 140)
    }

    override fun onPause() {
        super.onPause()
        finishRecognition()
        speechRecognizer.stopListening()
        tts.stop()

    }

    override fun onDestroy() {
        super.onDestroy()
        speechRecognizer.destroy()
        tts.shutdown()
    }

    override fun onStart() {
        super.onStart()
        GlobalScope.launch {
            delay(100)
            withContext(Dispatchers.Main) {
                binding.reyclerviewMessageList.scrollToPosition(adapter.itemCount - 1)
            }
        }
    }

    override fun onInit(status: Int) {
        if (status == TextToSpeech.SUCCESS) {
            val result = tts.setLanguage(Locale("vi", "VN"))
            if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED) {
                Toast.makeText(this, "TTS Not Supported for Vietnamese", Toast.LENGTH_LONG).show()
            }
        }
    }

    private fun playNews(data: String) {
        tts.speak(data, TextToSpeech.QUEUE_FLUSH, null, "")
    }

    private fun sendMessage(text: String) {
        //val message = binding.etMessage.text.toString()
        val message = text
        val timeStamp = Time.timeStamp()

        if (message.isNotEmpty()) {
            //Adds it to our local list
            messagesList.add(Message(message, Constants.SEND_ID, timeStamp))
            binding.edittextChatbox.setText("")

            adapter.insertMessage(Message(message, Constants.SEND_ID, timeStamp))
            binding.reyclerviewMessageList.scrollToPosition(adapter.itemCount - 1)

            botResponse(message)
        }
    }

    private fun botResponse(message: String) {
        val timeStamp = Time.timeStamp()
        // Khai báo một CoroutineScope
        val scope = CoroutineScope(Dispatchers.Main)
        GlobalScope.launch {
            var responseText = ""
            var ArticleText = ""
            withContext(Dispatchers.Main) {

                when {
                    message.toLowerCase().contains("đọc báo") -> {
                        responseText = titleArticle

                    }

                    message.toLowerCase().contains("tin số 1")
                    -> {
                        var text = hashMap[1]?.third.toString() + "\n"
                        val link = hashMap[1]?.first.toString()
                        text += "Nhấn vào đường link để đọc chi tiết:\n $link"
                        responseText = text
                        ArticleText = hashMap[1]?.second.toString()
                    }

                    message.toLowerCase().contains("tin số 2") -> {
                        var text = hashMap[2]?.third.toString() + "\n"
                        val link = hashMap[2]?.first.toString()
                        text += "Nhấn vào đường link để đọc chi tiết:\n $link"
                        responseText = text
                        ArticleText = hashMap[2]?.second.toString()
                    }

                    message.toLowerCase().contains("tin số 3") -> {
                        var text = hashMap[3]?.third.toString() + "\n"
                        val link = hashMap[3]?.first.toString()
                        text += "Nhấn vào đường link để đọc chi tiết:\n $link"
                        responseText = text
                        ArticleText = hashMap[3]?.second.toString()
                    }

                    message.toLowerCase().contains("tin số 4") -> {
                        var text = hashMap[4]?.third.toString() + "\n"
                        val link = hashMap[4]?.first.toString()
                        text += "Nhấn vào đường link để đọc chi tiết:\n $link"
                        responseText = text
                        ArticleText = hashMap[4]?.second.toString()
                    }

                    message.toLowerCase().contains("tin số 5") -> {
                        var text = hashMap[5]?.third.toString() + "\n"
                        val link = hashMap[5]?.first.toString()
                        text += "Nhấn vào đường link để đọc chi tiết:\n $link"
                        responseText = text
                        ArticleText = hashMap[5]?.second.toString()
                    }

                    message.toLowerCase().contains("tin số 6") -> {
                        var text = hashMap[6]?.third.toString() + "\n"
                        val link = hashMap[6]?.first.toString()
                        text += "Nhấn vào đường link để đọc chi tiết:\n $link"
                        responseText = text
                        ArticleText = hashMap[6]?.second.toString()
                    }

                    message.toLowerCase().contains("tin số 7") -> {
                        var text = hashMap[7]?.third.toString() + "\n"
                        val link = hashMap[7]?.first.toString()
                        text += "Nhấn vào đường link để đọc chi tiết:\n $link"
                        responseText = text
                        ArticleText = hashMap[7]?.second.toString()
                    }

                    message.toLowerCase().contains("báo thức") -> {
                        responseText = "Đặt báo thức thành công"
                        scope.launch {
                            delay(1500)
                            alarm(message)
                        }
                    }

                    message.toLowerCase().contains("đếm ngược") -> {
                        responseText = "Đặt đếm ngược  thành công"
                        scope.launch {
                            delay(1500)
                            getTimeInSeconds(message)
                        }
                    }

                    message.toLowerCase().contains("gọi taxi") -> {
                        responseText = "Đang gọi taxi G7"
                        scope.launch {
                            delay(1500)
                            callTaxi()
                        }
                    }

                    message.toLowerCase().contains("tìm kiếm") -> {
                        responseText = "Tôi sẽ mở google"
                        val startIndex = message.indexOf("Tìm kiếm thông tin về")
                        if (startIndex != -1) {
                            val substring = message.substring(startIndex)
                            scope.launch {
                                delay(1500)
                                search_google(substring)
                            }
                        }

                    }

                    message.toLowerCase().contains("giá vàngxx") -> {
                        responseText =
                            getGiaVangFromURLAsync("https://ngoctham.com/bang-gia-vang/").await()
                    }

                    message.toLowerCase().contains("giá xăng") -> {
                        responseText =
                            getGiaXangFromUrlAsync("https://vnexpress.net/chu-de/gia-xang-dau-3026").await()
                    }

//                    message.toLowerCase().contains("xổ số") -> {
//                        responseText =
//                            getXoSoFromUrlAsync("https://api-xsmb.cyclic.app/api/v1").await()
//                    }
                    message.toLowerCase().contains("đặt lịch") && message.toLowerCase().contains("âm lịch")||
                            message.toLowerCase().contains("đặt lịch") && message.toLowerCase().contains("ngày rằm")-> {
                        val Date = DateScheduler.extractLunarDate(message)
                        val time = DateScheduler.findTimeReferences(message)
                        val content = DateScheduler.getStringAfterKeyword(message)
                        try {
                            responseText = time +"\n"+content+"\n"+Date
                            if(time.isNullOrEmpty()) responseText = "Bạn cần thêm thêm các từ chỉ thời gian để đặt lịch như 5 giờ sáng , 5 giờ chiều , hoặc mốc thời gian cụ thể như 10:45\n Ví dụ đặt lịch cho tôi ngày mai lúc 10 GIỜ SÁNG với nội dung đi học toán"
                            else if (content.isNullOrEmpty())   responseText = "Bạn cần thêm thêm từ nội dung để đặt lịch\n Ví dụ đặt lịch cho tôi ngày mai lúc 10 giờ sáng với NỘI DUNG đi học toán"
                            else if (Date.isNullOrEmpty()) responseText = "Không thể xác định các từ thời gian. Thử lại"
                            else if (Date.contains("Không")) responseText = "Không xác định được ngày âm lịch"
                            else{
                                val currentUserUid = mAuth.currentUser?.uid!!
                                val event = Event(
                                    eventID = generateEventId(),
                                    date = convertDateString(Date),
                                    title = content,
                                    timeStart = time,
                                    timeEnd =addOneHourToTime(time) ,
                                    repeat = 0
                                )
                                responseText = "Thêm sự kiện thành công\n $content vào ngày ${event.date}"
                                addSingleEvent(currentUserUid, event)

                            }
                        }catch (e: Exception){
                            responseText = "Lỗi "+e
                            Log.d("loii",e.toString())
                        }
                    }
                    message.toLowerCase().contains("đặt lịch") -> {
                        val time = DateScheduler.findTimeReferences(message)
                        val content = DateScheduler.getStringAfterKeyword(message)
                        val listWordDate = DateScheduler.extractTemporalWords(message)
                        val Date = DateScheduler.checktime(listWordDate)
                        try {
                            responseText = time +"\n"+content+"\n"+Date
                            if(time.isNullOrEmpty()) responseText = "Bạn cần thêm thêm các từ chỉ thời gian để đặt lịch như 5 giờ sáng , 5 giờ chiều , hoặc mốc thời gian cụ thể như 10:45\n Ví dụ đặt lịch cho tôi ngày mai lúc 10 GIỜ SÁNG với nội dung đi học toán"
                            else if (content.isNullOrEmpty())   responseText = "Bạn cần thêm thêm từ nội dung để đặt lịch\n Ví dụ đặt lịch cho tôi ngày mai lúc 10 giờ sáng với NỘI DUNG đi học toán"
                            else if(listWordDate.isEmpty())  responseText = "Bạn cần thêm thêm các từ chỉ ngày để xác định ngày đặt lịch ví dụ hôm nay , ngày mai, thứ hai tuần sau \n Ví dụ đặt lịch cho tôi NGÀY MAI lúc 10 giờ sáng với nội dung đi học toán"
                            else if (Date.isNullOrEmpty()) responseText = "Không thể xác định các từ thời gian. Thử lại"
                            else if (Date.contains("Lỗi vì ngày")) responseText = "Thời gian đặt lịch đã quá hạn, Vui lòng nói thời gian hợp lệ"
                            else{
                                val currentUserUid = mAuth.currentUser?.uid!!
                                val event = Event(
                                    eventID = generateEventId(),
                                    date = convertDateString(Date),
                                    title = content,
                                    timeStart = time,
                                    timeEnd =addOneHourToTime(time) ,
                                    repeat = 0
                                )
                                responseText = "Thêm sự kiện thành công\n $content vào ngày ${event.date}"
                                addSingleEvent(currentUserUid, event)

                            }

                        }catch (e: Exception){
                            responseText = "Lỗi "+e
                        }
                    }
                    message.toLowerCase().contains("hôm nay") && message.toLowerCase().contains("sự kiện") -> {
                        val currentUserUid = mAuth.currentUser?.uid!!
                        val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
                        val todayDate = dateFormat.format(Date())

                        val events = withContext(Dispatchers.IO) {
                            getEventsByUserAndDate(currentUserUid, todayDate)
                        }

                        responseText = if (events.isNotEmpty()) {
                            events.joinToString(separator = "\n") { event ->
                                "${event.title}: ${event.timeStart} - ${event.timeEnd}"
                            }
                        } else {
                            "Hôm nay không có sự kiện"
                        }
                    }
                    message.toLowerCase().contains("kiểm tra") -> {
                        responseText = DateScheduler.extractLunarDate(message)
                    }
                    else -> {
                        val response = generativeModel.generateContent(message)
                        responseText = response?.text ?: "Không có câu trả lời"
                        responseText = responseText.replace("*", "")
                    }
                }
                messagesList.add(Message(responseText, Constants.RECEIVE_ID, timeStamp))
//                val index = messagesList.indexOf(loadingMessage)
//                Log.d("check", index.toString())
                //
                adapter.insertMessage(Message(responseText, Constants.RECEIVE_ID, timeStamp))
                binding.reyclerviewMessageList.scrollToPosition(adapter.itemCount - 1)
                if (ArticleText.isNotEmpty()) {
                    playNews(ArticleText)
                } else {
                    playNews(responseText)
                }
//Thread { writeCsvMessage() }.start()

            }
        }

    }
    suspend fun getEventsByUserAndDate(userId: String, date: String): List<Event> {
        return withContext(Dispatchers.IO) {
            try {
                val db = FirebaseFirestore.getInstance()
                val userRef = db.collection("User").document(userId)

                val userDocument = userRef.get().await()
                val eventIds = userDocument.get("eventID") as? List<String> ?: emptyList()

                val eventsRef = db.collection("Events")
                val eventsSnapshot = eventsRef.whereIn("eventID", eventIds)
                    .whereEqualTo("date", date)
                    .get()
                    .await()

                eventsSnapshot.toObjects(Event::class.java)
            } catch (e: Exception) {
                e.printStackTrace()
                emptyList()
            }
        }
    }
    fun convertDateString(inputDate: String): String {
        // Định dạng đầu vào
        val inputFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        // Định dạng đầu ra
        val outputFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())

        // Phân tích chuỗi đầu vào thành đối tượng Date
        val date = inputFormat.parse(inputDate)
        // Định dạng lại đối tượng Date thành chuỗi theo định dạng đầu ra
        return outputFormat.format(date)
    }
    fun addOneHourToTime(time: String): String {
        val dateFormat = SimpleDateFormat("HH:mm", Locale.getDefault())
        val date = dateFormat.parse(time)
        val calendar = Calendar.getInstance()
        calendar.time = date
        calendar.add(Calendar.HOUR_OF_DAY, 1)
        return dateFormat.format(calendar.time)
    }
    fun generateEventId(): String {
        return UUID.randomUUID().toString()
    }

    private suspend fun callGetArticle(url: String) {
        // Khởi tạo một coroutine scope
        titleArticle = "Hôm nay có các tin tức sau\n"
        // Bắt đầu một coroutine
        withContext(Dispatchers.IO) {
            // Gọi hàm getArticle trong một coroutine
            hashMap = getArticle(url)
        }

        // Xử lý kết quả trả về từ hàm getArticle trên luồng chính
        for ((key, value) in hashMap) {
            Log.d(
                "hashMap",
                "Khóa: $key, Giá trị: (${value.first}, ${value.second}, TIEU DE ${value.third})"
            )
            titleArticle += "Tin số $key ${value.third}\n"
        }
        titleArticle += "Bạn muốn đọc tin số mấy?"
        // Cập nhật giao diện hoặc thực hiện các tác vụ khác trên luồng chính nếu cần
        Log.d("hashMap", titleArticle)
    }

    private suspend fun getArticle(url: String): HashMap<Int, Triple<String, String, String>> {
        val hashMap = HashMap<Int, Triple<String, String, String>>()
        try {
            //python
            val py = Python.getInstance()
            val module = py.getModule("script")
            val num = module["num"]?.toInt()
            val face = module["meta_description"]
            val faceTitle = module["title"]

            val doc: Document = withContext(Dispatchers.IO) {
                Jsoup.connect(url).get()
            }

            val articles = doc.select("article").take(7)
            val uniqueLinks = mutableSetOf<String>() // Sử dụng một set để lưu trữ các href duy nhất
            for (article in articles) {
                val links = article.select("a[href]")
                for (link in links) {
                    val href = link.attr("href")
                    // Kiểm tra href kết thúc bằng ".html", không chứa từ khóa "tac-gia" và không chứa từ khóa "video"
                    if (href.endsWith(".html") && !href.contains("tac-gia") && !href.contains("video") && href !in uniqueLinks) {
                        uniqueLinks.add(href) // Nếu thỏa mãn điều kiện, thêm vào set
                        Log.d("href", "href: $href")
                    }
                }
            }
            uniqueLinks.forEachIndexed { index, link ->
                hashMap[index + 1] = Triple(
                    link.toString(),
                    face?.call(link).toString(),
                    faceTitle?.call(link).toString()
                )
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return hashMap
    }

    private fun getGiaVangFromURLAsync(s: String): Deferred<String> {
        return GlobalScope.async {
            try {
                var content = "Không tim thấy thông tin"
                val doc: Document = Jsoup.connect(s).get()
                val ngayElement: Element? = doc.select("p.note").first()
                val ngay: String = ngayElement?.text() ?: "Không có thông tin cập nhật"
                Log.d("NgayCapNhat", "$ngay")
                val table = doc.select("table.price-table").first()
                val rows = table?.select("tbody tr")
                if (rows != null) {
                    content = ""
                    for (row in rows) {
                        val type = row.select("td.type").text()
                        val buyPrice = row.select("td:eq(1)").text()
                        val sellPrice = row.select("td:eq(2)").text()
                        // In thông tin ra Logcat
                        Log.d(
                            "TrichXuat",
                            "Loại vàng: $type - Giá mua: $buyPrice - Giá bán: $sellPrice"
                        )
                        content += "$type - Giá mua: $buyPrice - Giá bán: $sellPrice\n"
                    }
                    content += "$ngay\n"
                    content += "Thông tin từ website Ngọc Thẩm "
                }
                return@async content
            } catch (e: Exception) {
                e.printStackTrace()
                return@async "Có lỗi xảy ra khi truy cập thông tin giá vàng."
            }
        }
    }

    private fun getGiaXangFromUrlAsync(url: String): Deferred<String> {
        return GlobalScope.async {
            try {
                var message = "Không có thông tin"
                val document: Document = Jsoup.connect(url).get()

                // Lấy tất cả các phần tử table từ trang web
                val tables: Elements = document.getElementsByTag("table")

                // Lặp qua từng bảng
                for (table in tables) {
                    // Lấy tất cả các hàng trong bảng
                    val rows: Elements = table.getElementsByTag("tr")

                    // Biến để lưu trữ thông điệp
                    message = ""

                    // Lặp qua từng hàng
                    for (row in rows) {
                        // Lấy tất cả các ô trong hàng
                        val cells: Elements = row.getElementsByTag("td")
                        val rowData = ArrayList<String>()

                        // Lặp qua từng ô trong hàng
                        for (cell in cells) {
                            // Lưu giá trị của ô vào danh sách dữ liệu của hàng
                            rowData.add(cell.text())
                        }

                        // Kiểm tra nếu hàng không rỗng và có đúng 3 ô
                        if (rowData.size == 3) {
                            val matHang = rowData[0]
                            val gia = rowData[1]
                            val soVoiKyTruoc = rowData[2]

                            if (matHang != "Mặt hàng") {
                                // Tạo thông điệp với dữ liệu từ hàng
                                message += "$matHang: $gia đồng/lít. (Tăng $soVoiKyTruoc đồng)\n"
                            }
                        }
                    }
                    message += "\nThông tin từ website vnexpress.net"
                    break // Chỉ xử lý bảng đầu tiên (bạn có thể xử lý tất cả các bảng nếu cần)
                }
                return@async message
            } catch (e: Exception) {
                e.printStackTrace()
                return@async "Có lỗi xảy ra khi truy cập thông tin giá xăng."
            }
        }
    }

    private fun getXoSoFromUrlAsync(url: String): Deferred<String> {
        return GlobalScope.async {
            try {
                var content = "Không có thông tin"
                // Gửi yêu cầu GET đến API và lấy dữ liệu JSON trả về
                val jsonResponse = URL(url).readText()

                // Phân tích dữ liệu JSON bằng JSONObject
                val jsonObject = JSONObject(jsonResponse)

                // Truy xuất các giá trị từ đối tượng JSON
                val time = jsonObject.getString("time")
                val gdb = jsonObject.getJSONObject("results").getJSONArray("ĐB").getString(0)
                val g1 = jsonObject.getJSONObject("results").getJSONArray("G1").getString(0)
                val g2Array = jsonObject.getJSONObject("results").getJSONArray("G2")
                val g3Array = jsonObject.getJSONObject("results").getJSONArray("G3")
                val g4Array = jsonObject.getJSONObject("results").getJSONArray("G4")
                val g5Array = jsonObject.getJSONObject("results").getJSONArray("G5")
                val g6Array = jsonObject.getJSONObject("results").getJSONArray("G6")
                val g7Array = jsonObject.getJSONObject("results").getJSONArray("G7")

                // Chuyển đổi các mảng JSONArray thành danh sách MutableList<String>
                val g2List = mutableListOf<String>()
                for (i in 0 until g2Array.length()) {
                    g2List.add(g2Array.getString(i))
                }

                val g3List = mutableListOf<String>()
                for (i in 0 until g3Array.length()) {
                    g3List.add(g3Array.getString(i))
                }

                val g4List = mutableListOf<String>()
                for (i in 0 until g4Array.length()) {
                    g4List.add(g4Array.getString(i))
                }

                val g5List = mutableListOf<String>()
                for (i in 0 until g5Array.length()) {
                    g5List.add(g5Array.getString(i))
                }

                val g6List = mutableListOf<String>()
                for (i in 0 until g6Array.length()) {
                    g6List.add(g6Array.getString(i))
                }

                val g7List = mutableListOf<String>()
                for (i in 0 until g7Array.length()) {
                    g7List.add(g7Array.getString(i))
                }
                content = ""
                content += "Thời gian: $time\nGiải đặc biệt: $gdb\nGiải nhất: $g1\nGiải nhì:  ${
                    g2List.joinToString(
                        separator = ", "
                    )
                }\n"
                // In ra các giá trị đã lấy được
                Log.d("checkkk", "Thời gian: $time")
                println("ĐB: $gdb")
                println("G1: $g1")
                Log.d("checkkk", "G2: ${g2List.joinToString(separator = ", ")}")
                println("G3: ${g3List.joinToString(separator = ", ")}")
                println("G4: ${g4List.joinToString(separator = ", ")}")
                println("G5: ${g5List.joinToString(separator = ", ")}")
                println("G6: ${g6List.joinToString(separator = ", ")}")
                println("G7: ${g7List.joinToString(separator = ", ")}")
                return@async content
            } catch (e: Exception) {
                e.printStackTrace()
                return@async "Có lỗi xảy ra khi truy cập thông tin XSMB."
            }
        }
    }
    fun addSingleEvent(userId: String, event: Event)  {
        val db = FirebaseFirestore.getInstance()
        val eventRef = db.collection("Events").document(event.eventID)

        eventRef.set(event)
            .addOnSuccessListener {
                // Thêm eventId vào danh sách eventID của người dùng
                db.collection("User").document(userId)
                    .update("eventID", FieldValue.arrayUnion(event.eventID))
                    .addOnSuccessListener {
                        showToast(this, "Event added and user updated successfully")

                    }
                    .addOnFailureListener { e ->
                        showToast(this, "Error updating user: $e")
                    }
            }
            .addOnFailureListener { e ->
                showToast(this, "Error adding event: $e")
            }
    }

    private fun alarm(text: String) {
        if (text.contains(":")) {
            val ls = text.split(" ")
            val lstemp = ls[ls.size - 1].split(":")
            val hour = lstemp[0].toInt()
            val minutes = lstemp[1].toInt()

            createAlarm(hour, minutes)
        } else {
            val ls = text.split(" ")
            val hour = ls[ls.size - 2].toInt()
            val minutes = 0
            createAlarm(hour, minutes)
        }
    }

    private fun createAlarm(hour: Int, minutes: Int) {
        if (hour < 24 && hour >= 0 && minutes < 60 && minutes >= 0) {
            val intent = Intent(AlarmClock.ACTION_SET_ALARM)
                .putExtra(AlarmClock.EXTRA_MESSAGE, "Báo thức")
                .putExtra(AlarmClock.EXTRA_HOUR, hour)
                .putExtra(AlarmClock.EXTRA_MINUTES, minutes)
            startActivity(intent)
        }

    }

    private fun callTaxi() {
        val phoneTaxi = "024323232"
        val intent = Intent(Intent.ACTION_CALL)
        intent.data = Uri.parse("tel:$phoneTaxi")
        startActivity(intent)
    }

    private fun search_google(key: String) {
        val intent = Intent(Intent.ACTION_WEB_SEARCH)
        intent.putExtra(SearchManager.QUERY, key)
        startActivity(intent)
    }

    fun getTimeInSeconds(text: String): Any {
        val arr = text.split(" ")
        val unit = arr.find { it in setOf("giây", "phút", "giờ") }
        val value = arr.getOrNull(2)?.toIntOrNull() ?: 0

        return when (unit) {
            "giây" -> startTimer("đếm ngươc", value)
            "phút" -> startTimer("đếm ngươc", value * 60)
            "giờ" -> startTimer("đếm ngươc", value * 3600)
            else -> 0
        }
    }

    fun startTimer(message: String?, seconds: Int) {
        val intent = Intent(AlarmClock.ACTION_SET_TIMER)
            .putExtra(AlarmClock.EXTRA_MESSAGE, message)
            .putExtra(AlarmClock.EXTRA_LENGTH, seconds)
        startActivity(intent)
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_item_chatbot, menu)
        return super.onCreateOptionsMenu(menu)
    }

    private val voice1: Voice = Voice(
        "vi-vn-x-vif-network", // giong nam mien nam
        Locale("vi", "VN"),
        300,
        300,
        false,
        setOf("NA", "f00", "202009152", "female", null)
    )
    private val voice2: Voice = Voice(
        "vi-vn-x-gft-network",
        Locale("vi", "VN"),
        300,
        300,
        false,
        setOf("NA", "f00", "202009152", "female", null)
    )
    private val voice3: Voice = Voice(
        "vi-vn-x-vie-local",
        Locale("vi", "VN"),
        300,
        300,
        false,
        setOf("NA", "f00", "202009152", "female", null)
    )
    private val voice4: Voice = Voice(
        "vi-vn-x-gft-network",
        Locale("vi", "VN"),
        300,
        300,
        false,
        setOf("NA", "f00", "202009152", "female", null)
    )
    private val voice5: Voice = Voice(
        "vi-VN-language",
        Locale("vi", "VN"),
        300,
        300,
        false,
        setOf("NA", "f00", "202009152", "female", null)
    )
    private val voice6: Voice = Voice(
        "vi-vn-x-vid-local",
        Locale("vi", "VN"),
        300,
        300,
        false,
        setOf("NA", "f00", "202009152", "female", null)
    )

    private val addedVoices: Set<Voice> = setOf(voice1, voice2, voice3, voice4, voice5, voice6)


    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        Log.d("Checkkk", addedVoices.toString())

        when (item.itemId) {


            R.id.speed_075x -> {
                tts.stop()
                tts.setSpeechRate(0.75F)

            }

            R.id.speed_1x -> {
                tts.stop()
                tts.setSpeechRate(1F)

            }

            R.id.speed_2x -> {
                tts.stop()
                tts.setSpeechRate(2F)

            }

            R.id.voice1 -> {

                tts.stop() // giong Nam - niem Nam
                tts.voice = addedVoices.elementAt(0)

            }

            R.id.voice2 -> {
                tts.stop() // giong nu - niem Bac
                tts.voice = addedVoices.elementAt(1)
            }

            R.id.voice3 -> {
                tts.stop() // giọng nữ - miền Nam
                tts.voice = addedVoices.elementAt(2)
            }

            R.id.voice5 -> {
                tts.stop()// giong nam - mien bac
                tts.voice = addedVoices.elementAt(4)
            }


            else -> return super.onOptionsItemSelected(item)
        }

        return true
    }
}