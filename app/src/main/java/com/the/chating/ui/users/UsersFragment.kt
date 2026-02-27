package com.the.chating.ui.users

import android.app.AlertDialog
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.core.content.ContentProviderCompat.requireContext
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.FirebaseDatabase
import com.the.chating.Constants
import com.the.chating.R
import com.the.chating.data.UserModel
import com.the.chating.databinding.FragmentUsersBinding


class UsersFragment : Fragment() {


    private var _binding: FragmentUsersBinding? = null

    private val binding get() = _binding!!

    private lateinit var viewModel: UsersViewmodel

    lateinit var adapterMessage: UsersAdapter

    private var pendingDeletePosition: Int? = null


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View? {

        _binding = FragmentUsersBinding.inflate(inflater, container, false)


        viewModel = ViewModelProvider(this).get(UsersViewmodel::class.java)

        setupRecyclerView()
        observeViewModel()

        viewModel.loadUsers()

        return  binding.root
    }

    private fun setupRecyclerView() {

        adapterMessage = UsersAdapter(
            onItemClick = { user -> openChat(user) },
            deleteMessages = { user, position ->
                showDeleteDialog(user, position)
            }
        )

        binding.recyclerMessage.layoutManager =
            LinearLayoutManager(requireContext())

        binding.recyclerMessage.setHasFixedSize(true)
        binding.recyclerMessage.adapter = adapterMessage
    }


    private fun observeViewModel(){
        viewModel.messageResults.observe(viewLifecycleOwner) { results ->
            adapterMessage.updatePostList(results)
        }

        viewModel.deleteResult.observe(viewLifecycleOwner) { success ->

            if (success) {

                pendingDeletePosition?.let { position ->

                    if (position < adapterMessage.publicationList.size) {
                        adapterMessage.publicationList.removeAt(position)
                        adapterMessage.notifyItemRemoved(position)
                    }
                }

                Toast.makeText(
                    requireContext(),
                    "Chat deleted",
                    Toast.LENGTH_SHORT
                ).show()
            } else {
                Toast.makeText(
                    requireContext(),
                    "Error deleting chat",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }


    private fun openChat(user: UserModel) {
        val bundle = Bundle().apply {
            putParcelable(Constants.KEY_USER, user)
        }
        findNavController().navigate(R.id.navigation_chating, bundle)
    }

    private fun showDeleteDialog(user: UserModel, position: Int) {
        AlertDialog.Builder(requireContext())
            .setTitle("Delete chat")
            .setMessage("Are you sure you want to delete this chat?")
            .setPositiveButton("Yes") { _, _ ->
                pendingDeletePosition = position
                viewModel.deleteChat(user.uid ?: return@setPositiveButton)

            }
            .setNegativeButton("No", null)
            .show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}