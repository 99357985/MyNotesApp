package com.manish.mynotepad.fragments

import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.core.os.bundleOf
import androidx.core.view.ViewCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.setFragmentResult
import androidx.navigation.NavController
import androidx.navigation.Navigation
import androidx.navigation.fragment.navArgs
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.transition.MaterialContainerTransform
import com.manish.mynotepad.R
import com.manish.mynotepad.activities.MainActivity
import com.manish.mynotepad.databinding.BottomSheetLayoutBinding
import com.manish.mynotepad.databinding.FragmentNoteDetailBinding
import com.manish.mynotepad.model.Note
import com.manish.mynotepad.utils.hideKeyboard
import com.manish.mynotepad.viewmodel.NoteViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date

class NoteDetailFragment : Fragment(R.layout.fragment_note_detail) {

    private lateinit var navController: NavController
    private lateinit var contentBinding: FragmentNoteDetailBinding
    private var note: Note? = null
    private var color = -1
    private lateinit var result: String
    private val noteViewModel: NoteViewModel by activityViewModels()
    private val currentDate = SimpleDateFormat.getInstance().format(Date())
    private val job = CoroutineScope(Dispatchers.Main)
    private val args: NoteDetailFragmentArgs by navArgs()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val animation = MaterialContainerTransform().apply {
            drawingViewId = R.id.fragment
            scrimColor = Color.TRANSPARENT
            duration = 300L
        }
        sharedElementEnterTransition = animation
        sharedElementReturnTransition = animation
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        contentBinding = FragmentNoteDetailBinding.bind(view)

        navController = Navigation.findNavController(view)
        val activity = activity as MainActivity

        ViewCompat.setTransitionName(contentBinding.noteDetailFragmentParent,"recyclerView_${args.note?.id}")

        contentBinding.backBtn.setOnClickListener {
            requireView().hideKeyboard()
            navController.popBackStack()
        }

        contentBinding.saveNote.setOnClickListener {
            insertNote()
        }
        try {
            contentBinding.editTextNoteContent.setOnFocusChangeListener { _, hasFocus ->
                if (hasFocus) {
                    contentBinding.bottomBar.visibility = View.VISIBLE
                    contentBinding.editTextNoteContent.setStylesBar(contentBinding.styleBar)
                } else {
                    contentBinding.bottomBar.visibility = View.GONE
                }
            }
        } catch (e: Throwable) {
            Log.d("TAG", e.stackTraceToString())
        }

        contentBinding.fabColorPick.setOnClickListener {
            val bottomSheetDialogue =
                BottomSheetDialog(requireContext(), R.style.BottomSheetDialogTheme)

            val bottomSheetView: View =
                layoutInflater.inflate(R.layout.bottom_sheet_layout, null)
            with(bottomSheetDialogue) {
                setContentView(bottomSheetView)
                show()
            }
            val bottomSheetBinding = BottomSheetLayoutBinding.bind(bottomSheetView)

            bottomSheetBinding.apply {
                colorPicker.apply {
                    setSelectedColor(color)
                    setOnColorSelectedListener { value ->
                        color = value
                        contentBinding.apply {
                            noteDetailFragmentParent.setBackgroundColor(color)
                            toolBarFragmentNoteDetail.setBackgroundColor(color)
                            bottomBar.setBackgroundColor(color)
                            activity.window.statusBarColor = color
                        }
                        bottomSheetBinding.bottomSheetParent.setCardBackgroundColor(color)
                    }
                }
                bottomSheetParent.setCardBackgroundColor(color)
            }
            bottomSheetView.post {
                bottomSheetDialogue.behavior.state = BottomSheetBehavior.STATE_EXPANDED
            }
        }
        setUpNote()
    }

    private fun setUpNote() {
        val note = args.note
        val title = contentBinding.editTextTitle
        val content = contentBinding.editTextNoteContent
        val lastEdited = contentBinding.noteLastEdited
        if (note == null) {
            contentBinding.noteLastEdited.text =
                getString(R.string.edited_on, SimpleDateFormat.getDateInstance().format(Date()))
        }
        if (note!=null) {
            title.setText(note.title)
            content.renderMD(note.content)
            lastEdited.text = getString(R.string.edited_on,note.date)
            color = note.color
            contentBinding.apply {
                job.launch {
                    delay(10)
                    noteDetailFragmentParent.setBackgroundColor(color)
                }
                toolBarFragmentNoteDetail.setBackgroundColor(color)
                bottomBar.setBackgroundColor(color)
            }
            activity?.window?.statusBarColor = note.color
        }
    }

    private fun insertNote() {
        if (contentBinding.editTextNoteContent.text.toString()
                .isEmpty() or contentBinding.editTextTitle.toString().isEmpty()
        ) {
            Toast.makeText(activity, "Please add some text", Toast.LENGTH_SHORT).show()
        } else {
            note = args.note
            when (note) {
                null -> {
                    noteViewModel.insertNote(Note(0,
                        contentBinding.editTextTitle.text.toString(),
                        contentBinding.editTextNoteContent.getMD(),
                        currentDate, color
                    ))
                    result = "Note Saved"
                    setFragmentResult("key", bundleOf("bundleKey" to result))
                    navController.navigate(NoteDetailFragmentDirections.actionNoteDetailFragmentToNoteFragment())
                }
                else -> {
                    updateNote()
                    navController.popBackStack()
                }
            }
        }
    }

    private fun updateNote() {
        if (note != null) {
            noteViewModel.updateNote(Note(
                note!!.id, contentBinding.editTextTitle.text.toString(),
                contentBinding.editTextNoteContent.getMD(),
                currentDate,
                color
            ))
        }
    }
}