package com.hoangdoviet.finaldoan

import android.accounts.AccountManager
import android.app.Activity
import android.app.SearchManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
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
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.chaquo.python.Python
import com.chaquo.python.android.AndroidPlatform
import com.github.zagum.speechrecognitionview.adapters.RecognitionListenerAdapter
import com.google.ai.client.generativeai.GenerativeModel
import com.google.api.client.extensions.android.http.AndroidHttp
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential
import com.google.api.client.json.jackson2.JacksonFactory
import com.google.api.client.util.DateTime
import com.google.api.client.util.ExponentialBackOff
import com.google.api.services.calendar.CalendarScopes
import com.google.api.services.calendar.model.EventDateTime
import com.google.api.services.calendar.model.EventReminder
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.hoangdoviet.finaldoan.adapter.MessageAdapter
import com.hoangdoviet.finaldoan.databinding.ActivityMain2Binding
import com.hoangdoviet.finaldoan.firebase.FirebaseHelper
import com.hoangdoviet.finaldoan.fragment.profileFragment
import com.hoangdoviet.finaldoan.fragment.profileFragment.Companion.PREF_ACCOUNT_NAME
import com.hoangdoviet.finaldoan.model.Event
import com.hoangdoviet.finaldoan.model.EventCreator
import com.hoangdoviet.finaldoan.model.Message
import com.hoangdoviet.finaldoan.model.Task
import com.hoangdoviet.finaldoan.utils.Constants
import com.hoangdoviet.finaldoan.utils.DateScheduler
import com.hoangdoviet.finaldoan.utils.Time
import com.hoangdoviet.finaldoan.utils.showToast
import com.hoangdoviet.finaldoan.viewmodel.UserGoogleViewModel
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
import pub.devrel.easypermissions.EasyPermissions
import java.net.URL
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import java.util.UUID
import java.util.regex.Pattern


class MainActivity2 : AppCompatActivity(), TextToSpeech.OnInitListener {

    lateinit var binding: ActivityMain2Binding
    lateinit var adapter: MessageAdapter
    lateinit var tts: TextToSpeech
    lateinit var speechRecognizer: SpeechRecognizer
    lateinit var titleArticle: String
    lateinit var hashMap: HashMap<Int, Triple<String, String, String>>
    var messagesList = mutableListOf<Message>()
    val generativeModel = GenerativeModel(
        modelName = "gemini-pro",
        apiKey = "AIzaSyB0uf8m7jwu9AjNBL1Ri6g437qJX5Q1e_s"
    )
    private val mAuth: FirebaseAuth by lazy { FirebaseAuth.getInstance() }
    private  var firebaseHelper: FirebaseHelper = FirebaseHelper()
    private lateinit var viewModel: UserGoogleViewModel
    private var checklogin = false
    private var mCredential: GoogleAccountCredential? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMain2Binding.inflate(layoutInflater)
        setContentView(binding.root)
        initCredentials()
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
        binding.btnHistory.setOnClickListener {
            val currentUserUid = mAuth.currentUser?.uid
            if(currentUserUid.isNullOrEmpty()){
                showToast(this, "Đăng nhập tài khoản để xem lịch sử trò chuyện")
                return@setOnClickListener
            }
            firebaseHelper.getAllMessages { messages ->
                adapter.messageList = messages.toMutableList()
                adapter.notifyDataSetChanged()
                binding.reyclerviewMessageList.scrollToPosition(adapter.itemCount - 1)
            }
        }
        viewModel = ViewModelProvider(this).get(UserGoogleViewModel::class.java)
        viewModel.isGoogleLoggedIn.observe(this, Observer { isLoggedIn ->
            checklogin = isLoggedIn
            if (isLoggedIn) {
                Log.d("checkdangnhap", "Đăng nhập rồi")
                if (mCredential?.selectedAccountName == null){
                    chooseAccount()
                }
            } else {
                Log.d("checkdangnhap", "Chưa đăng nhập")
            }
        })


    }
    private fun initCredentials() {
        mCredential = GoogleAccountCredential.usingOAuth2(
            this, listOf(CalendarScopes.CALENDAR)
        ).setBackOff(ExponentialBackOff())
        val accountName = getPreferences(Context.MODE_PRIVATE)
            .getString(profileFragment.PREF_ACCOUNT_NAME, null)
        if (accountName != null) {
            mCredential?.selectedAccountName = accountName
        }
        Log.d("checkkklogin", mCredential.toString())
    }
    private fun chooseAccount() {
        if (EasyPermissions.hasPermissions(this, android.Manifest.permission.GET_ACCOUNTS)) {
            val accountName = getPreferences(Context.MODE_PRIVATE)
                .getString(profileFragment.PREF_ACCOUNT_NAME, null)
            if (accountName != null) {
                mCredential?.selectedAccountName = accountName
            } else {
                mCredential?.newChooseAccountIntent()
                    ?.let { startActivityForResult(it, profileFragment.REQUEST_ACCOUNT_PICKER) }
            }
        } else {
            EasyPermissions.requestPermissions(
                this, "Ứng dụng cần quyền truy cập vào tài khoản Google của bạn.",
                profileFragment.REQUEST_PERMISSION_GET_ACCOUNTS, android.Manifest.permission.GET_ACCOUNTS
            )
        }
    }
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        when (requestCode) {
            profileFragment.REQUEST_ACCOUNT_PICKER -> {
                if (resultCode == Activity.RESULT_OK && data != null && data.extras != null) {
                    val accountName = data.getStringExtra(AccountManager.KEY_ACCOUNT_NAME)
                    if (accountName != null) {
                        val settings = getPreferences(Context.MODE_PRIVATE)
                        val editor = settings.edit()
                        editor.putString(profileFragment.PREF_ACCOUNT_NAME, accountName)
                        editor.apply()
                        mCredential?.selectedAccountName = accountName
                        Log.d("checkkklogin", "Account selected: $accountName")
                    }
                }
            }
        }
    }


    private fun setup() {
        adapter = MessageAdapter()
        binding.reyclerviewMessageList.adapter = adapter
        binding.reyclerviewMessageList.layoutManager = LinearLayoutManager(this)
        binding.layoutChatbox.visibility = View.INVISIBLE
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
        speechRecognizer = SpeechRecognizer.createSpeechRecognizer(this)
        binding.recognitionView.setSpeechRecognizer(speechRecognizer)
        binding.recognitionView.setRecognitionListener(object : RecognitionListenerAdapter() {
            override fun onResults(results: Bundle) {
                finishRecognition()
                val matches = results.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                if (!matches.isNullOrEmpty()) {
                    val text = matches[0]
                    sendMessage(text)
                }
            }
        })


        binding.recognitionView.setOnClickListener {
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
        binding.btnHistory.visibility = View.GONE
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
        binding.btnHistory.visibility = View.VISIBLE
        binding.recognitionView!!.stop()
        binding.recognitionView!!.play()
        binding.recognitionView!!.visibility = View.GONE
    }

    private fun KeyboardToSpeech() {
        binding.layoutChatbox.setVisibility(View.INVISIBLE)
        binding.layoutSpeech!!.visibility = View.VISIBLE
        val params = FrameLayout.LayoutParams(
            FrameLayout.LayoutParams.MATCH_PARENT,
            FrameLayout.LayoutParams.WRAP_CONTENT
        )
        params.setMargins(0, 0, 0, 410)
        closeKeyboard()
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
        val message = text
        val timeStamp = Time.timeStamp()

        if (message.isNotEmpty()) {
            messagesList.add(Message(message, Constants.SEND_ID, timeStamp))
            binding.edittextChatbox.setText("")
            // Hiển thị loading indicator
            adapter.setLoading(true)
            adapter.insertMessage(Message(message, Constants.SEND_ID, timeStamp))
            firebaseHelper.insertMessage(Message(message, Constants.SEND_ID, timeStamp))
            binding.reyclerviewMessageList.scrollToPosition(adapter.itemCount - 1)

            botResponse(message)
        }
    }

    private fun botResponse(message: String) {
        val timeStamp = Time.timeStamp()
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
                        responseText = "Tôi sẽ mở Google"
                        val keywordPattern = "tìm kiếm thông tin về\\s*(.+)"
                        val pattern = Pattern.compile(keywordPattern, Pattern.CASE_INSENSITIVE)
                        val matcher = pattern.matcher(message)

                        if (matcher.find()) {
                            val searchQuery = matcher.group(1).trim()
                            if (searchQuery.isNotEmpty()) {
                                scope.launch {
                                    delay(1500)
                                    search_google(searchQuery)
                                }
                            } else {
                                responseText = "Không tìm thấy nội dung tìm kiếm. Vui lòng thử lại.1"
                            }
                        } else {
                            responseText = "Không tìm thấy từ khóa tìm kiếm. Vui lòng thử lại.2"
                        }
                    }


                    message.toLowerCase().contains("giá vàng") -> {
                        responseText =
                            getGiaVangFromURLAsync("https://ngoctham.com/bang-gia-vang/").await()
                    }

                    message.toLowerCase().contains("giá xăng") -> {
                        responseText =
                            getGiaXangFromUrlAsync("https://vnexpress.net/chu-de/gia-xang-dau-3026").await()
                    }
                    message.toLowerCase().contains("đặt lịch") && message.toLowerCase().contains("âm lịch")||
                            message.toLowerCase().contains("đặt lịch") && message.toLowerCase().contains("ngày rằm")||
                            message.toLowerCase().contains("đặt lịch") && message.toLowerCase().contains("rằm")-> {
                        val Date = DateScheduler.extractLunarDate(message)
                        val time = DateScheduler.findTimeReferences(message)
                        val content = DateScheduler.getStringAfterKeyword(message)
                        val currentUserUid = mAuth.currentUser?.uid
                        if (currentUserUid.isNullOrEmpty()){
                            responseText = "Đăng nhập để sử dụng tính năng trên"
                        }else {
                            try {
                                responseText = time +"\n"+content+"\n"+Date
                                if(time.isNullOrEmpty()) responseText = "Bạn cần thêm thêm các từ chỉ thời gian để đặt lịch như 5 giờ sáng , 5 giờ chiều , hoặc mốc thời gian cụ thể như 10:45\n Ví dụ đặt lịch cho tôi ngày mai lúc 10 GIỜ SÁNG với nội dung đi học toán"
                                else if (content.isNullOrEmpty())   responseText = "Bạn cần thêm thêm từ nội dung để đặt lịch\n Ví dụ đặt lịch cho tôi ngày mai lúc 10 giờ sáng với NỘI DUNG đi học toán"
                                else if (Date.isNullOrEmpty()) responseText = "Không thể xác định các từ thời gian. Thử lại"
                                else if (Date.contains("Không")) responseText = "Không xác định được ngày âm lịch"
                                else{
                                    val event = Event(
                                        eventID = generateEventId(),
                                        date = convertDateString(Date),
                                        title = content,
                                        timeStart = time,
                                        timeEnd =addOneHourToTime(time) ,
                                        repeat = 0
                                    )
                                    responseText = "Thêm sự kiện thành công\n $content vào ngày ${event.date}"
                                    if(checklogin) {
                                        createCalendarEvent(event)
                                    }
                                    addSingleEvent(currentUserUid, event)

                                }
                            }catch (e: Exception){
                                responseText = "Có lỗi khi thêm sự kiện $e"
                            }
                        }

                    }
                    message.toLowerCase().contains("đặt lịch") -> {
                        val time = DateScheduler.findTimeReferences(message)
                        val content = DateScheduler.getStringAfterKeyword(message)
                        val listWordDate = DateScheduler.extractTemporalWords(message)
                        Log.d("checkTIme", listWordDate.toString())
                        val Date = DateScheduler.checktime(listWordDate)
                        val currentUserUid = mAuth.currentUser?.uid
                        if (currentUserUid.isNullOrEmpty()){
                            responseText = "Đăng nhập để sử dụng tính năng trên"
                        }
                        else {
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
                                    Log.d("checklogin", checklogin.toString())
                                    if(checklogin) {
                                        createCalendarEvent(event)
                                    }
                                    addSingleEvent(currentUserUid, event)
                                }

                            }catch (e: Exception){
                                responseText = "Có lỗi khi thêm sự kiện +$e"
                            }
                        }

                    }
                    message.toLowerCase().contains("thêm nhiệm vụ") -> {
                        val content = DateScheduler.getStringAfterKeyword(message)
                        val listWordDate = DateScheduler.extractTemporalWords(message)
                        Log.d("checkTIme", listWordDate.toString())
                        val Date = DateScheduler.checktime(listWordDate)
                        val currentUserUid = mAuth.currentUser?.uid
                        if (currentUserUid.isNullOrEmpty()){
                            responseText = "Đăng nhập để sử dụng tính năng trên"
                        }
                        else {
                            responseText = "\n"+content+"\n"+Date
                             if (content.isNullOrEmpty())   responseText = "Bạn cần thêm thêm từ nội dung để đặt lịch\n Ví dụ đặt lịch cho tôi ngày mai lúc 10 giờ sáng với NỘI DUNG đi học toán"
                            else if(listWordDate.isEmpty())  responseText = "Bạn cần thêm thêm các từ chỉ ngày để xác định ngày đặt lịch ví dụ hôm nay , ngày mai, thứ hai tuần sau \n Ví dụ đặt lịch cho tôi NGÀY MAI lúc 10 giờ sáng với nội dung đi học toán"
                            else if (Date.isNullOrEmpty()) responseText = "Không thể xác định các từ thời gian. Thử lại"
                            else if (Date.contains("Lỗi vì ngày")) responseText = "Thời gian đặt lịch đã quá hạn, Vui lòng nói thời gian hợp lệ"
                            else {
                                 responseText = "Thêm nhiệm vụ thành công\n $content vào ngày ${convertDateString(Date)}"
                                 Log.d("Checktime",convertDateString1(Date) )
                                 addTask(currentUserUid, convertDateString1(Date), content)
                             }
                        }
                    }
                    message.toLowerCase().contains("hôm nay") && message.toLowerCase().contains("sự kiện") -> {
                        val currentUserUid = mAuth.currentUser?.uid
                        if(currentUserUid == null){
                            responseText = "Đăng nhập để xem sự kiện"
                        }
                        else {
                            Log.d("checkhomnay", currentUserUid.toString())
                            val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
                            val todayDate = dateFormat.format(Date())
                            Log.d("checkhomnay", todayDate)
                            val events = withContext(Dispatchers.IO) {
                                getEventsByUserAndDate(currentUserUid, todayDate)
                            }
                            Log.d("checkhomnay", events.toString())

                            responseText = if (events.isNotEmpty()) {
                                events.joinToString(separator = "\n") { event ->
                                    "${event.title}: ${event.timeStart} - ${event.timeEnd}"
                                }
                            } else {
                                "Hôm nay không có sự kiện"
                            }
                        }

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
                adapter.setLoading(false)
                adapter.insertMessage(Message(responseText, Constants.RECEIVE_ID, timeStamp))
                firebaseHelper.insertMessage(Message(responseText, Constants.RECEIVE_ID, timeStamp))
                binding.reyclerviewMessageList.scrollToPosition(adapter.itemCount - 1)
                if (ArticleText.isNotEmpty()) {
                    playNews(ArticleText)
                } else {
                    playNews(responseText)
                }

            }
        }

    }
    private fun ensureCredential() {
        if (mCredential == null) {
            mCredential = GoogleAccountCredential.usingOAuth2(
                this, listOf(CalendarScopes.CALENDAR)
            ).setBackOff(ExponentialBackOff())

            val accountName = getPreferences(Context.MODE_PRIVATE)
                .getString(PREF_ACCOUNT_NAME, null)

            if (accountName != null) {
                mCredential?.selectedAccountName = accountName
            } else {
                // Xử lý khi tài khoản không có hoặc null
                Log.e("Credential22", "Account name is null")
            }
        }
    }

    private fun createCalendarEvent(event: Event, recurrenceType: String? =null) {
        try {
            val dateFormat = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())

            val startDateTimeString = "${event.date} ${event.timeStart}"
            val endDateTimeString = "${event.date} ${event.timeEnd}"

            val startDate = dateFormat.parse(startDateTimeString)
            val endDate = dateFormat.parse(endDateTimeString)

            val startDateTime = DateTime(startDate)
            val endDateTime = DateTime(endDate)

            val googleEvent = com.google.api.services.calendar.model.Event()
                .setSummary(event.title)
                .setLocation("Hà Nội, Việt Nam")
                .setDescription("Đặt lịch bởi ứng dụng LichThongMinh")
            val start = EventDateTime()
                .setDateTime(startDateTime)
                .setTimeZone("Asia/Ho_Chi_Minh")
            googleEvent.start = start
            val end = EventDateTime()
                .setDateTime(endDateTime)
                .setTimeZone("Asia/Ho_Chi_Minh")
            googleEvent.end = end
            // Thiết lập tái phát
            val recurrence = when (recurrenceType) {
                "1" -> listOf("RRULE:FREQ=DAILY;COUNT=10")
                "3" -> listOf("RRULE:FREQ=WEEKLY;COUNT=10")
                "4" -> listOf("RRULE:FREQ=MONTHLY;COUNT=10")
                "5" -> listOf("RRULE:FREQ=YEARLY;COUNT=10")
                else -> null
            }
            recurrence?.let {
                googleEvent.recurrence = it
            }

            val reminderOverrides = listOf(
                EventReminder().setMethod("email").setMinutes(24 * 60),
                EventReminder().setMethod("popup").setMinutes(10)
            )

            val reminders = com.google.api.services.calendar.model.Event.Reminders()
                .setUseDefault(false)
                .setOverrides(reminderOverrides)
            googleEvent.reminders = reminders

            val calendarId = "primary"
            val transport = AndroidHttp.newCompatibleTransport()
            val jsonFactory = JacksonFactory.getDefaultInstance()
            val service = com.google.api.services.calendar.Calendar.Builder(
                transport, jsonFactory, mCredential
            )
                .setApplicationName("Google Calendar API Android Quickstart")
                .build()
            Log.d("checkkklogin", mCredential.toString())

            EventCreator(this,service, calendarId, googleEvent).execute()
        } catch (e: Exception) {
            e.printStackTrace()
            Log.d("checkkklogin", e.message.toString())
            Toast.makeText(this, "Lỗi khi tạo sự kiện: ${e.message}", Toast.LENGTH_LONG).show()

        }

    }


private suspend fun getEventsByUserAndDate(userId: String, date: String): List<Event> {
    return withContext(Dispatchers.IO) {
        try {
            val db = FirebaseFirestore.getInstance()
            val userRef = db.collection("User").document(userId)

            Log.d("Firestore", "Fetching user document for userId: $userId")
            val userDocument = userRef.get().await()
            Log.d("Firestore", "User document fetched: ${userDocument.data}")

            val eventIds = userDocument.get("eventID") as? List<String> ?: emptyList()
            Log.d("Firestore", "Event IDs: $eventIds")

            if (eventIds.isEmpty()) {
                Log.d("Firestore", "No event IDs to query")
                return@withContext emptyList<Event>()
            }
            val resultEvents = mutableListOf<Event>()
            for (chunk in eventIds.chunked(30)) {
                val eventsRef = db.collection("Events")
                val eventsSnapshot = eventsRef.whereIn("eventID", chunk)
                    .whereEqualTo("date", date)
                    .get()
                    .await()

                val events = eventsSnapshot.toObjects(Event::class.java)
                resultEvents.addAll(events)
                if (resultEvents.size >= 10) {
                    break
                }
            }
            resultEvents
        } catch (e: Exception) {
            e.printStackTrace()
            Log.e("Firestore", "Error fetching events", e)
            emptyList()
        }
    }
}
    fun convertDateString(inputDate: String): String {
        val inputFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val outputFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
        val date = inputFormat.parse(inputDate)
        return outputFormat.format(date)
    }
    fun convertDateString1(inputDate: String): String {
        val inputFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val outputFormat = SimpleDateFormat("yyyyMMdd", Locale.getDefault())
        val date = inputFormat.parse(inputDate)
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
        titleArticle = "Hôm nay có các tin tức sau\n"
        withContext(Dispatchers.IO) {
            hashMap = getArticle(url)
        }
        for ((key, value) in hashMap) {
            Log.d(
                "hashMap",
                "Khóa: $key, Giá trị: (${value.first}, ${value.second}, TIEU DE ${value.third})"
            )
            titleArticle += "Tin số $key ${value.third}\n"
        }
        titleArticle += "Bạn muốn đọc tin số mấy?"
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
            val uniqueLinks = mutableSetOf<String>()
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
                Log.d("chekvang", e.toString())
                return@async "Có lỗi xảy ra khi truy cập thông tin giá vàng."
            }
        }
    }

    private fun getGiaXangFromUrlAsync(url: String): Deferred<String> {
        return GlobalScope.async {
            try {
                var message = "Không có thông tin"
                val document: Document = Jsoup.connect(url).get()
                val tables: Elements = document.getElementsByTag("table")
                for (table in tables) {
                    val rows: Elements = table.getElementsByTag("tr")
                    message = ""
                    for (row in rows) {
                        val cells: Elements = row.getElementsByTag("td")
                        val rowData = ArrayList<String>()
                        for (cell in cells) {
                            rowData.add(cell.text())
                        }
                        if (rowData.size == 3) {
                            val matHang = rowData[0]
                            val gia = rowData[1]
                            val soVoiKyTruoc = rowData[2]
                            if (matHang != "Mặt hàng") {
                                message += "$matHang: $gia đồng/lít. (Tăng $soVoiKyTruoc đồng)\n"
                            }
                        }
                    }
                    message += "\nThông tin từ website vnexpress.net"
                    break
                }
                return@async message
            } catch (e: Exception) {
                e.printStackTrace()
                return@async "Có lỗi xảy ra khi truy cập thông tin giá xăng."
            }
        }
    }


    private fun addTask(userId: String, date: String, title: String) {
        val db = FirebaseFirestore.getInstance()
        val taskId = db.collection("Tasks").document().id
        val task = Task(id = taskId, title = title, status = "Chưa làm")
        db.collection("Tasks").document(taskId).set(task)
            .addOnSuccessListener {
                Log.d("TaskActivity", "Task added to Tasks collection successfully")
                val tasksByDateId = "$date-$userId"
                val tasksByDateRef = db.collection("TasksByDate").document(tasksByDateId)
                tasksByDateRef.get().addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        val document = task.result
                        if (document != null && document.exists()) {
                            tasksByDateRef.update("taskIds", FieldValue.arrayUnion(taskId))
                                .addOnSuccessListener {
                                    Log.d("TaskActivity", "Task added to TasksByDate collection successfully")
                                    db.collection("User").document(userId)
                                        .update("taskIds", FieldValue.arrayUnion(taskId))
                                        .addOnSuccessListener {
                                            showToast(this, "Thêm nhiệm vụ thành công")
                                        }
                                        .addOnFailureListener { e ->
                                            Log.e("TaskActivity", "Failed to update user's taskIds", e)
                                        }
                                }
                                .addOnFailureListener { e ->
                                    Log.e("TaskActivity", "Failed to update TasksByDate", e)
                                }
                        } else {
                            val newTaskDate = hashMapOf(
                                "taskIds" to arrayListOf(taskId)
                            )
                            tasksByDateRef.set(newTaskDate)
                                .addOnSuccessListener {
                                    Log.d("TaskActivity", "TaskByDate document created successfully")
                                    db.collection("User").document(userId)
                                        .update("taskIds", FieldValue.arrayUnion(taskId))
                                        .addOnSuccessListener {
                                            Log.d("TaskActivity", "Task added to User's taskIds successfully")
                                        }
                                        .addOnFailureListener { e ->
                                            Log.e("TaskActivity", "Failed to update user's taskIds", e)
                                        }
                                }
                                .addOnFailureListener { e ->
                                    Log.e("TaskActivity", "Failed to create TaskByDate document", e)
                                }
                        }
                    } else {
                        Log.e("TaskActivity", "Failed to get TaskByDate document", task.exception)
                    }
                }
            }
            .addOnFailureListener { e ->
                Log.e("TaskActivity", "Failed to add task", e)
            }
    }
    fun addSingleEvent(userId: String, event: Event)  {
        val db = FirebaseFirestore.getInstance()
        val eventRef = db.collection("Events").document(event.eventID)

        eventRef.set(event)
            .addOnSuccessListener {
                db.collection("User").document(userId)
                    .update("eventID", FieldValue.arrayUnion(event.eventID))
                    .addOnSuccessListener {
                        showToast(this, "Thêm sự kiện thành công")

                    }
                    .addOnFailureListener { e ->
                        showToast(this, "Có lỗi khi thêm sự kiện: $e")
                    }
            }
            .addOnFailureListener { e ->
                showToast(this, "Có lỗi khi thêm sự kiện: $e")
            }
    }

    private fun alarm(text: String) {
        val text = DateScheduler.findTimeReferences(text)
            val ls = text?.split(" ")
            val lstemp = ls?.get(ls.size - 1)?.split(":")
            val hour = lstemp?.get(0)?.toInt()
            val minutes = lstemp?.get(1)?.toInt()
            if(hour != null && minutes != null)
            createAlarm(hour, minutes)
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
        val unit = arr.find { it in setOf("giây", "phút", "giờ","tiếng") }
        val value = arr.getOrNull(2)?.toIntOrNull() ?: 0

        return when (unit) {
            "giây" -> startTimer("đếm ngươc", value)
            "phút" -> startTimer("đếm ngươc", value * 60)
            "giờ" -> startTimer("đếm ngươc", value * 3600)
            "tiếng" -> startTimer("đếm ngươc", value * 3600)
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