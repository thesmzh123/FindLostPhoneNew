package device.spotter.finder.appss.fragments

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.text.TextUtils
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.Window
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.find.lost.app.phone.utils.InternetConnection
import com.find.lost.app.phone.utils.SharedPrefUtils
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import device.spotter.finder.appss.R
import device.spotter.finder.appss.activities.BaseActivity
import device.spotter.finder.appss.utils.Constants.RC_SIGN_IN
import device.spotter.finder.appss.utils.Constants.TAGI
import kotlinx.android.synthetic.main.enter_phone_num_update_layout.view.*
import kotlinx.android.synthetic.main.fragment_profile.view.*


class ProfileFragment : BaseFragment() {

    private var mGoogleSignInClient: GoogleSignInClient? = null
    private var mMenuButtonListener: MenuButtonListener? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // This callback will only be called when MyFragment is at least Started.
        val callback: OnBackPressedCallback =
            object : OnBackPressedCallback(true /* enabled by default */) {
                override fun handleOnBackPressed() {
                    // Handle the back button event
                    baseContext!!.navigateFragment(R.id.nav_home)
                }
            }
        requireActivity().onBackPressedDispatcher.addCallback(this, callback)

        // The callback can be enabled or disabled here or in handleOnBackPressed()
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        root = inflater.inflate(R.layout.fragment_profile, container, false)
        layoutNumber = root!!.layoutNumber
        num = root!!.num
        mMenuButtonListener?.menuEnable(false)
        //Then we need a GoogleSignInOptions object
        //And we need to build it as below
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.default_web_client_id))
            .requestEmail()
            .build()
        //Then we will get the GoogleSignInClient object from GoogleSignIn class
        mGoogleSignInClient = GoogleSignIn.getClient(requireActivity(), gso)

        root!!.login_with_google.setOnClickListener {
            signIn()
        }
        if (isLoggedIn()) {
            changeUi()
        } else {
            root!!.signInBlock.visibility = View.VISIBLE
            root!!.profileBlock.visibility = View.GONE
            root!!.layoutNumber.visibility = View.GONE

        }
        root!!.signOut.setOnClickListener {
            FirebaseAuth.getInstance().signOut()
            root!!.signInBlock.visibility = View.VISIBLE
            root!!.profileBlock.visibility = View.GONE
            mainContext!!.updateNavView()
            baseContext!!.changeMenu()
        }

        root!!.updateBtn.setOnClickListener {
            val factory = LayoutInflater.from(requireActivity())
            @SuppressLint("InflateParams") val deleteDialogView: View =
                factory.inflate(R.layout.enter_phone_num_update_layout, null)
            (context as BaseActivity).deleteDialog = if (Build.VERSION.SDK_INT > 23) {

                MaterialAlertDialogBuilder(requireActivity()).create()
            } else {
                AlertDialog.Builder(requireActivity()).create()
            }

            (context as BaseActivity).deleteDialog!!.requestWindowFeature(Window.FEATURE_NO_TITLE)
            (context as BaseActivity).deleteDialog!!.setView(deleteDialogView)
            (context as BaseActivity).deleteDialog!!.setCancelable(false)
//        deleteDialogView.ccp1.registerCarrierNumberEditText(deleteDialogView.editText_carrierNumber1)
            deleteDialogView.mainBtnNUmCancel.setOnClickListener {
                (context as BaseActivity).deleteDialog!!.dismiss()
            }
            deleteDialogView.mainBtnNUm.setOnClickListener {
                if (TextUtils.isEmpty(deleteDialogView.editText_carrierNumber1.text)) {
                    showToast(getString(R.string.fill_the_field))
                } else {
                    if (InternetConnection().checkConnection(requireActivity())) {
                        (context as BaseActivity).showDialog(getString(R.string.sending_you_verification_code))
                        (context as BaseActivity).getNum =
                            deleteDialogView.ccpNUm.selectedCountryCode + deleteDialogView.editText_carrierNumber1.text.toString()
                        (context as BaseActivity).sendVerificationCode(deleteDialogView.ccpNUm.selectedCountryCode + deleteDialogView.editText_carrierNumber1.text.toString())
//                        updatePhoneNumber(deleteDialogView.editText_carrierNumber1.text.toString())

                        (context as BaseActivity).deleteDialog!!.dismiss()
                    } else {
                        showToast(getString(R.string.no_internet))
                    }
                }
            }

            (context as BaseActivity).deleteDialog!!.show()
            (context as BaseActivity).deleteDialog!!.window!!.decorView.setBackgroundResource(
                android.R.color.transparent
            )

        }
        return root!!
    }


    @SuppressLint("SetTextI18n")
    private fun changeUi() {
        root!!.signInBlock.visibility = View.GONE
        root!!.profileBlock.visibility = View.VISIBLE
        root!!.name.text = auth.currentUser?.displayName
        root!!.email.text = auth.currentUser?.email
        Glide.with(this).load(auth.currentUser?.photoUrl)
            .into(root!!.profileImage)
        val phone = SharedPrefUtils.getStringData(requireActivity(), "phoneNum").toString()
        if (phone.isNotEmpty() || !phone.equals("null", true)) {
            layoutNumber!!.visibility = View.VISIBLE
            num!!.text = "Your number is $phone"
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        (activity as AppCompatActivity).supportActionBar?.title = ""
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        if (context is MenuButtonListener) {
            mMenuButtonListener = context
        }
    }

    override fun onDetach() {
        super.onDetach()
        if (mMenuButtonListener != null) {
            mMenuButtonListener = null
        }
    }

    /**
     * Use this method to signIn to google login
     */
    private fun signIn() {
        val signInIntent = mGoogleSignInClient!!.signInIntent
        startActivityForResult(signInIntent, RC_SIGN_IN)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == RC_SIGN_IN) {
            val task = GoogleSignIn.getSignedInAccountFromIntent(data)
            try {
                // Google Sign In was successful, authenticate with Firebase
                val account = task.getResult(ApiException::class.java)
                firebaseAuthWithGoogle(account!!)
            } catch (e: Exception) {
                // Google Sign In failed, update UI appropriately
                Log.w(TAGI, "Google sign in failed", e)
                showToast(getString(R.string.unable_to_sign_in))

            }

        }
    }

    private fun firebaseAuthWithGoogle(account: GoogleSignInAccount) {
        Log.d(TAGI, "firebaseAuthWithGoogle:" + account.id!!)
        showDialog(getString(R.string.authenticating_user))

        val credential = GoogleAuthProvider.getCredential(account.idToken, null)
        auth.signInWithCredential(credential)
            .addOnCompleteListener(requireActivity()) { task ->
                try {
                    if (task.isSuccessful) {

                        // Sign in success, update UI with the signed-in user's information
                        Log.d(TAGI, "signInWithCredential:success")
                        val user = auth.currentUser
                        Log.d(TAGI, user?.displayName + "\n")
                        Log.d(TAGI, "firebaseAuthWithGoogle: " + user?.uid)
                        if (user != null) {
                            changeUi()
                            mainContext!!.updateNavView()
                            baseContext!!.changeMenu()
                            insertDataToDb(user.displayName, user.email)
                        } else {
                            root!!.signInBlock.visibility = View.VISIBLE
                            root!!.profileBlock.visibility = View.GONE
                        }
                    } else {
                        // If sign in fails, display a message to the user.
                        Log.w(TAGI, "signInWithCredential:failure", task.exception)
                        showToast(getString(R.string.authentication_failed))
                        hideDialog()
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                }

            }

    }

    override fun onStop() {
        super.onStop()
        mMenuButtonListener!!.menuEnable(true)
    }

    interface MenuButtonListener {
        fun menuEnable(isEnable: Boolean)
    }

}