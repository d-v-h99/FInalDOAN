package com.hoangdoviet.finaldoan

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.speech.RecognizerIntent
import android.speech.tts.TextToSpeech
import android.util.Log
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.ai.client.generativeai.GenerativeModel
import com.hoangdoviet.demodoan.adapter.MessageAdapter
import com.hoangdoviet.finaldoan.databinding.ActivityMainBinding
import com.hoangdoviet.finaldoan.model.Message
import com.hoangdoviet.finaldoan.utils.Constants.RECEIVE_ID
import com.hoangdoviet.finaldoan.utils.Constants.SEND_ID
import com.hoangdoviet.finaldoan.utils.Time
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.async
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONObject
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import org.jsoup.nodes.Element
import org.jsoup.select.Elements
import java.net.URL
import java.util.Locale

class MainActivity : AppCompatActivity(), TextToSpeech.OnInitListener {
    lateinit var binding: ActivityMainBinding
    lateinit var adapter: MessageAdapter
    private lateinit var tts: TextToSpeech
    var messagesList = mutableListOf<Message>()
    val generativeModel = GenerativeModel(
        // For text-only input, use the gemini-pro model
        modelName = "gemini-pro",
        apiKey = "AIzaSyB0uf8m7jwu9AjNBL1Ri6g437qJX5Q1e_s"
        // ENTER YOUR KEY
    )
    private val micVoiceActivity =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
            if (it.resultCode == Activity.RESULT_OK) {
                it.data?.apply {
                    getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS)?.let { resultList ->
                        if (resultList.isNotEmpty()) {
                            try {
                                //setResult(resultList[0])
                                //viet ham nay
                                sendMessage(resultList[0])
                            } catch (ex: Exception) {
                                Log.d("SpeechToTextTAG", "micVoiceActivity: ${ex.message}")
                            }
                        }
                    }
                }
            }
        }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        recyclerView()

        clickEvents()
        tts = TextToSpeech(this, this)
    }

    private fun recyclerView() {
        adapter = MessageAdapter()
        binding.rvMessages.adapter = adapter
        binding.rvMessages.layoutManager = LinearLayoutManager(applicationContext)

    }

    private fun clickEvents() {
        binding.mic.setOnClickListener {
            onMicClick()
        }

        //Send a message
        binding.btnSend.setOnClickListener {
            sendMessage(binding.etMessage.text.toString())
            //onMicClick()
        }

        //Scroll back to correct position when user clicks on text view
        binding.etMessage.setOnClickListener {
            GlobalScope.launch {
                delay(100)

                withContext(Dispatchers.Main) {
                    binding.rvMessages.scrollToPosition(adapter.itemCount - 1)

                }
            }
        }
    }

    override fun onStart() {
        super.onStart()
        GlobalScope.launch {
            delay(100)
            withContext(Dispatchers.Main) {
                binding.rvMessages.scrollToPosition(adapter.itemCount - 1)
            }
        }
    }
    private fun sendMessage(text: String) {
        //val message = binding.etMessage.text.toString()
        val message = text
        val timeStamp = Time.timeStamp()

        if (message.isNotEmpty()) {
            //Adds it to our local list
            messagesList.add(Message(message, SEND_ID, timeStamp))
            binding.etMessage.setText("")

            adapter.insertMessage(Message(message, SEND_ID, timeStamp))
            binding.rvMessages.scrollToPosition(adapter.itemCount - 1)

            botResponse(message)
        }
    }

    private fun onMicClick() {
        try {
            val mIntent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH)
            mIntent.putExtra(
                RecognizerIntent.EXTRA_LANGUAGE_MODEL,
                RecognizerIntent.LANGUAGE_MODEL_FREE_FORM
            )
            mIntent.putExtra(
                RecognizerIntent.EXTRA_LANGUAGE, "vi-VN"
            )

            micVoiceActivity.launch(mIntent)
        } catch (ex: Exception) {
            Log.d("SpeechToTextTAG", "onUser1MicClick: ${ex.message}")
        }
    }
    private fun botResponse(message: String) {
        val timeStamp = Time.timeStamp()
        //val intent = Intent(this, MainActivity3::class.java)
        //
//        messagesList.add(Message("Dang nhap...", RECEIVE_ID, timeStamp))
//        val loadingMessage = Message("Đang nhập...", RECEIVE_ID, timeStamp)
//        adapter.insertMessage(loadingMessage)
//        binding.rvMessages.scrollToPosition(adapter.itemCount - 1)

        GlobalScope.launch {
            var responseText = ""
            var contentIntent = ""
            withContext(Dispatchers.Main) {

                when (message.toLowerCase()) {
                    "đọc báo" -> {
                        responseText =
                            "Có các trang báo hỗ trợ tiếng nói sau:\n Người lao động\n Báo tuổi trẻ \n Báo giao thông\nBạn muốn đọc trang báo nào?"

                    }

                    "người lao động" -> {
                        responseText = "Tôi sẽ mở trang báo Người lao động"
                        contentIntent = "https://nld.com.vn/"

                    }

                    "báo tuổi trẻ" -> {
                        responseText = "Tôi sẽ mở trang báo Tuổi trẻ"
                        contentIntent = "https://tuoitre.vn/"

                    }

                    "báo giao thông" -> {
                        responseText = "Tôi sẽ mở trang báo Giao thông"
                        contentIntent = "https://www.baogiaothong.vn/"

                    }

                    "giá vàng" -> {
                        responseText =
                            getGiaVangFromURLAsync("https://ngoctham.com/bang-gia-vang/").await()
                    }
                    "giá xăng" -> {
                        responseText =
                            getGiaXangFromUrlAsync("https://vnexpress.net/chu-de/gia-xang-dau-3026").await()
                    }
                    "xổ số" -> {
                        responseText = getXoSoFromUrlAsync("https://api-xsmb.cyclic.app/api/v1").await()
                    }

                    else -> {
                        val response = generativeModel.generateContent(message)
                        responseText = response?.text ?: "Không có câu trả lời"
                        responseText = responseText.replace("*", "")
                    }
                }
                messagesList.add(Message(responseText, RECEIVE_ID, timeStamp))
//                val index = messagesList.indexOf(loadingMessage)
//                Log.d("check", index.toString())
                //
                adapter.insertMessage(Message(responseText, RECEIVE_ID, timeStamp))
                binding.rvMessages.scrollToPosition(adapter.itemCount - 1)

                playNews(responseText)
                if (contentIntent.isNotEmpty()) {
                    delay(1000)
                    intent.putExtra("article", contentIntent)
                    startActivity(intent)
                }
            }
        }
//        GlobalScope.launch {
//            //Fake response delay
//            delay(1000)
//
//            withContext(Dispatchers.Main) {
//                //Gets the response
//               // val response = "Day la cau tra loi"
//                val response = generativeModel.generateContent(message)
//
//                //Adds it to our local list
//                messagesList.add(Message(response.toString(), RECEIVE_ID, timeStamp))
//
//                //Inserts our message into the adapter
//                adapter.insertMessage(Message(response.toString(), RECEIVE_ID, timeStamp))
//
//                //Scrolls us to the position of the latest message
//                binding.rvMessages.scrollToPosition(adapter.itemCount - 1)
//
//            }
//        }
    }
    private fun playNews(data: String) {
        tts.speak(data, TextToSpeech.QUEUE_FLUSH, null, "")
    }
    override fun onInit(status: Int) {
        if (status == TextToSpeech.SUCCESS) {
            val result = tts.setLanguage(Locale("vi", "VN"))
            if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED) {
                Toast.makeText(this, "TTS Not Supported for Vietnamese", Toast.LENGTH_LONG).show()
            }
        }
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
            } catch (e: Exception){
                e.printStackTrace()
                return@async "Có lỗi xảy ra khi truy cập thông tin giá xăng."
            }
        }
    }
    private fun getXoSoFromUrlAsync(url: String) : Deferred<String> {
        return GlobalScope.async {
            try {
                var content ="Không có thông tin"
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
                content=""
                content+="Thời gian: $time\nGiải đặc biệt: $gdb\nGiải nhất: $g1\nGiải nhì:  ${g2List.joinToString(separator = ", ")}\n"
                // In ra các giá trị đã lấy được
                Log.d("checkkk","Thời gian: $time")
                println("ĐB: $gdb")
                println("G1: $g1")
                Log.d("checkkk","G2: ${g2List.joinToString(separator = ", ")}")
                println("G3: ${g3List.joinToString(separator = ", ")}")
                println("G4: ${g4List.joinToString(separator = ", ")}")
                println("G5: ${g5List.joinToString(separator = ", ")}")
                println("G6: ${g6List.joinToString(separator = ", ")}")
                println("G7: ${g7List.joinToString(separator = ", ")}")
                return@async content
            }catch (e: Exception){
                e.printStackTrace()
                return@async "Có lỗi xảy ra khi truy cập thông tin XSMB."
            }
        }
    }
}