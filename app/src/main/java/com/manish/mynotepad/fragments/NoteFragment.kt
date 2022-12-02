package com.manish.mynotepad.fragments

import android.content.res.Configuration
import android.graphics.Color
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import androidx.fragment.app.Fragment
import android.view.View
import android.view.ViewTreeObserver
import android.view.WindowManager
import android.view.inputmethod.EditorInfo
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.fragment.app.activityViewModels
import androidx.navigation.Navigation
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import com.google.android.material.snackbar.BaseTransientBottomBar
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.transition.MaterialElevationScale
import com.manish.mynotepad.R
import com.manish.mynotepad.activities.MainActivity
import com.manish.mynotepad.adapter.NoteAdapter
import com.manish.mynotepad.databinding.FragmentNoteBinding
import com.manish.mynotepad.utils.SwipeToDelete
import com.manish.mynotepad.utils.hideKeyboard
import com.manish.mynotepad.viewmodel.NoteViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.util.concurrent.TimeUnit

class NoteFragment : Fragment(R.layout.fragment_note) {

    private lateinit var noteBinding: FragmentNoteBinding
    private val noteViewModel: NoteViewModel by activityViewModels()
    private lateinit var noteAdapter: NoteAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        exitTransition = MaterialElevationScale(false).apply {
            duration = 350
        }
        enterTransition = MaterialElevationScale(true).apply {
            duration = 350
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        noteBinding = FragmentNoteBinding.bind(view)
        val activity = activity as MainActivity
        val navController = Navigation.findNavController(view)
        requireView().hideKeyboard()
        CoroutineScope(Dispatchers.Main).launch {
            delay(10)v
            activity.window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
            activity.window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS)
            activity.window.statusBarColor = Color.parseColor("#9E9D9D")
        }
        noteBinding.addNoteFab.setOnClickListener {
            noteBinding.appBarLayout.visibility = View.INVISIBLE
            navController.navigate(NoteFragmentDirections.actionNoteFragmentToNoteDetailFragment())
        }
        noteBinding.innerFab.setOnClickListener {
            noteBinding.appBarLayout.visibility = View.INVISIBLE
            navController.navigate(NoteFragmentDirections.actionNoteFragmentToNoteDetailFragment())
        }
        recyclerViewDisplay()

        swipeToDelete(noteBinding.recyclerViewNote)
        noteBinding.searchNotes.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
                noteBinding.noData.isVisible = false
            }

            override fun onTextChanged(sequence: CharSequence?, p1: Int, p2: Int, p3: Int) {
                if (sequence.toString().isNotEmpty()) {
                    val text = sequence.toString()
                    val query = "%$text%"
                    if (query.isNotEmpty()) {
                        noteViewModel.searchNote(query).observe(viewLifecycleOwner) {
                            noteAdapter.submitList(it)
                        }
                    } else {
                        observerDataChanges()
                    }
                } else {
                    observerDataChanges()
                }
            }

            override fun afterTextChanged(p0: Editable?) {
                //
            }

        })
        noteBinding.searchNotes.setOnEditorActionListener {v,actionId,_ ->
            if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                v.clearFocus()
                requireView().hideKeyboard()
            }
            return@setOnEditorActionListener true
        }
        noteBinding.recyclerViewNote.setOnScrollChangeListener { _, scrollX, scrollY, _, oldScrollY ->
            when {
                scrollY>oldScrollY -> {
                    noteBinding.chatFabText.isVisible = false
                }
                scrollX==scrollY -> {
                    noteBinding.chatFabText.isVisible = true
                }
                else -> {
                    noteBinding.chatFabText.isVisible = true
                }
            }
        }
    }

    private fun swipeToDelete(recyclerViewNote: RecyclerView) {

        val swipeToDeleteCallback = object : SwipeToDelete() {
            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                val position = viewHolder.absoluteAdapterPosition
                val note = noteAdapter.currentList[position]
                var actionbtnTapped = false
                noteViewModel.deleteNote(note)
                noteBinding.searchNotes.apply {
                    hideKeyboard()
                    clearFocus()
                }
                if (noteBinding.searchNotes.text.toString().isEmpty()) {
                    observerDataChanges()
                }
                val snackBar = Snackbar.make(requireView(),"Note Deleted",Snackbar.LENGTH_LONG)
                    .addCallback(object : BaseTransientBottomBar.BaseCallback<Snackbar>(){
                        override fun onDismissed(transientBottomBar: Snackbar?, event: Int) {
                            super.onDismissed(transientBottomBar, event)
                        }

                        override fun onShown(transientBottomBar: Snackbar?) {
                            transientBottomBar?.setAction("UNDO") {
                                noteViewModel.insertNote(note)
                                actionbtnTapped = true
                                noteBinding.noData.isVisible = false
                            }
                            super.onShown(transientBottomBar)
                        }
                    }).apply {
                        animationMode = Snackbar.ANIMATION_MODE_FADE
                        setAnchorView(R.id.add_note_fab)
                    }
                snackBar.setActionTextColor(ContextCompat.getColor(requireContext(),R.color.yellowOrange))
                snackBar.show()
            }
        }
        val itemTouchHelper = ItemTouchHelper(swipeToDeleteCallback)
        itemTouchHelper.attachToRecyclerView(recyclerViewNote)
    }

    private fun observerDataChanges() {
        noteViewModel.getAllNotes().observe(viewLifecycleOwner) {
            noteBinding.noData.isVisible = it.isEmpty()
            noteAdapter.submitList(it)
        }
    }

    private fun recyclerViewDisplay() {
        when (resources.configuration.orientation) {
            Configuration.ORIENTATION_PORTRAIT -> setUpRecyclerView(2)
            Configuration.ORIENTATION_LANDSCAPE -> setUpRecyclerView(3)
        }
    }

    private fun setUpRecyclerView(spanCount: Int) {

        noteBinding.recyclerViewNote.apply {
            layoutManager = StaggeredGridLayoutManager(spanCount,StaggeredGridLayoutManager.VERTICAL)
            setHasFixedSize(true)
            noteAdapter = NoteAdapter()
            noteAdapter.stateRestorationPolicy = RecyclerView.Adapter.StateRestorationPolicy.PREVENT_WHEN_EMPTY
            adapter = noteAdapter
            postponeEnterTransition(300L,TimeUnit.MILLISECONDS)
            viewTreeObserver.addOnPreDrawListener {
                startPostponedEnterTransition()
                true
            }
        }
        observerDataChanges()
    }
}