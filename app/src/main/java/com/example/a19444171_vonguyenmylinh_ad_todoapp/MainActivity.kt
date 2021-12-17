package com.example.a19444171_vonguyenmylinh_ad_todoapp

import android.content.Context
import android.graphics.Color
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.EditText
import android.widget.ListView
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.firebase.database.*

class MainActivity : AppCompatActivity() {
    lateinit var database: DatabaseReference
    var toDoList: MutableList<ToDoModel>? = null
    lateinit var  adapter: ToDoApdapter
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val fab = findViewById<FloatingActionButton>(R.id.btn_add)

        val listView = findViewById<ListView>(R.id.listView)
        toDoList = mutableListOf<ToDoModel>()
        adapter = ToDoApdapter(this, toDoList!!)
        listView.adapter = adapter

        database = FirebaseDatabase.getInstance("https://todoapp-c01a4-default-rtdb.asia-southeast1.firebasedatabase.app/").reference
        database.addValueEventListener(object: ValueEventListener{
            override fun onCancelled(p0: DatabaseError) {

            }

            override fun onDataChange(p0: DataSnapshot) {
                toDoList!!.clear()
                addItemToList(p0)
            }
        })
        fab.setOnClickListener{_ ->
            val alertDialog = AlertDialog.Builder(this)
            val textEditView = EditText(this)
            alertDialog.setMessage("Add New Task")
            alertDialog.setView(textEditView)
            alertDialog.setPositiveButton("Save") { dialog, i ->
                val todoItem = ToDoModel.createList()
                todoItem.itemDataText = textEditView.text.toString()
                todoItem.status = false

                val newTodo = database.child("toDo").push()
                todoItem.uID = newTodo.key

                newTodo.setValue(todoItem)
                dialog.dismiss()
            }
            alertDialog.setNegativeButton("Cancel") { dialog, i ->
                dialog.dismiss()
            }
            alertDialog.setCancelable(true)
            alertDialog.show()
        }
    }

    private fun addItemToList(snapshot: DataSnapshot) {
        val items = snapshot.children.iterator()
        if (items.hasNext()) {
            val index = items.next()
            var itemsIterator = index.children.iterator()

            while(itemsIterator.hasNext()) {
                val current = itemsIterator.next()

                val todoItemData = ToDoModel.createList()
                val map = current.value as HashMap<String, Any>

                todoItemData.uID = current.key
                todoItemData.status = map.get("status") as Boolean?
                todoItemData.itemDataText = map.get("itemDataText") as String?
                toDoList?.add(todoItemData)
                Log.d("snapshot", toDoList.toString())
            }

        }
        adapter.notifyDataSetChanged()
    }

    fun modifyStatus(uId: String, status: Boolean) {
        val itemRef = database.child("toDo").child(uId)
        itemRef.child("status").setValue(status)
    }
    fun modifyText(uId: String, text: String) {
        val itemRef = database.child("toDo").child(uId)
        itemRef.child("itemDataText").setValue(text)
    }
    fun delete(uId: String){
        val itemRef = database.child("toDo").child(uId)
        itemRef.removeValue()
        adapter.notifyDataSetChanged()
    }
}

class ToDoApdapter(private val context: Context, private val toDoList: MutableList<ToDoModel>) : BaseAdapter() {
    val inflater = LayoutInflater.from(context)
    class ListViewHolder(view: View?){
        val textView = view!!.findViewById<TextView>(R.id.item_text_todo)
        val status = view!!.findViewById<TextView>(R.id.item_status)
    }
    override fun getCount(): Int {
        return toDoList.size
    }

    override fun getItem(position: Int): Any {
        return toDoList.get(position)
    }

    override fun getItemId(position: Int): Long {
        return position.toLong()
    }

    override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
        val view: View
        val listView: ListViewHolder
        if (convertView == null){
            view = inflater.inflate(R.layout.item_layout, parent, false)
            listView = ListViewHolder(view)
            view.tag = listView
        }
        else{
            view = convertView
            listView = view.tag as ListViewHolder
        }

        listView.textView?.text = toDoList[position].itemDataText
        listView.status?.text = if (toDoList[position].status == true) {
            listView.status.setTextColor(Color.GREEN)
            "Completed"
        } else {
            listView.status.setTextColor(Color.RED)
            "Not completed"
        }

        view.setOnClickListener{_ ->
            val alertDialog = AlertDialog.Builder(context)
            val textEditView = EditText(context)
            alertDialog.setMessage("Edit Task")
            alertDialog.setView(textEditView)
            textEditView.setText( toDoList[position].itemDataText)
            alertDialog.setPositiveButton("Save") { dialog, i ->
                val main = context as MainActivity
                main.modifyText(toDoList[position].uID.toString(), textEditView.text.toString())
                dialog.dismiss()
            }
            alertDialog.setNegativeButton("Change Status") { dialog, i ->
                val main = context as MainActivity
                main.modifyStatus(toDoList[position].uID.toString(), !toDoList[position].status!!)
                dialog.dismiss()
            }
            alertDialog.setNeutralButton("Delele") { dialog, i ->
                val main = context as MainActivity
                main.delete(toDoList[position].uID.toString())
                dialog.dismiss()
            }
            alertDialog.show()
        }
        return view
    }
}
