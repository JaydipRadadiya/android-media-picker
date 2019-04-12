package com.turtlebody.mediapicker.activities

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.TextView
import androidx.fragment.app.Fragment
import com.turtlebody.mediapicker.R
import com.turtlebody.mediapicker.fragments.FolderListFragment
import com.turtlebody.mediapicker.base.ActivityBase
import com.turtlebody.mediapicker.core.Constants
import com.turtlebody.mediapicker.core.FileManager
import com.turtlebody.mediapicker.core.ImagePicker
import com.turtlebody.mediapicker.core.PickerConfig
import com.turtlebody.mediapicker.core.base.MediaListFragment
import com.turtlebody.mediapicker.fragments.FileListFragment
import com.turtlebody.mediapicker.fragments.medias.AudioListFragment
import com.turtlebody.mediapicker.models.ImageVideoFolder
import com.wangsun.custompicker.api.FilePicker
import com.wangsun.custompicker.api.Picker
import com.wangsun.custompicker.api.callbacks.FilePickerCallback
import com.wangsun.custompicker.api.entity.ChosenFile
import com.wangsun.custompicker.utils.MimeUtils
import org.jetbrains.anko.find
import java.io.File
import java.io.Serializable




class ActivityLibMain : ActivityBase() {


    private lateinit var mFilePicker: FilePicker
    private var mFileType: Int = Constants.FileTypes.FILE_TYPE_IMAGE
    private lateinit var mMenuItem: MenuItem
    private lateinit var mPickerConfig: PickerConfig

    private lateinit var vToolbarCounter: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(com.turtlebody.mediapicker.R.layout.tb_media_picker_activity)

        initToolbar(com.turtlebody.mediapicker.R.drawable.ic_arrow_back_black_24dp,find(R.id.toolbar))
        toolbarTitle = "Select Folder"
        vToolbarCounter =  find<TextView>(R.id.toolbar_txt_count)
        mFilePicker = FilePicker(this)

        if(intent.extras!=null){
            mPickerConfig = intent.getSerializableExtra(PickerConfig.ARG_BUNDLE) as PickerConfig
            mFileType = intent.getIntExtra(ImagePicker.FILE_TYPE,Constants.FileTypes.FILE_TYPE_IMAGE)
        }

    }

    override fun onBackPressed() {
        //super.onBackPressed()
        val fragment = supportFragmentManager.findFragmentById(R.id.frame_content)
        when (fragment) {
            is FolderListFragment -> finish()
            is FileListFragment -> {
                super.onBackPressed()
                toolbarTitle = "Select Folder"
                vToolbarCounter.visibility = View.GONE
                mMenuItem.isVisible = true
            }
            else -> super.onBackPressed()
        }

    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.activity_main_menu, menu)
        mMenuItem = menu.getItem(0)
        startFolderListFragment()
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        when (item?.itemId) {
            android.R.id.home -> {
                onBackPressed()
                return true
            }
            com.turtlebody.mediapicker.R.id.action_open ->{
                startImagePicker()
            }
            else -> return super.onOptionsItemSelected(item)
        }
        return true
    }

    @SuppressLint("CheckResult")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (resultCode == Activity.RESULT_OK && requestCode == Picker.PICK_FILE){
            mFilePicker.submit(data)
            println("filePath: $data")
        }
        else
            super.onActivityResult(requestCode, resultCode, data)
    }


    private fun startFolderListFragment(){
        toolbarTitle = "Select Folder"
        vToolbarCounter.visibility = View.GONE
        mMenuItem.isVisible = true

        val bundle =  Bundle()
        bundle.putInt(ImagePicker.FILE_TYPE,mFileType)

        val fragment = FolderListFragment.newInstance(Constants.Fragment.FOLDER_LIST, bundle)
        val ft = supportFragmentManager.beginTransaction()
        ft.add(R.id.frame_content, fragment, FolderListFragment::class.java.simpleName)
                .addToBackStack(null)
                .commit()
    }

    /**
     * @param folderInfo if(audio) then folderPath else folderId
     */
    fun startMediaListFragment(folderInfo: String){
        val bundle =  Bundle()
        bundle.putSerializable(ImagePicker.FILE_TYPE, mFileType)
        bundle.putSerializable(MediaListFragment.B_ARG_PICKER_CONFIG, mPickerConfig)
        var fragment : Fragment
        var fragmentTag : String
        when(mFileType){
            Constants.FileTypes.FILE_TYPE_IMAGE -> {
                toolbarTitle = "Choose Image"
                bundle.putString(ImageVideoFolder.FOLDER_ID,folderInfo)
                bundle.putSerializable(PickerConfig.ARG_BUNDLE, mPickerConfig)
                fragment = FileListFragment.newInstance(Constants.Fragment.IMAGE_LIST, bundle)
                fragmentTag = FileListFragment::class.java.simpleName
            }
            Constants.FileTypes.FILE_TYPE_VIDEO -> {
                toolbarTitle = "Choose Video"
                bundle.putString(ImageVideoFolder.FOLDER_ID,folderInfo)
                bundle.putSerializable(PickerConfig.ARG_BUNDLE, mPickerConfig)
                fragment = FileListFragment.newInstance(Constants.Fragment.IMAGE_LIST, bundle)
                fragmentTag = FileListFragment::class.java.simpleName

            }
            Constants.FileTypes.FILE_TYPE_AUDIO -> {
                toolbarTitle = "Choose Audio"
                bundle.putString(AudioListFragment.B_ARG_FOLDER_PATH,folderInfo)
                fragment = AudioListFragment.newInstance(Constants.Fragment.AUDIO_LIST, bundle)
                fragmentTag = AudioListFragment::class.java.simpleName

            }
            else ->{
                toolbarTitle = "Choose Image"
                bundle.putString(ImageVideoFolder.FOLDER_ID,folderInfo)
                bundle.putSerializable(PickerConfig.ARG_BUNDLE, mPickerConfig)
                fragment = FileListFragment.newInstance(Constants.Fragment.IMAGE_LIST, bundle)
                fragmentTag = FileListFragment::class.java.simpleName
            }
        }

        vToolbarCounter.visibility = View.VISIBLE
        mMenuItem.isVisible = false

        val ft = supportFragmentManager.beginTransaction()
        ft.add(R.id.frame_content, fragment, fragmentTag)
                .addToBackStack(null)
                .commit()
    }

    @Deprecated("irrelevant")
    fun startImageListFragment(folderId: String){
        when(mFileType){
            Constants.FileTypes.FILE_TYPE_IMAGE -> toolbarTitle = "Choose Image"
            Constants.FileTypes.FILE_TYPE_VIDEO -> toolbarTitle = "Choose Video"
            Constants.FileTypes.FILE_TYPE_AUDIO -> toolbarTitle = "Choose Audio"
        }

        vToolbarCounter.visibility = View.VISIBLE
        mMenuItem.isVisible = false

        val bundle =  Bundle()
        bundle.putString(ImageVideoFolder.FOLDER_ID,folderId)
        bundle.putSerializable(PickerConfig.ARG_BUNDLE, mPickerConfig)
        bundle.putSerializable(ImagePicker.FILE_TYPE, mFileType)

        val fragment = FileListFragment.newInstance(Constants.Fragment.IMAGE_LIST, bundle)
        val ft = supportFragmentManager.beginTransaction()
        ft.add(R.id.frame_content, fragment, FileListFragment::class.java.simpleName)
                .addToBackStack(null)
                .commit()

    }

    fun updateCounter(counter: Int){
        vToolbarCounter.text = "$counter"
    }

    fun sendBackData(list: MutableList<Uri>){
        if(list.isNotEmpty()){
            val intent = Intent()
            intent.putExtra(FileListFragment.URI_LIST_KEY,list as Serializable)
            setResult(Activity.RESULT_OK, intent)
        }
        finish()
    }

    private fun startImagePicker() {

        when(mFileType){
            Constants.FileTypes.FILE_TYPE_IMAGE->{
                mFilePicker.setFilePickerCallback(object : FilePickerCallback{
                    override fun onFilesChosen(files: MutableList<ChosenFile>?) {
                        files?.let {
                            val finalFiles = ArrayList<Uri>()
                            if (!files.isEmpty()) {
                                for (i in files) {
                                    if (i.isSuccess && i.size!=0L) {
                                        finalFiles.add(FileManager.getContentUri(this@ActivityLibMain, File(i.originalPath)))
                                    }
                                }
                            }
                            sendBackData(finalFiles)
                        }
                    }
                    override fun onError(message: String?) {}
                })
                        .allowMultipleFiles(mPickerConfig.mAllowMultiImages)
                        .setFileType(MimeUtils.FileType.IMAGE)
                        .setMimeTypes(MimeUtils.MimeType.IMAGE)
                        .pickFile()
            }
            Constants.FileTypes.FILE_TYPE_VIDEO->{
                mFilePicker.setFilePickerCallback(object : FilePickerCallback{
                    override fun onFilesChosen(files: MutableList<ChosenFile>?) {
                        files?.let {
                            val finalFiles = ArrayList<Uri>()
                            if (!files.isEmpty()) {
                                for (i in files) {
                                    if (i.isSuccess && i.size!=0L) {
                                        finalFiles.add(FileManager.getContentUri(this@ActivityLibMain, File(i.originalPath)))
                                    }
                                }
                            }
                            sendBackData(finalFiles)
                        }
                    }
                    override fun onError(message: String?) {}
                })
                        .allowMultipleFiles(mPickerConfig.mAllowMultiImages)
                        .setFileType(MimeUtils.FileType.VIDEO)
                        .setMimeTypes(MimeUtils.MimeType.VIDEO)
                        .pickFile()
            }
            Constants.FileTypes.FILE_TYPE_AUDIO->{
                mFilePicker.setFilePickerCallback(object : FilePickerCallback{
                    override fun onFilesChosen(files: MutableList<ChosenFile>?) {
                        files?.let {
                            val finalFiles = ArrayList<Uri>()
                            if (!files.isEmpty()) {
                                for (i in files) {
                                    if (i.isSuccess && i.size!=0L) {
                                        finalFiles.add(FileManager.getContentUri(this@ActivityLibMain, File(i.originalPath)))
                                    }
                                }
                            }
                            sendBackData(finalFiles)
                        }
                    }
                    override fun onError(message: String?) {}
                })
                        .allowMultipleFiles(mPickerConfig.mAllowMultiImages)
                        .setFileType(MimeUtils.FileType.AUDIO)
                        .setMimeTypes(MimeUtils.MimeType.AUDIO)
                        .pickFile()
            }
        }

    }
}
