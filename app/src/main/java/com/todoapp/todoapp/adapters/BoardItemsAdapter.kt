package com.todoapp.todoapp.adapters

import com.todoapp.todoapp.models.Board
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.todoapp.todoapp.R

// The open annotation on a class is the opposite of Java's final:
// it allows others to inherit from this class.
open class BoardItemsAdapter(private val context:Context,
                             private var list:ArrayList<Board>):
    RecyclerView.Adapter<RecyclerView.ViewHolder>()
    {
        private var onClickListener: OnClickListener? = null

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
            return MyViewHolder(LayoutInflater.from(context).inflate(R.layout.item_board,parent,false))
            //A ViewHolder describes an item view and metadata about its place within the RecyclerView
        }

        override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
            // onBindViewHolder = Called by RecyclerView to display the data at the specified position.
            // This method should update the contents of the RecyclerView.ViewHolder.itemView to
            // reflect the item at the given position.

            val model = list[position]

            if (holder is MyViewHolder) {

                Glide
                        .with(context)
                        .load(model.image)
                        .centerCrop()
                        .placeholder(R.drawable.ic_board_place_holder)
                        .into(holder.itemView.findViewById(R.id.iv_board_image))

                holder.itemView.findViewById<TextView>(R.id.tv_name).text = model.name
                holder.itemView.findViewById<TextView>(R.id.tv_created_by).text = "Autore : ${model.createdBy}"

                holder.itemView.setOnClickListener {

                    if (onClickListener != null) {
                        onClickListener!!.onClick(position, model)
                    }
                }
            }
        }
        override fun getItemCount(): Int {
            return list.size
        }

        /**
         * A function for OnClickListener where the Interface is the expected parameter..
         */
        fun setOnClickListener(onClickListener: OnClickListener) {
            this.onClickListener = onClickListener
        }

        /**
         * An interface for onclick items.
         */
        interface OnClickListener {
            fun onClick(position: Int, model: Board)
        }

        private class MyViewHolder(view: View):RecyclerView.ViewHolder(view)

    }
