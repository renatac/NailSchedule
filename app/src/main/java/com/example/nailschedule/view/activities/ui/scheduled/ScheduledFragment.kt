package com.example.nailschedule.view.activities.ui.scheduled

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.example.nailschedule.databinding.FragmentScheduledBinding
import com.example.nailschedule.view.activities.data.model.User

class ScheduledFragment : Fragment() {

    private lateinit var scheduledViewModel: ScheduledViewModel
    private var _binding: FragmentScheduledBinding? = null

    private val scheduledAdapter: ScheduledAdapter by lazy {
        ScheduledAdapter(::onClick)
    }

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        scheduledViewModel =
            ViewModelProvider(this).get(ScheduledViewModel::class.java)

        _binding = FragmentScheduledBinding.inflate(inflater, container, false)
        setupAdapter()
        val root: View = binding.root
        /* val textView: TextView = binding.textNotifications
        scheduledViewModel.text.observe(viewLifecycleOwner, Observer {
            textView.text = it
        }) */
        return root
    }

    private fun setupAdapter() {
        binding.recyclerScheduled.adapter = scheduledAdapter
        val u = arrayListOf(User(name = "Renata Cristovam",
            service = "Serviço caro 1",
            date = "Sua data foi agendada - 13/02/2022",
            time = "Horário agendado: 05:25"
        ), User(name = "Renata Cristovam",
            service = "Serviço caro 1",
            date = "Sua data foi agendada - 13/02/2022",
            time = "Horário agendado: 05:25"
        ), User(name = "Renata Cristovam",
            service = "Serviço caro 1",
            date = "Sua data foi agendada - 13/02/2022",
            time = "Horário agendado: 05:25"
        ), User(name = "Renata Cristovam",
            service = "Serviço caro 1",
            date = "Sua data foi agendada - 13/02/2022",
            time = "Horário agendado: 05:25"
        ), User(name = "Renata Cristovam",
            service = "Serviço caro 1",
            date = "Sua data foi agendada - 13/02/2022",
            time = "Horário agendado: 05:25"
        ), User(name = "Renata Cristovam",
            service = "Serviço caro 1",
            date = "Sua data foi agendada - 13/02/2022",
            time = "Horário agendado: 05:25"
        ), User(name = "Renata Cristovam",
            service = "Serviço caro 1",
            date = "Sua data foi agendada - 13/02/2022",
            time = "Horário agendado: 05:25"
        ))
        scheduledAdapter.setItemScheduledList(u)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun onClick(user: User) {
        print(user)
    }
}