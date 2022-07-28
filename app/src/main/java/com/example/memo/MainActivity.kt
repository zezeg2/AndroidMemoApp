package com.example.memo

import android.annotation.SuppressLint
import android.os.AsyncTask
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.recyclerview.widget.LinearLayoutManager
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() , OnDeleteListener{

    lateinit var db : MemoDatabase
    var memoList = listOf<MemoEntity>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        db = MemoDatabase.getInstance(this)!!

        button_add.setOnClickListener {
            val memo = MemoEntity(null, edit_text.text.toString())
            edit_text.setText("")
            insertMemo(memo)
        }
        recyclerView.layoutManager = LinearLayoutManager(this)

        getAllMemo()
    }

    /**
     * 1. Insert Data
     * 2. Get Data
     * 3. Delete Data
     * 4. Set RecyclerView
     */

    @SuppressLint("StaticFieldLeak")
    fun insertMemo(memoEntity: MemoEntity){
        //1. MainThread vs WorkerThread(background Thread)

        val insertTask = object : AsyncTask<Unit,Unit,Unit>(){
            override fun doInBackground(vararg params: Unit?) {
                db.memoDAO().insert(memoEntity)
            }


            override fun onPostExecute(result: Unit?) {
                super.onPostExecute(result)
                getAllMemo()
            }

        }

        insertTask.execute()
    }

    fun getAllMemo(){
        val getTask = object : AsyncTask<Unit,Unit,Unit>(){
            override fun doInBackground(vararg params: Unit?) {
                memoList = db.memoDAO().getAll()
            }

            override fun onPostExecute(result: Unit?) {
                setRecyclerView(memoList)
            }

        }

        getTask.execute()
    }

    fun deleteMemo(memoEntity: MemoEntity){
        val deleteTask = object : AsyncTask<Unit,Unit,Unit>(){
            override fun doInBackground(vararg params: Unit?) {
                db.memoDAO().delete(memoEntity)
            }

            override fun onPostExecute(result: Unit?) {
                super.onPostExecute(result)
                getAllMemo()
            }

        }
        deleteTask.execute()
    }

    fun setRecyclerView(memoList : List<MemoEntity>){
        recyclerView.adapter = MyAdapter(this, memoList, this)
    }

    override fun onDeleteListener(memoEntity: MemoEntity) {
        deleteMemo(memoEntity)
    }
}