package com.example.smartlab.adapter

import android.app.AlertDialog
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.smartlab.databinding.DialogHistoryRuleBinding
import com.example.smartlab.databinding.ItemHistoryRulesBinding
import com.example.smartlab.databinding.ItemRuleBinding
import com.example.smartlab.databinding.ItemUserBinding
import com.example.smartlab.model.HistoryRuleResponse
import com.example.smartlab.model.RuleData
import com.example.smartlab.model.UserProfile
import java.time.LocalDateTime
import java.time.OffsetDateTime
import java.time.format.DateTimeFormatter


class UserAdapter(
    private val users: MutableList<UserProfile>,
    private val onEditClick: (UserProfile) -> Unit,
    private val onDeleteClick: (UserProfile) -> Unit
) :
    RecyclerView.Adapter<UserAdapter.UserViewHolder>() {

    inner class UserViewHolder(val binding: ItemUserBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(user: UserProfile) = with(binding) {
            nameAndSurname.text = user.fullName
            email.text = user.email
            role.text = user.role


                // Обробники натискань
                editRule.setOnClickListener { onEditClick(user) }
                deleteRule.setOnClickListener { onDeleteClick(user) }



        }




    }

    fun removeUser(user: UserProfile) {
        val index = users.indexOfFirst { it.id == user.id }
        if (index != -1) {
            users.removeAt(index)
            notifyItemRemoved(index)
        }
    }


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): UserViewHolder {
        val binding = ItemUserBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return UserViewHolder(binding)
    }

    override fun onBindViewHolder(holder: UserViewHolder, position: Int) {
        holder.bind(users[position])
    }

    override fun getItemCount(): Int = users.size






}