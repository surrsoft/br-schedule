package com.example.brschedule

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.appbar.MaterialToolbar
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

class MainActivity : AppCompatActivity() {

    private lateinit var prefs: SharedPreferences
    private val gson = Gson()
    private var workers = mutableListOf<Worker>()
    private var currentWorkerIndex = 0
    private var isLocked = true

    private lateinit var workerSpinner: Spinner
    private lateinit var btnShiftUp: ImageButton
    private lateinit var btnShiftDown: ImageButton
    private lateinit var btnLock: ImageButton
    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: ScheduleAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        prefs = getSharedPreferences("br_schedule_prefs", Context.MODE_PRIVATE)
        loadData()

        val toolbar = findViewById<MaterialToolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)

        workerSpinner = findViewById(R.id.workerSpinner)
        btnShiftUp = findViewById(R.id.btnShiftUp)
        btnShiftDown = findViewById(R.id.btnShiftDown)
        btnLock = findViewById(R.id.btnLock)
        recyclerView = findViewById(R.id.recyclerView)

        setupSpinner()
        setupRecyclerView()
        setupControls()
        updateLockState()
    }

    private fun loadData() {
        val workersJson = prefs.getString("workers", null)
        if (workersJson != null) {
            val type = object : TypeToken<MutableList<Worker>>() {}.type
            workers = gson.fromJson(workersJson, type)
        } else {
            workers = mutableListOf(
                Worker(0, "Работник 1"),
                Worker(1, "Работник 2"),
                Worker(2, "Работник 3"),
                Worker(3, "Работник 4")
            )
        }
        currentWorkerIndex = prefs.getInt("current_worker_index", 0)
        isLocked = prefs.getBoolean("is_locked", true)
    }

    private fun saveData() {
        prefs.edit().apply {
            putString("workers", gson.toJson(workers))
            putInt("current_worker_index", currentWorkerIndex)
            putBoolean("is_locked", isLocked)
            apply()
        }
    }

    private fun setupSpinner() {
        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, workers.map { it.name })
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        workerSpinner.adapter = adapter
        workerSpinner.setSelection(currentWorkerIndex)

        workerSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                currentWorkerIndex = position
                this@MainActivity.adapter.setOffset(workers[currentWorkerIndex].offset)
                saveData()
            }
            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }
    }

    private fun setupRecyclerView() {
        adapter = ScheduleAdapter()
        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = adapter
        adapter.setOffset(workers[currentWorkerIndex].offset)
        recyclerView.scrollToPosition(5000) // Scroll to "today" area
    }

    private fun setupControls() {
        btnShiftUp.setOnClickListener {
            workers[currentWorkerIndex].offset--
            adapter.setOffset(workers[currentWorkerIndex].offset)
            saveData()
        }

        btnShiftDown.setOnClickListener {
            workers[currentWorkerIndex].offset++
            adapter.setOffset(workers[currentWorkerIndex].offset)
            saveData()
        }

        btnLock.setOnClickListener {
            isLocked = !isLocked
            updateLockState()
            saveData()
        }
    }

    private fun updateLockState() {
        btnShiftUp.isEnabled = !isLocked
        btnShiftDown.isEnabled = !isLocked
        
        val alpha = if (isLocked) 0.5f else 1.0f
        btnShiftUp.alpha = alpha
        btnShiftDown.alpha = alpha

        if (isLocked) {
            btnLock.setImageResource(R.drawable.ic_lock_closed)
            btnLock.setColorFilter(ContextCompat.getColor(this, R.color.black))
        } else {
            btnLock.setImageResource(R.drawable.ic_lock_open)
            btnLock.setColorFilter(ContextCompat.getColor(this, R.color.colorPrimary))
        }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.main_menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_about -> {
                startActivity(Intent(this, AboutActivity::class.java))
                true
            }
            R.id.action_rename -> {
                showRenameDialog()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun showRenameDialog() {
        val builder = AlertDialog.Builder(this)
        builder.setTitle("Переименовать работника")

        val input = EditText(this)
        input.setText(workers[currentWorkerIndex].name)
        input.setSelection(input.text.length)
        builder.setView(input)

        builder.setPositiveButton("Сохранить") { dialog, _ ->
            val newName = input.text.toString().trim()
            if (newName.isNotEmpty() && newName.length <= 50) {
                workers[currentWorkerIndex].name = newName
                setupSpinner() // Refresh spinner
                saveData()
                dialog.dismiss()
            } else {
                Toast.makeText(this, "Невалидное имя", Toast.LENGTH_SHORT).show()
            }
        }
        builder.setNegativeButton("Отмена") { dialog, _ -> dialog.cancel() }

        builder.show()
    }
}
