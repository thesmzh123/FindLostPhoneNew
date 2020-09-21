package lost.phone.finder.app.online.finder.utils

import android.annotation.SuppressLint
import android.content.ContentProviderOperation
import android.content.Context
import android.content.OperationApplicationException
import android.os.Handler
import android.os.Looper
import android.os.RemoteException
import android.provider.ContactsContract
import android.provider.ContactsContract.CommonDataKinds
import android.util.Log
import android.widget.Toast
import ezvcard.Ezvcard
import ezvcard.VCard
import ezvcard.parameter.AddressType
import ezvcard.parameter.EmailType
import ezvcard.parameter.TelephoneType
import lost.phone.finder.app.online.finder.R
import lost.phone.finder.app.online.finder.utils.Constants.TAGI
import java.io.File
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*

object RestoreContacts {
    var context: Context? = null
    fun restoreData(path: String?, context: Context) {
        this.context = context
        val file = File(path!!)
        try {
            val vcl = Ezvcard.parse(file).all()
            if (vcl.size == 0) {
                Log.d(TAGI, "restoreData: Invalid/empty vCard file.")
                Handler(Looper.getMainLooper()).post {
                    showToast("Invalid/empty vCard file.")
                }
                return
            }
            Log.d(
                TAGI,
                "restoreData: Parsing & saving contacts from vCard files..." + 0 + vcl.size
            )
            parseFile(vcl, context)
            /*    if (data.getBooleanExtra("delete", false))
                file.delete();*/
            Log.d(
                TAGI,
                "Finished restoring contacts from vCard files."
            )
            Handler(Looper.getMainLooper()).post {
                showToast(context.getString(R.string.contacts_success_r))
            }

        } catch (e: IOException) {
            Log.e("CBR", e.message!!)
            Log.d(TAGI, "An error occurred while reading vCard files.")
        } catch (e: RemoteException) {
            Log.e("CBR", e.message!!)
            Log.d(TAGI, "An error occurred while saving contacts.")
        } catch (e: OperationApplicationException) {
            Log.e("CBR", e.message!!)
            Log.d(TAGI, "An error occurred while saving contacts.")
        }
    }

    @Throws(RemoteException::class, OperationApplicationException::class)
    private fun parseFile(
        vcl: List<VCard>,
        context: Context
    ) {
        for (vc in vcl) {
            val op = parseVCard(vc)
            context.contentResolver.applyBatch(ContactsContract.AUTHORITY, op)
        }
    }

    @SuppressLint("SimpleDateFormat")
    private fun parseVCard(vc: VCard): ArrayList<ContentProviderOperation> {
        val ops =
            ArrayList<ContentProviderOperation>()
        ops.add(
            ContentProviderOperation.newInsert(
                ContactsContract.RawContacts.CONTENT_URI
            )
                .withValue(ContactsContract.RawContacts.ACCOUNT_TYPE, null)
                .withValue(ContactsContract.RawContacts.ACCOUNT_NAME, null)
                .build()
        )
        if (vc.structuredName != null || vc.formattedName != null) {
            val sn = vc.structuredName
            val fn = vc.formattedName
            var cpo = ContentProviderOperation.newInsert(
                ContactsContract.Data.CONTENT_URI
            )
                .withValueBackReference(ContactsContract.Data.RAW_CONTACT_ID, 0)
                .withValue(
                    ContactsContract.Data.MIMETYPE,
                    CommonDataKinds.StructuredName.CONTENT_ITEM_TYPE
                )
            if (sn != null) {
                cpo = cpo.withValue(
                    CommonDataKinds.StructuredName.PREFIX,
                    if (sn.prefixes.isEmpty()) "" else sn.prefixes[0]
                )
                    .withValue(CommonDataKinds.StructuredName.GIVEN_NAME, sn.given)
                    .withValue(CommonDataKinds.StructuredName.FAMILY_NAME, sn.family)
                    .withValue(
                        CommonDataKinds.StructuredName.MIDDLE_NAME,
                        if (sn.additionalNames.isEmpty()) "" else sn.additionalNames[0]
                    )
                    .withValue(
                        CommonDataKinds.StructuredName.SUFFIX,
                        if (sn.suffixes.isEmpty()) "" else sn.suffixes[0]
                    )
                if (vc.getExtendedProperty("X-PHONETIC-LAST-NAME") != null) cpo = cpo.withValue(
                    CommonDataKinds.StructuredName.PHONETIC_FAMILY_NAME,
                    vc.getExtendedProperty("X-PHONETIC-LAST-NAME").value
                )
                if (vc.getExtendedProperty("X-PHONETIC-FIRST-NAME") != null) cpo = cpo.withValue(
                    CommonDataKinds.StructuredName.PHONETIC_GIVEN_NAME,
                    vc.getExtendedProperty("X-PHONETIC-FIRST-NAME").value
                )
                if (vc.getExtendedProperty("X-PHONETIC-MIDDLE-NAME") != null) cpo = cpo.withValue(
                    CommonDataKinds.StructuredName.PHONETIC_MIDDLE_NAME,
                    vc.getExtendedProperty("X-PHONETIC-MIDDLE-NAME").value
                )
            }
            if (fn != null) {
                cpo = cpo.withValue(CommonDataKinds.StructuredName.DISPLAY_NAME, fn.value)
            }
            ops.add(cpo.build())
        }
        for (tele in vc.telephoneNumbers) {
            var cpo = ContentProviderOperation.newInsert(
                ContactsContract.Data.CONTENT_URI
            )
                .withValueBackReference(ContactsContract.Data.RAW_CONTACT_ID, 0)
                .withValue(
                    ContactsContract.Data.MIMETYPE,
                    CommonDataKinds.Phone.CONTENT_ITEM_TYPE
                )
                .withValue(CommonDataKinds.Phone.NUMBER, tele.text)
            cpo = when {
                tele.types.contains(TelephoneType.HOME) -> cpo.withValue(
                    CommonDataKinds.Phone.TYPE,
                    CommonDataKinds.Phone.TYPE_HOME
                )
                tele.types.contains(TelephoneType.WORK) -> cpo.withValue(
                    CommonDataKinds.Phone.TYPE,
                    CommonDataKinds.Phone.TYPE_WORK
                )
                tele.types.contains(TelephoneType.CELL) -> cpo.withValue(
                    CommonDataKinds.Phone.TYPE,
                    CommonDataKinds.Phone.TYPE_MOBILE
                )
                tele.types.contains(TelephoneType.FAX) -> cpo.withValue(
                    CommonDataKinds.Phone.TYPE,
                    CommonDataKinds.Phone.TYPE_FAX_WORK
                )
                tele.types.contains(TelephoneType.PAGER) -> cpo.withValue(
                    CommonDataKinds.Phone.TYPE,
                    CommonDataKinds.Phone.TYPE_PAGER
                )
                tele.types.contains(TelephoneType.CAR) -> cpo.withValue(
                    CommonDataKinds.Phone.TYPE,
                    CommonDataKinds.Phone.TYPE_CAR
                )
                else -> cpo.withValue(CommonDataKinds.Phone.TYPE, CommonDataKinds.Phone.TYPE_OTHER)
            }
            ops.add(cpo.build())
        }
        for (email in vc.emails) {
            var cpo = ContentProviderOperation.newInsert(
                ContactsContract.Data.CONTENT_URI
            )
                .withValueBackReference(ContactsContract.Data.RAW_CONTACT_ID, 0)
                .withValue(
                    ContactsContract.Data.MIMETYPE,
                    CommonDataKinds.Email.CONTENT_ITEM_TYPE
                )
                .withValue(CommonDataKinds.Email.DATA, email.value)
            cpo = when {
                email.types.contains(EmailType.WORK) -> cpo.withValue(
                    CommonDataKinds.Email.TYPE,
                    CommonDataKinds.Email.TYPE_WORK
                )
                email.types.contains(EmailType.HOME) -> cpo.withValue(
                    CommonDataKinds.Email.TYPE,
                    CommonDataKinds.Email.TYPE_HOME
                )
                else -> cpo.withValue(CommonDataKinds.Email.TYPE, CommonDataKinds.Email.TYPE_OTHER)
            }
            ops.add(cpo.build())
        }
        for (im in vc.getExtendedProperties("X-GOOGLE-TALK")) {
            val cpo = ContentProviderOperation.newInsert(
                ContactsContract.Data.CONTENT_URI
            )
                .withValueBackReference(ContactsContract.Data.RAW_CONTACT_ID, 0)
                .withValue(
                    ContactsContract.Data.MIMETYPE,
                    CommonDataKinds.Im.CONTENT_ITEM_TYPE
                )
                .withValue(CommonDataKinds.Im.DATA, im.value)
                .withValue(CommonDataKinds.Im.PROTOCOL, CommonDataKinds.Im.PROTOCOL_GOOGLE_TALK)
            ops.add(cpo.build())
        }
        for (im in vc.getExtendedProperties("X-AIM")) {
            val cpo = ContentProviderOperation.newInsert(
                ContactsContract.Data.CONTENT_URI
            )
                .withValueBackReference(ContactsContract.Data.RAW_CONTACT_ID, 0)
                .withValue(
                    ContactsContract.Data.MIMETYPE,
                    CommonDataKinds.Im.CONTENT_ITEM_TYPE
                )
                .withValue(CommonDataKinds.Im.DATA, im.value)
                .withValue(CommonDataKinds.Im.PROTOCOL, CommonDataKinds.Im.PROTOCOL_AIM)
            ops.add(cpo.build())
        }
        for (im in vc.getExtendedProperties("X-MSN")) {
            val cpo = ContentProviderOperation.newInsert(
                ContactsContract.Data.CONTENT_URI
            )
                .withValueBackReference(ContactsContract.Data.RAW_CONTACT_ID, 0)
                .withValue(
                    ContactsContract.Data.MIMETYPE,
                    CommonDataKinds.Im.CONTENT_ITEM_TYPE
                )
                .withValue(CommonDataKinds.Im.DATA, im.value)
                .withValue(CommonDataKinds.Im.PROTOCOL, CommonDataKinds.Im.PROTOCOL_MSN)
            ops.add(cpo.build())
        }
        for (im in vc.getExtendedProperties("X-YAHOO")) {
            val cpo = ContentProviderOperation.newInsert(
                ContactsContract.Data.CONTENT_URI
            )
                .withValueBackReference(ContactsContract.Data.RAW_CONTACT_ID, 0)
                .withValue(
                    ContactsContract.Data.MIMETYPE,
                    CommonDataKinds.Im.CONTENT_ITEM_TYPE
                )
                .withValue(CommonDataKinds.Im.DATA, im.value)
                .withValue(CommonDataKinds.Im.PROTOCOL, CommonDataKinds.Im.PROTOCOL_YAHOO)
            ops.add(cpo.build())
        }
        for (im in vc.getExtendedProperties("X-SKYPE-USERNAME")) {
            val cpo = ContentProviderOperation.newInsert(
                ContactsContract.Data.CONTENT_URI
            )
                .withValueBackReference(ContactsContract.Data.RAW_CONTACT_ID, 0)
                .withValue(
                    ContactsContract.Data.MIMETYPE,
                    CommonDataKinds.Im.CONTENT_ITEM_TYPE
                )
                .withValue(CommonDataKinds.Im.DATA, im.value)
                .withValue(CommonDataKinds.Im.PROTOCOL, CommonDataKinds.Im.PROTOCOL_SKYPE)
            ops.add(cpo.build())
        }
        for (im in vc.getExtendedProperties("X-QQ")) {
            val cpo = ContentProviderOperation.newInsert(
                ContactsContract.Data.CONTENT_URI
            )
                .withValueBackReference(ContactsContract.Data.RAW_CONTACT_ID, 0)
                .withValue(
                    ContactsContract.Data.MIMETYPE,
                    CommonDataKinds.Im.CONTENT_ITEM_TYPE
                )
                .withValue(CommonDataKinds.Im.DATA, im.value)
                .withValue(CommonDataKinds.Im.PROTOCOL, CommonDataKinds.Im.PROTOCOL_QQ)
            ops.add(cpo.build())
        }
        for (im in vc.getExtendedProperties("X-ICQ")) {
            val cpo = ContentProviderOperation.newInsert(
                ContactsContract.Data.CONTENT_URI
            )
                .withValueBackReference(ContactsContract.Data.RAW_CONTACT_ID, 0)
                .withValue(
                    ContactsContract.Data.MIMETYPE,
                    CommonDataKinds.Im.CONTENT_ITEM_TYPE
                )
                .withValue(CommonDataKinds.Im.DATA, im.value)
                .withValue(CommonDataKinds.Im.PROTOCOL, CommonDataKinds.Im.PROTOCOL_ICQ)
            ops.add(cpo.build())
        }
        for (im in vc.getExtendedProperties("X-JABBER")) {
            val cpo = ContentProviderOperation.newInsert(
                ContactsContract.Data.CONTENT_URI
            )
                .withValueBackReference(ContactsContract.Data.RAW_CONTACT_ID, 0)
                .withValue(
                    ContactsContract.Data.MIMETYPE,
                    CommonDataKinds.Im.CONTENT_ITEM_TYPE
                )
                .withValue(CommonDataKinds.Im.DATA, im.value)
                .withValue(CommonDataKinds.Im.PROTOCOL, CommonDataKinds.Im.PROTOCOL_JABBER)
            ops.add(cpo.build())
        }
        for (ad in vc.addresses) {
            var cpo = ContentProviderOperation.newInsert(
                ContactsContract.Data.CONTENT_URI
            )
                .withValueBackReference(ContactsContract.Data.RAW_CONTACT_ID, 0)
                .withValue(
                    ContactsContract.Data.MIMETYPE,
                    CommonDataKinds.StructuredPostal.CONTENT_ITEM_TYPE
                )
                .withValue(CommonDataKinds.StructuredPostal.STREET, ad.streetAddress)
                .withValue(CommonDataKinds.StructuredPostal.POBOX, ad.poBox)
                .withValue(CommonDataKinds.StructuredPostal.CITY, ad.locality)
                .withValue(CommonDataKinds.StructuredPostal.REGION, ad.region)
                .withValue(CommonDataKinds.StructuredPostal.POSTCODE, ad.postalCode)
                .withValue(CommonDataKinds.StructuredPostal.COUNTRY, ad.country)
            cpo = when {
                ad.types.contains(AddressType.HOME) -> cpo.withValue(
                    CommonDataKinds.StructuredPostal.TYPE,
                    CommonDataKinds.StructuredPostal.TYPE_HOME
                )
                ad.types.contains(AddressType.WORK) -> cpo.withValue(
                    CommonDataKinds.StructuredPostal.TYPE,
                    CommonDataKinds.StructuredPostal.TYPE_WORK
                )
                else -> cpo.withValue(
                    CommonDataKinds.StructuredPostal.TYPE,
                    CommonDataKinds.StructuredPostal.TYPE_OTHER
                )
            }
            ops.add(cpo.build())
        }
        val t = vc.titles
        for ((i, org) in vc.organizations.withIndex()) {
            var cpo = ContentProviderOperation.newInsert(
                ContactsContract.Data.CONTENT_URI
            )
                .withValueBackReference(ContactsContract.Data.RAW_CONTACT_ID, 0)
                .withValue(
                    ContactsContract.Data.MIMETYPE,
                    CommonDataKinds.Organization.CONTENT_ITEM_TYPE
                )
                .withValue(CommonDataKinds.Organization.COMPANY, org.values[0])
            if (t.size > i) {
                cpo = cpo.withValue(CommonDataKinds.Organization.TITLE, t[i].value)
            }
            ops.add(cpo.build())
        }
        if (vc.nickname != null) {
            val cpo = ContentProviderOperation.newInsert(
                ContactsContract.Data.CONTENT_URI
            )
                .withValueBackReference(ContactsContract.Data.RAW_CONTACT_ID, 0)
                .withValue(
                    ContactsContract.Data.MIMETYPE,
                    CommonDataKinds.Nickname.CONTENT_ITEM_TYPE
                )
                .withValue(
                    CommonDataKinds.Nickname.NAME,
                    if (vc.nickname.values.isEmpty()) "" else vc.nickname.values[0]
                )
                .withValue(CommonDataKinds.Nickname.TYPE, CommonDataKinds.Nickname.TYPE_DEFAULT)
            ops.add(cpo.build())
        }
        if (vc.notes != null && vc.notes.isNotEmpty()) {
            val cpo = ContentProviderOperation.newInsert(
                ContactsContract.Data.CONTENT_URI
            )
                .withValueBackReference(ContactsContract.Data.RAW_CONTACT_ID, 0)
                .withValue(
                    ContactsContract.Data.MIMETYPE,
                    CommonDataKinds.Note.CONTENT_ITEM_TYPE
                )
                .withValue(CommonDataKinds.Note.NOTE, vc.notes[0].value)
            ops.add(cpo.build())
        }
        for (url in vc.urls) {
            val cpo = ContentProviderOperation.newInsert(
                ContactsContract.Data.CONTENT_URI
            )
                .withValueBackReference(ContactsContract.Data.RAW_CONTACT_ID, 0)
                .withValue(
                    ContactsContract.Data.MIMETYPE,
                    CommonDataKinds.Website.CONTENT_ITEM_TYPE
                )
                .withValue(CommonDataKinds.Website.URL, url.value)
            ops.add(cpo.build())
        }
        if (vc.birthday != null) {
            val date = vc.birthday.date
            val d = SimpleDateFormat("dd.MM.yyyy").format(date)
            val cpo = ContentProviderOperation.newInsert(
                ContactsContract.Data.CONTENT_URI
            )
                .withValueBackReference(ContactsContract.Data.RAW_CONTACT_ID, 0)
                .withValue(
                    ContactsContract.Data.MIMETYPE,
                    CommonDataKinds.Event.CONTENT_ITEM_TYPE
                )
                .withValue(CommonDataKinds.Event.START_DATE, d)
                .withValue(CommonDataKinds.Event.TYPE, CommonDataKinds.Event.TYPE_BIRTHDAY)
            ops.add(cpo.build())
        }
        if (vc.anniversary != null) {
            val date = vc.anniversary.date
            val d = SimpleDateFormat("dd.MM.yyyy").format(date)
            val cpo = ContentProviderOperation.newInsert(
                ContactsContract.Data.CONTENT_URI
            )
                .withValueBackReference(ContactsContract.Data.RAW_CONTACT_ID, 0)
                .withValue(
                    ContactsContract.Data.MIMETYPE,
                    CommonDataKinds.Event.CONTENT_ITEM_TYPE
                )
                .withValue(CommonDataKinds.Event.START_DATE, d)
                .withValue(CommonDataKinds.Event.TYPE, CommonDataKinds.Event.TYPE_ANNIVERSARY)
            ops.add(cpo.build())
        }
        val photo = getPhoto(vc)
        if (photo != null) {
            val cpo = ContentProviderOperation.newInsert(
                ContactsContract.Data.CONTENT_URI
            )
                .withValueBackReference(ContactsContract.Data.RAW_CONTACT_ID, 0)
                .withValue(
                    ContactsContract.Data.MIMETYPE,
                    CommonDataKinds.Photo.CONTENT_ITEM_TYPE
                )
                .withValue(CommonDataKinds.Photo.PHOTO, photo)
            ops.add(cpo.build())
        }
        return ops
    }

    private fun getPhoto(vc: VCard): ByteArray? {
        for (photo in vc.photos) {
            if (photo.data != null) return photo.data
        }
        return null
    }

    private fun showToast(toast: String) {
        Toast.makeText(context, toast, Toast.LENGTH_LONG).show()

    }

}