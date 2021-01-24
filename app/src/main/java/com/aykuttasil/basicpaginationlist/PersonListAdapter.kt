package com.aykuttasil.basicpaginationlist

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class PersonListAdapter : RecyclerView.Adapter<PersonListAdapter.ViewHolder>() {

    private var personList = mutableListOf<Person>()
    private var hashSet = hashSetOf<Int>()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val vi = LayoutInflater.from(parent.context).inflate(R.layout.item_people, parent, false)
        return ViewHolder(vi)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = personList[position]
        holder.bind(item)
    }

    fun addItems(list: List<Person>) {
        list.forEach {
            if (!hashSet.contains(it.id)) {
                hashSet.add(it.id)
                personList.add(it)
            }
        }
        notifyDataSetChanged()
    }

    fun clearList() {
        personList.clear()
        hashSet.clear()
        notifyDataSetChanged()
    }

    override fun getItemCount(): Int {
        return personList.size
    }

    class ViewHolder(private val vi: View) : RecyclerView.ViewHolder(vi) {

        fun bind(person: Person) {
            vi.findViewById<TextView>(R.id.txtPeopleFullName).text =
                "${person.fullName}(${person.id})"
        }
    }


}