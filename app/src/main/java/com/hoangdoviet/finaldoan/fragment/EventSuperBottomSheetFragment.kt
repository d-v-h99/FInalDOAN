package com.hoangdoviet.finaldoan.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import com.andrefrsousa.superbottomsheet.SuperBottomSheetFragment
import com.hoangdoviet.finaldoan.adapter.EventListAdapter
import com.hoangdoviet.finaldoan.databinding.BottomSheetEventListBinding
import com.hoangdoviet.finaldoan.model.Event

class EventSuperBottomSheetFragment(private val events: List<Event>) : SuperBottomSheetFragment(), EventListAdapter.EventClickListener {

    private var _binding: BottomSheetEventListBinding? = null
    private val binding get() = _binding!!
    private lateinit var eventListAdapter: EventListAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        super.onCreateView(inflater, container, savedInstanceState)
        _binding = BottomSheetEventListBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        eventListAdapter = EventListAdapter(mutableListOf(), this)
        binding.recyclerView.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = eventListAdapter.apply {
                updateEvents(events)
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    override fun onEventClick(event: Event, position: Int) {
        val fragment = EventFragment.newInstance(event, position)
        fragment.show(childFragmentManager, "EventFragment")
    }

    override fun getExpandedHeight(): Int {
        return ViewGroup.LayoutParams.WRAP_CONTENT
    }
}
