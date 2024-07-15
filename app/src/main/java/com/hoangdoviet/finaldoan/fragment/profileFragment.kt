package com.hoangdoviet.finaldoan.fragment

import android.accounts.AccountManager
import android.app.Activity
import android.app.ProgressDialog
import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.net.ConnectivityManager
import android.net.Uri
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
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
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
        if(mAuth.currentUser != null){
            binding.lienketGoogle.visibility= View.VISIBLE
            binding.logout.visibility = View.VISIBLE
            binding.login.visibility = View.GONE
            binding.deleteAllTask.visibility = View.VISIBLE
            binding.deleteAllEvent.visibility= View.VISIBLE
        }
        binding.login.setOnClickListener {
            val intent = Intent(activity, AuthActivity::class.java)
            startActivity(intent)
        }
        binding.logout.setOnClickListener {
            if (mAuth.currentUser != null) {
                mAuth.signOut()
                showToast(requireContext(), "Thoát tài khoản thành công")
                updateUI()
            }
            binding.logout.visibility = View.GONE
            binding.login.visibility = View.VISIBLE
            binding.username.text="Cá nhân"
            binding.lienketGoogle.visibility = View.GONE
            binding.logoutGoogle.visibility = View.GONE
        }

        binding.mailDev.setOnClickListener {
            val recipient = "hoangdoviet27042002@gmail.com"
            val subject = "Góp ý ứng dụng LichThongMinh"

            val mailIntent = Intent(Intent.ACTION_SENDTO).apply {
                data = Uri.parse("mailto:")
                putExtra(Intent.EXTRA_EMAIL, arrayOf(recipient))
                putExtra(Intent.EXTRA_SUBJECT, subject)
            }

            try {
                startActivity(Intent.createChooser(mailIntent, "Choose an email client"))
            } catch (e: ActivityNotFoundException) {
                Toast.makeText(context, "Không có ứng dụng mail trên điện thoại", Toast.LENGTH_SHORT).show()
            }
        }
        binding.deleteAllEvent.setOnClickListener {
            deleteAllEvents()
        }
        binding.deleteAllTask.setOnClickListener {
            deleteAllTasks()
        }

    }

    private fun updateUIGoogle(isGoogleLoggedIn: Boolean) {
        if (isGoogleLoggedIn) {
            binding.txtlienketGoogle.text = "Liên kết tài khoản : \n"+ (mCredential!!.selectedAccountName)
        }

    }

    private fun initView() {
        mProgress = ProgressDialog(requireContext())
        mProgress!!.setMessage("Loading...")

        binding.lienketGoogle.setOnClickListener {
            getResultsFromApi()

        }

        binding.logoutGoogle.setOnClickListener {
            unlinkGoogleAccount()
            binding.txtlienketGoogle.text = "Liên kết Google Calendar API"
        }
    }
    private fun deleteAllEvents() {
        val userId = FirebaseAuth.getInstance().currentUser?.uid ?: return
        val db = FirebaseFirestore.getInstance()

        db.collection("User").document(userId).get()
            .addOnSuccessListener { document ->
                if (document.exists()) {
                    val eventIDs = document.get("eventID") as? List<String> ?: emptyList()
                    val batch = db.batch()

                    for (eventId in eventIDs) {
                        val eventRef = db.collection("Events").document(eventId)
                        batch.delete(eventRef)
                    }
                    val userRef = db.collection("User").document(userId)
                    batch.update(userRef, "eventID", emptyList<String>())
                    batch.commit().addOnCompleteListener {
                        if (it.isSuccessful) {
                            Toast.makeText(context, "Xoá tất cả sự kiện thành công", Toast.LENGTH_SHORT).show()
                        } else {
                            Toast.makeText(context, "Có lỗi xảy ra khi xoá sự kiện", Toast.LENGTH_SHORT).show()
                        }
                    }
                }
            }
            .addOnFailureListener {
                Toast.makeText(context, "Failed to load user events", Toast.LENGTH_SHORT).show()
            }
    }

    private fun deleteAllTasks() {
        val userId = FirebaseAuth.getInstance().currentUser?.uid ?: return
        val db = FirebaseFirestore.getInstance()
        db.collection("User").document(userId).get()
            .addOnSuccessListener { document ->
                if (document.exists()) {
                    val taskIds = document.get("taskIds") as? List<String> ?: emptyList()
                    val batch = db.batch()
                    for (taskId in taskIds) {
                        val taskRef = db.collection("Tasks").document(taskId)
                        batch.delete(taskRef)
                    }
                    val userRef = db.collection("User").document(userId)
                    batch.update(userRef, "taskIds", emptyList<String>())
                    var tasksToRemove = taskIds.size
                    var tasksRemoved = 0
                    for (taskId in taskIds) {
                        db.collection("TasksByDate").whereArrayContains("taskIds", taskId).get()
                            .addOnSuccessListener { querySnapshot ->
                                for (document in querySnapshot.documents) {
                                    batch.update(document.reference, "taskIds", FieldValue.arrayRemove(taskId))
                                }
                                tasksRemoved++
                                if (tasksRemoved == tasksToRemove) {
                                    batch.commit().addOnCompleteListener {
                                        if (it.isSuccessful) {
                                            Toast.makeText(context, "Xoá tất cả nhiệm vụ thành công", Toast.LENGTH_SHORT).show()
                                        } else {
                                            Toast.makeText(context, "Có lỗi xảy ra khi xoá nhiệm vụ", Toast.LENGTH_SHORT).show()
                                        }
                                    }
                                }
                            }
                            .addOnFailureListener {
                                tasksRemoved++
                                if (tasksRemoved == tasksToRemove) {
                                    batch.commit().addOnCompleteListener {
                                        if (it.isSuccessful) {
                                            Toast.makeText(context, "Xoá tất cả nhiệm vụ thành công", Toast.LENGTH_SHORT).show()
                                        } else {
                                            Toast.makeText(context, "Có lỗi xảy ra khi xoá nhiệm vụ", Toast.LENGTH_SHORT).show()
                                        }
                                    }
                                }
                            }
                    }

                    if (tasksToRemove == 0) {
                        batch.commit().addOnCompleteListener {
                            if (it.isSuccessful) {
                                Toast.makeText(context, "Xoá tất cả nhiệm vụ thành công", Toast.LENGTH_SHORT).show()
                            } else {
                                Toast.makeText(context, "Có lỗi xảy ra khi xoá nhiệm vụ", Toast.LENGTH_SHORT).show()
                            }
                        }
                    }
                }
            }
            .addOnFailureListener {
                Toast.makeText(context, "Failed to load user tasks", Toast.LENGTH_SHORT).show()
            }
    }



    private fun updateUI() {
        if (mAuth.currentUser != null) {
            //binding.btnLogout.visibility = View.VISIBLE
            binding.username.text = "Xin chào, "+ mAuth.currentUser?.displayName ?: "Cá nhân"
        }
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
            //binding.btnLogoutGoogle.isEnabled = true
            binding.logoutGoogle.visibility = View.VISIBLE

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




    private fun initCredentials() {
        mCredential = GoogleAccountCredential.usingOAuth2(
            requireContext().applicationContext,
            arrayListOf(CalendarScopes.CALENDAR)
        ).setBackOff(ExponentialBackOff())
        val accountName = requireActivity().getPreferences(Context.MODE_PRIVATE)
            .getString(PREF_ACCOUNT_NAME, null)
        if (accountName != null) {
            mCredential?.selectedAccountName = accountName
            binding.logoutGoogle.visibility = View.VISIBLE
        }
        userViewModel.setCredential(mCredential)
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
        val settings = requireActivity().getPreferences(Context.MODE_PRIVATE)
        val editor = settings.edit()
        editor.remove(PREF_ACCOUNT_NAME)
        editor.apply()
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
}

