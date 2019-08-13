package com.example.customtasks

import android.content.Intent
import android.content.SharedPreferences
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.widget.Toast
import androidx.appcompat.widget.Toolbar
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import org.joda.time.JodaTimePermission


class MainActivity : AppCompatActivity() {

    companion object{
        const val CHANGE_ITEM_REQUEST = 1
        const val CREATE_ITEM_REQUEST = 2
    }

    lateinit var itemsViewModel: ItemsViewModel
    lateinit var linearLayoutManager : LinearLayoutManager
    lateinit var itemsAdapter : ItemsAdapter


    private val recyclerView by lazy(LazyThreadSafetyMode.NONE){
        findViewById<RecyclerView>(R.id.itemsView)
    }

    private val toolBar by lazy (LazyThreadSafetyMode.NONE){
        findViewById<Toolbar>(R.id.appbarlayout_tool_bar)
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        initRecyclerView()
        itemsViewModel = ViewModelProviders.of(this).get(ItemsViewModel::class.java)
        itemsViewModel.getAll().observe(this,  Observer<MutableList<Item>>{itemsAdapter.updateItems(it)})
        toolBar.inflateMenu(R.menu.toolbar_menu)
        toolBar.title = "Tasks"
        toolBar.setOnMenuItemClickListener{
            item ->
            when(item.itemId){
                R.id.addNewTask -> {
                    createNewTask()
                }
            }
            true
        }
    }

    fun initRecyclerView(){
        itemsAdapter = ItemsAdapter()
        recyclerView.adapter = itemsAdapter
        linearLayoutManager = LinearLayoutManager(this)
        recyclerView.layoutManager = linearLayoutManager

        ItemTouchHelper(object : ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT or ItemTouchHelper.RIGHT){
            override fun onMove(
                recyclerView: RecyclerView,
                viewHolder: RecyclerView.ViewHolder,
                target: RecyclerView.ViewHolder
            ): Boolean {
                return false
            }

            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                deleteTask(viewHolder.adapterPosition)
            }
        }).attachToRecyclerView(recyclerView)

        itemsAdapter.onItemEditClicked = {position : Int -> changeTask(position)}
        itemsAdapter.onTaskStartClicked = {position : Int, isStarted: Boolean ->
            updateTask(itemsAdapter.getItem(position), isStarted)}
    }

    fun createNewTask(){
        val intent = Intent(this, ItemEditActivity::class.java)
        intent.putExtra(ItemEditActivity.ITEM_OBJECT, Item())
        intent.putExtra(ItemEditActivity.ITEM_NUMBER, itemsAdapter.itemCount)
        startActivityForResult(intent, CREATE_ITEM_REQUEST)
    }

    fun changeTask(position : Int){
        val intent = Intent(this, ItemEditActivity::class.java)
        intent.putExtra(ItemEditActivity.ITEM_OBJECT, itemsAdapter.getItem(position))
        intent.putExtra(ItemEditActivity.ITEM_NUMBER, position)
        startActivityForResult(intent, CHANGE_ITEM_REQUEST)
    }

    fun deleteTask(position : Int){
        itemsViewModel.delete(itemsAdapter.getItem(position))
    }

    fun updateTask(item : Item, isStarted : Boolean){
        item.onTaskStop = {stopTime, duration ->
            Toast.makeText(this, "This task take ${duration/60} minutes", Toast.LENGTH_LONG).show()
        }
        item.onTaskStart = {startTime, duration ->
            Toast.makeText(this, "Start task that take ${duration/60} minutes", Toast.LENGTH_LONG).show()
        }
        item.trigger()
        itemsViewModel.update(item)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == CHANGE_ITEM_REQUEST && resultCode == RESULT_OK) {
            val item = data!!.getSerializableExtra(ItemEditActivity.ITEM_OBJECT) as Item
            itemsViewModel.update(item)
        }
        if (requestCode == CREATE_ITEM_REQUEST && resultCode == RESULT_OK) {
            val item = data!!.getSerializableExtra(ItemEditActivity.ITEM_OBJECT) as Item
            itemsViewModel.insert(item)
        }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.items_list_menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {

        return when(item.itemId){
            R.id.addNewTask -> {
                createNewTask()
                true
            }
            R.id.deleteAllTasks -> {
                itemsViewModel.deleteAll()
                true
            }
            else -> {
                super.onOptionsItemSelected(item)
            }
        }
    }


}
