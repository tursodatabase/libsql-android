package tech.turso.libsql.examples.todo

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.draggable
import androidx.compose.foundation.gestures.rememberDraggableState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonColors
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import tech.turso.libsql.Database
import tech.turso.libsql.EmbeddedReplicaDatabase
import tech.turso.libsql.Libsql
import tech.turso.libsql.examples.todo.ui.theme.TodoTheme
import java.util.UUID
import kotlin.concurrent.fixedRateTimer

data class Item(
    val id: UUID = UUID.randomUUID(), var title: String, var isCompleted: Boolean = false
)

class MainActivity : ComponentActivity() {
    private val db: Database by lazy { Libsql.open(this.filesDir.path + "/todo.db") }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        actionBar?.hide()

        val db = this.db
        db.connect().use {
            it.execute("create table if not exists todos(id text, title text, is_completed integer)")
        }

        enableEdgeToEdge()
        setContent {
            val padding = 24.dp
            var text by remember { mutableStateOf("") }
            var todos by remember { mutableStateOf(getAllTodos(db)) }

            val coroutineScope = rememberCoroutineScope()

            DisposableEffect(Unit) {
                val timer = fixedRateTimer(initialDelay = 0, period = 1000) {
                    coroutineScope.launch(Dispatchers.Main) {
                        Log.i("EmbeddedReplica", "Syncing!")
                        if (db is EmbeddedReplicaDatabase) db.sync()
                        todos = getAllTodos(db)
                    }
                }

                onDispose { timer.cancel() }
            }

            TodoTheme {
                Box(
                    Modifier.background(
                        Brush.verticalGradient(
                            listOf(Color(0xff15262b), Color(0xff2e5857))
                        )
                    )
                ) {
                    Column(
                        modifier = Modifier.fillMaxSize()
                            .padding(horizontal = padding, vertical = padding * Math.E.toFloat()),
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            TextField(
                                singleLine = true,
                                value = text,
                                onValueChange = { text = it },
                                shape = RoundedCornerShape(10.dp),
                                modifier = Modifier.weight(1f).clip(RoundedCornerShape(10.dp)),
                            )
                            Spacer(modifier = Modifier.padding(10.dp))
                            IconButton(colors = IconButtonColors(
                                containerColor = MaterialTheme.colorScheme.primaryContainer,
                                contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                                disabledContentColor = Color.Unspecified,
                                disabledContainerColor = Color.Unspecified
                            ), modifier = Modifier.size(42.dp).aspectRatio(1f), onClick = {
                                if (text.isNotEmpty()) {
                                    val item = Item(title = text)
                                    db.connect().use {
                                        it.execute(
                                            "insert into todos values (?, ?, ?)",
                                            item.id.toString(),
                                            item.title,
                                            if (item.isCompleted) 1 else 0,
                                        )
                                    }
                                    todos = buildList {
                                        addAll(todos)
                                        add(item)
                                    }
                                    text = ""
                                }
                            }) {
                                Icon(Icons.Default.Add, contentDescription = null)
                            }
                        }
                        Spacer(Modifier.size(padding))
                        LazyColumn {
                            items(todos, key = { it.id }) { item ->
                                TodoItem(
                                    item,
                                    onCheck = { checkedItem ->
                                        todos = todos.map {
                                            if (checkedItem.id == it.id) it.copy(isCompleted = !it.isCompleted) else it
                                        }

                                        coroutineScope.launch {
                                            db.connect().use { conn ->
                                                conn.execute(
                                                    "update todos set is_completed = ? where id = ?",
                                                    if (item.isCompleted) 0 else 1,
                                                    item.id.toString(),
                                                )
                                            }
                                        }
                                    },
                                    onDelete = { deletedItem ->
                                        todos = todos.filterNot { deletedItem.id == it.id }

                                        coroutineScope.launch {
                                            db.connect().use {
                                                it.execute(
                                                    "delete from todos where id = ?",
                                                    item.id.toString(),
                                                )
                                            }
                                        }
                                    },
                                )
                                Spacer(modifier = Modifier.padding(6.dp))
                            }
                        }
                    }
                }
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        db.close()
    }
}

@Composable
fun TodoItem(
    item: Item, onCheck: (Item) -> Unit, onDelete: (Item) -> Unit
) {
    var offsetX by remember { mutableFloatStateOf(0f) }
    val animatedOffsetX by animateFloatAsState(
        targetValue = offsetX,
        animationSpec = spring(),
    )

    Box(modifier = Modifier.offset { IntOffset(animatedOffsetX.toInt(), 0) }.border(
            width = 2.dp,
            color = MaterialTheme.colorScheme.onPrimaryContainer,
            shape = RoundedCornerShape(10.dp),
        )) {
        Row(
            modifier = Modifier.padding(
                    horizontal = 12.dp,
                    vertical = 14.dp,
                ).draggable(orientation = Orientation.Horizontal,
                    state = rememberDraggableState { delta ->
                        if (offsetX + delta < 0) {
                            offsetX += delta
                        }
                    },
                    onDragStopped = {
                        if (offsetX < -200) {
                            offsetX = -1200f
                            delay(300)
                            onDelete(item)
                        } else {
                            offsetX = 0f
                        }
                    }),
        ) {
            Text(
                text = item.title,
                color = MaterialTheme.colorScheme.onBackground,
                modifier = Modifier.weight(10f),
                textDecoration = if (item.isCompleted) TextDecoration.LineThrough else null
            )
            Spacer(Modifier.padding(2.dp))
            if (item.isCompleted) {
                IconButton(colors = IconButtonColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                    disabledContentColor = Color.Unspecified,
                    disabledContainerColor = Color.Unspecified
                ),
                    modifier = Modifier.size(20.dp).aspectRatio(1f),
                    onClick = { onCheck(item) }) {
                    Icon(
                        Icons.Default.CheckCircle,
                        contentDescription = null,
                        modifier = Modifier.weight(1f),
                    )
                }
            } else {
                IconButton(colors = IconButtonColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                    disabledContentColor = Color.Unspecified,
                    disabledContainerColor = Color.Unspecified,
                ),
                    modifier = Modifier.size(20.dp).aspectRatio(1f),
                    onClick = { onCheck(item) }) {}
            }
        }
    }
}

fun getAllTodos(db: Database): List<Item> = db.connect().use {
    it.query("select * from todos").use { rows ->
        rows.map { row ->
            Item(
                id = UUID.fromString(row[0] as String),
                title = row[1] as String,
                isCompleted = row[2] as Long == 1L,
            )
        }
    }
}
