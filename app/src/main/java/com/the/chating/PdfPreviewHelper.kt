package com.the.chating

import android.content.Context
import android.graphics.Bitmap
import android.graphics.pdf.PdfRenderer
import android.os.ParcelFileDescriptor
import android.widget.ImageView
import android.widget.Toast
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File

object PdfPreviewHelper {
    fun renderFromFirebaseUrl(context: Context, firebaseUrl: String, imageView: ImageView) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                if (!firebaseUrl.startsWith("https://")) {
                    withContext(Dispatchers.Main) {
                        imageView.setImageResource(android.R.drawable.ic_delete)
                        Toast.makeText(context, "PDF is not download", Toast.LENGTH_SHORT).show()
                    }
                    return@launch
                }

                val storageRef = FirebaseStorage.getInstance().getReferenceFromUrl(firebaseUrl)
                val localFile = File.createTempFile("tempPdf", ".pdf")

                storageRef.getFile(localFile).addOnSuccessListener {
                    renderLocalPdf(context, localFile, imageView)
                }.addOnFailureListener { e ->

                    CoroutineScope(Dispatchers.Main).launch {
                        imageView.setImageResource(android.R.drawable.ic_delete)
                        Toast.makeText(context, "Failed to download PDF", Toast.LENGTH_SHORT).show()
                    }
                }

            } catch (e: Exception) {
//                Log.e("PDF_PREVIEW", "Exception: ${e.message}")
                withContext(Dispatchers.Main) {
                    imageView.setImageResource(android.R.drawable.ic_delete)
                    Toast.makeText(context, "Failde download PDF", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun renderLocalPdf(context: Context, file: File, imageView: ImageView) {
        try {
            val fileDescriptor = ParcelFileDescriptor.open(file, ParcelFileDescriptor.MODE_READ_ONLY)
            val renderer = PdfRenderer(fileDescriptor)

            if (renderer.pageCount > 0) {
                val page = renderer.openPage(0)
                val bitmap = Bitmap.createBitmap(page.width, page.height, Bitmap.Config.ARGB_8888)
                page.render(bitmap, null, null, PdfRenderer.Page.RENDER_MODE_FOR_DISPLAY)
                page.close()
                renderer.close()
                fileDescriptor.close()

                CoroutineScope(Dispatchers.Main).launch {
                    imageView.setImageBitmap(bitmap)
                }
            }

        } catch (e: Exception) {

            CoroutineScope(Dispatchers.Main).launch {
                imageView.setImageResource(android.R.drawable.ic_delete)
            }
        }
    }
}