package com.hoangdoviet.finaldoan.fragment

import android.accounts.AccountManager
import android.app.Activity
import android.app.ProgressDialog
import android.content.Context
import android.content.Intent
import android.net.ConnectivityManager
import android.os.AsyncTask
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import com.google.android.gms.common.ConnectionResult
import com.google.android.gms.common.GoogleApiAvailability
import com.google.api.client.extensions.android.http.AndroidHttp
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential
import com.google.api.client.json.jackson2.JacksonFactory
import com.google.api.client.util.DateTime
import com.google.api.client.util.ExponentialBackOff
import com.google.api.services.calendar.Calendar
import com.google.api.services.calendar.CalendarScopes
import com.google.api.services.calendar.model.Event
import com.google.api.services.calendar.model.EventAttendee
import com.google.api.services.calendar.model.EventDateTime
import com.google.api.services.calendar.model.EventReminder
import com.google.firebase.auth.FirebaseAuth
import com.hoangdoviet.finaldoan.AuthActivity
import com.hoangdoviet.finaldoan.databinding.FragmentProfileBinding
import com.hoangdoviet.finaldoan.model.EventCreator
import com.hoangdoviet.finaldoan.utils.showToast
import com.hoangdoviet.finaldoan.viewmodel.UserGoogleViewModel
import pub.devrel.easypermissions.AfterPermissionGranted
import pub.devrel.easypermissions.EasyPermissions
import java.text.SimpleDateFormat
import java.util.Locale

class profileFragment : Fragment() {
    private var _binding: FragmentProfileBinding? = null
    private val binding get() = _binding!!
    private val mAuth: FirebaseAuth by lazy { FirebaseAuth.getInstance() }
    private val userViewModel: UserGoogleViewModel by activityViewModels()

    companion object {
        const val REQUEST_ACCOUNT_PICKER = 1000
        const val REQUEST_AUTHORIZATION = 1001
        const val REQUEST_GOOGLE_PLAY_SERVICES = 1002
        const val REQUEST_PERMISSION_GET_ACCOUNTS = 1003
        const val PREF_ACCOUNT_NAME = "accountName"
        const val PREFS_NAME = "MyPrefs"
        const val GOOGLE_LOGIN_STATUS = "GoogleLoginStatus"
    }

    private var mCredential: GoogleAccountCredential? = null
    private var mProgress: ProgressDialog? = null


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentProfileBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        updateUI()
        initCredentials()
        initView()
        userViewModel.isGoogleLoggedIn.observe(viewLifecycleOwner) { isGoogleLoggedIn ->
            updateUIGoogle(isGoogleLoggedIn)
        }

        binding.btnLogin.setOnClickListener {
            val intent = Intent(activity, AuthActivity::class.java)
            startActivity(intent)
        }

        binding.btnLogout.setOnClickListener {
            if (mAuth.currentUser != null) {
                mAuth.signOut()
                showToast(requireContext(), "Thoát tài khoản thành công")
                updateUI() // Cập nhật lại giao diện sau khi đăng xuất
            }
        }
    }

    private fun updateUIGoogle(isGoogleLoggedIn: Boolean) {
        if (isGoogleLoggedIn) {
            // Người dùng đã đăng nhập Google
            binding.btnLogoutGoogle.visibility = View.VISIBLE
        } else {
            // Người dùng chưa đăng nhập Google
            binding.btnLogoutGoogle.visibility = View.GONE
        }

    }

    private fun initView() {
        mProgress = ProgressDialog(requireContext())
        mProgress!!.setMessage("Loading...")

        binding.btnLoginGoogleAPI.setOnClickListener {
            getResultsFromApi()

        }

        binding.btnAddEvent.setOnClickListener {
            //createCalendarEvent()
            val event = com.hoangdoviet.finaldoan.model.Event(
                eventID = "1111122",
                date = "18/06/2024",
                title = "checkkk 123",
                timeStart = "02:23",
                timeEnd = "02:43",
                repeat = 0
            )
          createCalendarEvent(event)

        }
        binding.btnLogoutGoogle.setOnClickListener {
            unlinkGoogleAccount()
        }
    }

    private fun updateUI() {
        if (mAuth.currentUser != null) {
            binding.btnLogout.visibility = View.VISIBLE
            binding.username.text = mAuth.currentUser?.displayName ?: "Người dùng"
        } else {
            binding.btnLogout.visibility = View.GONE
        }
//        val isGoogleLoggedIn = mCredential
    //        ?.selectedAccountName != null
//        userViewModel.setGoogleLoggedIn(isGoogleLoggedIn)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun getResultsFromApi() {
        if (!isGooglePlayServicesAvailable()) {
            acquireGooglePlayServices()
            Log.d("checkapi", "a")
        } else if (mCredential!!.selectedAccountName == null) {
            chooseAccount()
            Log.d("checkapi", "b")
        } else if (!isDeviceOnline()) {
            Log.d("checkapi", "c")
        } else {
            userViewModel.setGoogleLoggedIn(true)
            binding.btnLogoutGoogle.isEnabled = true

        }
    }

    private fun isGooglePlayServicesAvailable(): Boolean {
        val apiAvailability = GoogleApiAvailability.getInstance()
        val connectionStatusCode = apiAvailability.isGooglePlayServicesAvailable(requireContext())
        return connectionStatusCode == ConnectionResult.SUCCESS
    }

    private fun isDeviceOnline(): Boolean {
        val connMgr = requireContext().getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val networkInfo = connMgr.activeNetworkInfo
        return networkInfo != null && networkInfo.isConnected
    }

    @AfterPermissionGranted(REQUEST_PERMISSION_GET_ACCOUNTS)
    private fun chooseAccount() {
        if (EasyPermissions.hasPermissions(requireContext(), android.Manifest.permission.GET_ACCOUNTS)) {
            val accountName = requireActivity().getPreferences(Context.MODE_PRIVATE)
                .getString(PREF_ACCOUNT_NAME, null)
            if (accountName != null) {
                mCredential!!.selectedAccountName = accountName
                getResultsFromApi()
            } else {
                startActivityForResult(mCredential!!.newChooseAccountIntent(), REQUEST_ACCOUNT_PICKER)
            }
        } else {
            EasyPermissions.requestPermissions(
                this,
                "Ứng dụng cần quyền truy cập vào tài khoản Google của bạn.",
                REQUEST_PERMISSION_GET_ACCOUNTS,
                android.Manifest.permission.GET_ACCOUNTS
            )
        }
    }

    private fun acquireGooglePlayServices() {
        val apiAvailability = GoogleApiAvailability.getInstance()
        val connectionStatusCode = apiAvailability.isGooglePlayServicesAvailable(requireContext())
        if (apiAvailability.isUserResolvableError(connectionStatusCode)) {
            showGooglePlayServicesAvailabilityErrorDialog(connectionStatusCode)
        }
    }

//    private fun createCalendarEvent() {
//        val event = Event()
//            .setSummary("Google I/O 2015")
//            .setLocation("800 Howard St., San Francisco, CA 94103")
//            .setDescription("A chance to hear more about Google's developer products.")
//        val startDateTime = DateTime(System.currentTimeMillis())
//        val start = EventDateTime()
//            .setDateTime(startDateTime)
//            .setTimeZone("Asia/Ho_Chi_Minh")
//        event.start = start
//        val endDateTime = DateTime(System.currentTimeMillis() + 3600000)
//        val end = EventDateTime()
//            .setDateTime(endDateTime)
//            .setTimeZone("Asia/Ho_Chi_Minh")
//        event.end = end
//
//        val reminderOverrides = listOf(
//            EventReminder().setMethod("email").setMinutes(24 * 60),
//            EventReminder().setMethod("popup").setMinutes(10))
//
//        val reminders = Event.Reminders()
//            .setUseDefault(false)
//            .setOverrides(reminderOverrides)
//        event.reminders = reminders
//
//        val calendarId = "primary"
//        val transport = AndroidHttp.newCompatibleTransport()
//        val jsonFactory = JacksonFactory.getDefaultInstance()
//        val service = Calendar.Builder(
//            transport, jsonFactory, mCredential)
//            .setApplicationName("Google Calendar API Android Quickstart")
//            .build()
//
//        EventCreator(service, calendarId, event).execute()
//    }
private fun createCalendarEvent(dateString: String, timeStart: String, timeEnd: String) {
    val dateFormat = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())

    val startDateTimeString = "$dateString $timeStart"
    val endDateTimeString = "$dateString $timeEnd"

    val startDate = dateFormat.parse(startDateTimeString)
    val endDate = dateFormat.parse(endDateTimeString)

    val startDateTime = DateTime(startDate)
    val endDateTime = DateTime(endDate)

    val event = Event()
        .setSummary("Google I/O 2015")
        .setLocation("800 Howard St., San Francisco, CA 94103")
        .setDescription("A chance to hear more about Google's developer products.")
    val start = EventDateTime()
        .setDateTime(startDateTime)
        .setTimeZone("Asia/Ho_Chi_Minh")
    event.start = start
    val end = EventDateTime()
        .setDateTime(endDateTime)
        .setTimeZone("Asia/Ho_Chi_Minh")
    event.end = end

    val reminderOverrides = listOf(
        EventReminder().setMethod("email").setMinutes(24 * 60),
        EventReminder().setMethod("popup").setMinutes(10))

    val reminders = Event.Reminders()
        .setUseDefault(false)
        .setOverrides(reminderOverrides)
    event.reminders = reminders

    val calendarId = "primary"
    val transport = AndroidHttp.newCompatibleTransport()
    val jsonFactory = JacksonFactory.getDefaultInstance()
    val service = Calendar.Builder(
        transport, jsonFactory, mCredential)
        .setApplicationName("Google Calendar API Android Quickstart")
        .build()


    EventCreator(service, calendarId, event).execute()

}


    private fun initCredentials() {
        mCredential = GoogleAccountCredential.usingOAuth2(
            requireContext().applicationContext,
            arrayListOf(CalendarScopes.CALENDAR)
        ).setBackOff(ExponentialBackOff())
        val accountName = requireActivity().getPreferences(Context.MODE_PRIVATE)
            .getString(PREF_ACCOUNT_NAME, null)
        if (accountName != null) {
            mCredential?.selectedAccountName = accountName
        }
        userViewModel.setCredential(mCredential)
    }

    private inner class EventCreator(
        val service: Calendar,
        val calendarId: String,
        val event: Event
    ) : AsyncTask<Void, Void, Event?>() {

        override fun doInBackground(vararg params: Void?): Event? {
            return try {
                service.events().insert(calendarId, event).execute()
            } catch (e: Exception) {
                e.printStackTrace()
                cancel(true)
                null
            }
        }

        override fun onPreExecute() {
            super.onPreExecute()
            mProgress!!.show()
        }

        override fun onPostExecute(result: Event?) {
            super.onPostExecute(result)
            Log.d("ProfileFragment", result.toString())
            Toast.makeText(requireContext(), "Event created successfully", Toast.LENGTH_SHORT).show()
            mProgress!!.hide()
        }

        override fun onCancelled() {
            super.onCancelled()
            mProgress!!.hide()
        }
    }

    fun showGooglePlayServicesAvailabilityErrorDialog(connectionStatusCode: Int) {
        val apiAvailability = GoogleApiAvailability.getInstance()
        val dialog = apiAvailability.getErrorDialog(
            requireActivity(),
            connectionStatusCode,
            REQUEST_GOOGLE_PLAY_SERVICES)
        dialog?.show()
    }
    private fun unlinkGoogleAccount() {
        // Xóa tài khoản đã lưu trong SharedPreferences
        val settings = requireActivity().getPreferences(Context.MODE_PRIVATE)
        val editor = settings.edit()
        editor.remove(PREF_ACCOUNT_NAME)
        editor.apply()

        // Thiết lập lại thông tin xác thực
        mCredential?.setSelectedAccountName(null)
        mCredential = null
        //userViewModel.setCredential(null)
        initCredentials()

        showToast(requireContext(), "Đã thoát liên kết tài khoản Google")
        userViewModel.setGoogleLoggedIn(false)
    }


    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        when (requestCode) {
            REQUEST_GOOGLE_PLAY_SERVICES -> {
                if (resultCode != Activity.RESULT_OK) {
//                    binding.txtOut.text = "This app requires Google Play Services. Please install Google Play Services on your device and relaunch this app."
                    showToast(requireContext(), "This app requires Google Play Services. Please install Google Play Services on your device and relaunch this app.")
                } else {
                    getResultsFromApi()
                }
            }
            REQUEST_ACCOUNT_PICKER -> {
                if (resultCode == Activity.RESULT_OK && data != null && data.extras != null) {
                    val accountName = data.getStringExtra(AccountManager.KEY_ACCOUNT_NAME)
                    if (accountName != null) {
                        val settings = requireActivity().getPreferences(Context.MODE_PRIVATE)
                        val editor = settings.edit()
                        editor.putString(PREF_ACCOUNT_NAME, accountName)
                        editor.apply()
                        mCredential!!.selectedAccountName = accountName
                        userViewModel.setCredential(mCredential)

                        getResultsFromApi()
                    }
                }
            }
            REQUEST_AUTHORIZATION -> {
                if (resultCode == Activity.RESULT_OK) {
                    getResultsFromApi()
                }
            }
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        EasyPermissions.onRequestPermissionsResult(requestCode, permissions, grantResults, this)
    }
    private fun createCalendarEvent(event: com.hoangdoviet.finaldoan.model.Event) {
        val dateFormat = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())

        val startDateTimeString = "${event.date} ${event.timeStart}"
        val endDateTimeString = "${event.date} ${event.timeEnd}"

        val startDate = dateFormat.parse(startDateTimeString)
        val endDate = dateFormat.parse(endDateTimeString)

        val startDateTime = DateTime(startDate)
        val endDateTime = DateTime(endDate)

        val event = com.google.api.services.calendar.model.Event()
            .setSummary(event.title)
            .setLocation("Hà Nội, Việt Nam")
            .setDescription("Đặt lịch bởi ứng dụng HoangLich")
        val start = EventDateTime()
            .setDateTime(startDateTime)
            .setTimeZone("Asia/Ho_Chi_Minh")
        event.start = start
        val end = EventDateTime()
            .setDateTime(endDateTime)
            .setTimeZone("Asia/Ho_Chi_Minh")
        event.end = end

        val reminderOverrides = listOf(
            EventReminder().setMethod("email").setMinutes(24 * 60),
            EventReminder().setMethod("popup").setMinutes(10))

        val reminders = com.google.api.services.calendar.model.Event.Reminders()
            .setUseDefault(false)
            .setOverrides(reminderOverrides)
        event.reminders = reminders

        val calendarId = "primary"
        val transport = AndroidHttp.newCompatibleTransport()
        val jsonFactory = JacksonFactory.getDefaultInstance()
        val service = com.google.api.services.calendar.Calendar.Builder(
            transport, jsonFactory, mCredential)
            .setApplicationName("Google Calendar API Android Quickstart")
            .build()
        Log.d("checkkklogin", mCredential.toString())
        EventCreator1(service, calendarId, event).execute()
    }
    //AsyncTask này xử lý việc tạo sự kiện lịch trong nền, hiển thị và ẩn hộp thoại tiến trình khi cần thiết.
    private inner class EventCreator1 internal constructor(val service: com.google.api.services.calendar.Calendar, //ược sử dụng để truy cập các phương thức của Google Calendar API.
                                                          val calendarId: String, //D của lịch trên Google Calendar mà sự kiện sẽ được thêm vào. Thông thường, giá trị này là "primary" cho lịch chính của người dùng.
                                                          val event: Event,
                                                         )  : //Thông tin xác thực (GoogleAccountCredential) được sử dụng để xác thực và ủy quyền các yêu cầu API.
        AsyncTask<Void, Void, Event?>() {
        //AsyncTask được sử dụng để thực hiện các tác vụ nền mà không làm gián đoạn giao diện người dùng.

        override fun doInBackground(vararg params: Void?): Event? {
            return try {
                service.events().insert(calendarId, event).execute()
            } catch (e: Exception) {
                e.printStackTrace()
                cancel(true)
                null
            }
        }

        override fun onPreExecute() { // Phương thức này chạy trên luồng giao diện người dùng trước khi
            super.onPreExecute()
            mProgress!!.show()
        }

        override fun onPostExecute(result: Event?) {
            //Phương thức này chạy trên luồng giao diện người dùng sau khi doInBackground hoàn thành. Nó nhận kết quả là một đối tượng Event.
            super.onPostExecute(result)
            Log.d("MainActivity", result.toString())
            Toast.makeText(requireContext(), "Event created successfully", Toast.LENGTH_SHORT).show()
            mProgress!!.hide()
        }

        override fun onCancelled() {
            super.onCancelled()
            mProgress!!.hide()
        }
    }
}

