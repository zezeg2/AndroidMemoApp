# Memo App

### 요구사항

- RecycleView를 사용하여 성능 최적화
- RoomDatabase 사용
- 앱을 실행시켰을때 데이터베이스로부터 목록을 가져와 화면에 보여준다(read)
- 버튼을 눌러 데이터베이스에 저장하고 목록을 가져온다(insert, read)
- LongClick을 통해 메모를 삭제하고 목록을 가져온다.(delete)

### ActivityMain Layout

ConstraintLayout

- EditText : 사용자 입력을 받음
- Button : 목록에 추가하는 버튼
- RecycleView :  동적 목록 생성

```xml
<?xml version="1.0" encoding="utf-8"?>
<androidx.constraintlayout.widget.ConstraintLayoutxmlns:android="http://schemas.android.com/apk/res/android"
    xmlns:app="http://schemas.android.com/apk/res-auto"
    xmlns:tools="http://schemas.android.com/tools"
    android:layout_width="match_parent"
    android:layout_height="match_parent"
    tools:context=".MainActivity">

    <TextView
android:id="@+id/title"
        android:layout_width="wrap_content"
        android:layout_height="wrap_content"
        android:layout_marginTop="32dp"
        android:text="Memo List"
        android:textSize="32sp"
        android:textStyle="bold"
        app:layout_constraintEnd_toEndOf="parent"
        app:layout_constraintStart_toStartOf="parent"
        app:layout_constraintTop_toTopOf="parent" />

    <EditText
android:id="@+id/edit_text"
        android:layout_width="280dp"
        android:layout_height="wrap_content"
        android:layout_marginStart="22dp"
        android:layout_marginBottom="12dp"
        android:hint="Add memo..."
        app:layout_constraintBottom_toBottomOf="parent"
        app:layout_constraintStart_toStartOf="parent" />

    <Button
android:id="@+id/button_add"
        android:layout_width="0dp"
        android:layout_height="wrap_content"
        android:layout_margin="12dp"
        android:text="Add"
        android:textAllCaps="false"
        android:background="@drawable/button_red"
        app:layout_constraintBottom_toBottomOf="parent"
        app:layout_constraintEnd_toEndOf="parent"
        app:layout_constraintStart_toEndOf="@id/edit_text"/>

    <androidx.recyclerview.widget.RecyclerView
android:id="@+id/recyclerView"
        android:layout_width="match_parent"
        android:layout_height="0dp"
        android:layout_margin="20dp"
        app:layout_constraintBottom_toTopOf="@+id/edit_text"
        app:layout_constraintTop_toBottomOf="@+id/title"
        tools:layout_editor_absoluteX="0dp" />

</androidx.constraintlayout.widget.ConstraintLayout>
```

### item_memo

RecycleView를 통해 동적으로 생성하는 View Group

ConstraintLayout 사용

```xml
<?xml version="1.0" encoding="utf-8"?>
<androidx.constraintlayout.widget.ConstraintLayoutxmlns:android="http://schemas.android.com/apk/res/android"
    xmlns:app="http://schemas.android.com/apk/res-auto"
    xmlns:tools="http://schemas.android.com/tools"
    android:id="@+id/root"
    android:layout_width="match_parent"
    android:layout_height="100dp">

    <ImageView
android:id="@+id/check"
        android:layout_width="30dp"
        android:layout_height="30dp"
        android:layout_marginStart="16dp"
        android:src="@drawable/icon_check"
        app:layout_constraintBottom_toBottomOf="parent"
        app:layout_constraintStart_toStartOf="parent"
        app:layout_constraintTop_toTopOf="parent" />

    <TextView
android:id="@+id/text_view_memo"
        android:layout_width="wrap_content"
        android:layout_height="wrap_content"
        android:layout_marginStart="24dp"
        android:text=""
        android:textSize="16sp"
        app:layout_constraintBottom_toBottomOf="parent"
        app:layout_constraintStart_toEndOf="@id/check"
        app:layout_constraintTop_toTopOf="parent" />

</androidx.constraintlayout.widget.ConstraintLayout>
```

### MemoEntity

- id → AutoGenerate
- memo

```kotlin
@Entity(tableName = "memo")
data class MemoEntity(

    @PrimaryKey(autoGenerate = true)
    var id : Long?,
    var memo: String) {

}
```

### MemoDatabase

- RoomDatabase 생성

```kotlin
@Database(entities = arrayOf(MemoEntity::class), version = 1)
abstract class MemoDatabase : RoomDatabase(){
    abstract fun memoDAO() : MemoDAO

    companion object{
        var INSTANCE : MemoDatabase? = null
        fun getInstance(context: Context) : MemoDatabase?{
            if (INSTANCE == null){
                synchronized(MemoDatabase::class){
                    INSTANCE = Room.databaseBuilder(context.applicationContext,
                    MemoDatabase::class.java, "memo.db")
                        .fallbackToDestructiveMigration()
                        .build()
                }
            }
            return INSTANCE
        }
    }
}
```

### MemoDAO

- insert() → MemoEntity를 데이터베이스에 insert, 키 중복시 Replace
- getAll() → 데이터베이스에 저장된 모든 값을 List<MemoEntity>로 반환
- delete → 데이터베이스에서 MemoEntity에 해당하는 레코드를 삭제

```kotlin
@Dao
interface MemoDAO {
    @Insert(onConflict = REPLACE)
    fun insert(memo : MemoEntity)

    @Query("select * from memo")
    fun getAll() : List<MemoEntity>

    @Delete
    fun delete(memo: MemoEntity)
}
```

### MainActivity

```kotlin
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

    fun insertMemo(memoEntity: MemoEntity){

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

    override fun onDeleteListenr(memoEntity: MemoEntity) {
        deleteMemo(memoEntity)
    }
}
```

- onDeleteListener를 별도로 인터페이스로 생성하고 이를 MainActivity에서 구현

    ```kotlin
    interface OnDeleteListener {
        fun onDeleteListener(memoEntity: MemoEntity)
    }
    ```

- insertMemo
    - AsyncTask를 통해 백그라운드에서 onClickListner로부터 받은 MemoEntity를 DAO를 통해 데이터베이스에 insert
    - onPostExecute에서 데이터베이스로부터 모든 Memo를 쿼리하는 getAllMemo 실행
- getAllMemo
    - AsyncTask를 통해 백그라운드에서 DAO를 통해 데이터베이스의 모든 레코드를 List<MemoEntity>로 받는다
    - onPostExecute에서  setRecyclerView 메소드 실행
- setRecyclerView
    - RecyclerView.Adaptor를 오버라이딩한 MyAdapter를 생성하고 recyclerView.adapter에 매핑
        - 파라미터에 memoList와 conetext(this), onDeleteListener(this) 전달
    - MyAdapter
        - MyViewHolder 정의
        - getItemCount, onCreateViewHolder, onBindViewHolder 구현해야 한다
        - onDeleteListener를 파라미터로 받아 각 ViewHolder객체에서 DAO를통해 delete 실행(root → item_memo Layout)

        ```kotlin
        class MyAdapter(
            private var context: Context, private var list: List<MemoEntity>, var onDeleteListener : OnDeleteListener
        ) : RecyclerView.Adapter<MyAdapter.MyViewHolder>() {
        
            inner class MyViewHolder(itemView : View) : RecyclerView.ViewHolder(itemView){
                val memo: TextView = itemView.text_view_memo
                val root: ConstraintLayout = itemView.root
            }
            override fun getItemCount(): Int {
                return list.size
            }
        
            override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
                val itemView = LayoutInflater.from(context).inflate(R.layout.item_memo,parent, false)
                return MyViewHolder(itemView)
            }
        
            override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
                val memo = list[position]
                holder.memo.text = memo.memo
        					
        				//
                holder.root.setOnLongClickListener(object : View.OnLongClickListener {
                    override fun onLongClick(v: View?): Boolean {
                        onDeleteListener.onDeleteListener(memo)
                        return true
                    }
                })
            }
        
        }
        ```
        
<img width="413" alt="image" src="https://user-images.githubusercontent.com/71999370/181495320-ea6c4afc-f56d-4ba3-ab2e-248c432d314a.png">
<img width="408" alt="image" src="https://user-images.githubusercontent.com/71999370/181495334-0e9e5fe3-7aed-476a-a558-828d18f0048f.png">
