@file:OptIn(androidx.compose.foundation.layout.ExperimentalLayoutApi::class)

package com.raizey.mantiq.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Apps
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.ContentCut
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Keyboard
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.testTagsAsResourceId
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.platform.testTag
import com.raizey.mantiq.MainActivity
import com.raizey.mantiq.data.InstalledApp
import com.raizey.mantiq.data.InstalledAppsRepository
import com.raizey.mantiq.data.SecureSnippetRepository
import com.raizey.mantiq.data.UserSnippet
import com.raizey.mantiq.ime.KeyboardAppearancePreferences
import com.raizey.mantiq.ime.KeyboardPalette
import com.raizey.mantiq.ime.KeyboardPreferences

private enum class AppTab(val title: String, val icon: ImageVector) {
    HOME("الرئيسية", Icons.Default.Home),
    SNIPPETS("اختصارات", Icons.Default.ContentCut),
    AI("AI", Icons.Default.AutoAwesome),
    THEMES("تصاميم", Icons.Default.Palette),
    SETTINGS("إعدادات", Icons.Default.Settings),
}

@Composable
fun MantiqApp(
    keyboardState: MainActivity.KeyboardState,
    snippetRepository: SecureSnippetRepository,
    appsRepository: InstalledAppsRepository,
    onEnableKeyboard: () -> Unit,
    onChooseKeyboard: () -> Unit,
    onCopyDiagnostics: () -> Unit,
) {
    CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
        var selectedTab by rememberSaveable { mutableStateOf(AppTab.HOME) }
        var snippets by remember { mutableStateOf(snippetRepository.list()) }
        val installedApps = remember { appsRepository.listLaunchableApps() }

        Scaffold(
            containerColor = MantiqBackground,
            bottomBar = {
                NavigationBar(containerColor = Color(0xFF0E161E), tonalElevation = 0.dp) {
                    AppTab.entries.forEach { tab ->
                        NavigationBarItem(
                            selected = selectedTab == tab,
                            onClick = { selectedTab = tab },
                            icon = { Icon(tab.icon, contentDescription = tab.title) },
                            label = { Text(tab.title, fontSize = 10.sp, maxLines = 1) },
                            colors = NavigationBarItemDefaults.colors(
                                selectedIconColor = MantiqAccent,
                                selectedTextColor = MantiqAccent,
                                indicatorColor = Color(0xFF183A32),
                                unselectedIconColor = MantiqMuted,
                                unselectedTextColor = MantiqMuted,
                            ),
                        )
                    }
                }
            },
        ) { padding ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .semantics { testTagsAsResourceId = true },
            ) {
                when (selectedTab) {
                    AppTab.HOME -> HomeScreen(
                        keyboardState = keyboardState,
                        snippets = snippets,
                        onEnableKeyboard = onEnableKeyboard,
                        onChooseKeyboard = onChooseKeyboard,
                        onOpenSnippets = { selectedTab = AppTab.SNIPPETS },
                    )
                    AppTab.SNIPPETS -> SnippetsScreen(
                        snippets = snippets,
                        installedApps = installedApps,
                        onSave = { snippet ->
                            snippetRepository.upsert(snippet)
                            snippets = snippetRepository.list()
                        },
                        onCreate = snippetRepository::create,
                        onDelete = { id ->
                            snippetRepository.delete(id)
                            snippets = snippetRepository.list()
                        },
                    )
                    AppTab.AI -> AiScreen()
                    AppTab.THEMES -> ThemesScreen()
                    AppTab.SETTINGS -> SettingsScreen(onCopyDiagnostics)
                }
            }
        }
    }
}

@Composable
private fun ScreenHeader(title: String, subtitle: String, icon: ImageVector) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        Surface(shape = MaterialTheme.shapes.medium, color = Color(0xFF173A31)) {
            Icon(icon, contentDescription = null, tint = MantiqAccent, modifier = Modifier.padding(12.dp).size(26.dp))
        }
        Column(modifier = Modifier.weight(1f)) {
            Text(title, style = MaterialTheme.typography.headlineMedium)
            Text(subtitle, style = MaterialTheme.typography.bodyMedium, color = MantiqMuted)
        }
    }
}

@Composable
private fun HomeScreen(
    keyboardState: MainActivity.KeyboardState,
    snippets: List<UserSnippet>,
    onEnableKeyboard: () -> Unit,
    onChooseKeyboard: () -> Unit,
    onOpenSnippets: () -> Unit,
) {
    var testText by rememberSaveable { mutableStateOf("") }
    val focusRequester = remember { FocusRequester() }
    val keyboardController = LocalSoftwareKeyboardController.current

    LazyColumn(
        modifier = Modifier.fillMaxSize().imePadding(),
        contentPadding = androidx.compose.foundation.layout.PaddingValues(20.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = Color.Transparent),
                shape = MaterialTheme.shapes.large,
            ) {
                Box(
                    modifier = Modifier
                        .background(Brush.linearGradient(listOf(Color(0xFF143D33), Color(0xFF101B25))))
                        .padding(22.dp),
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text("Mantiq", style = MaterialTheme.typography.displaySmall)
                        Text("لوحتك، اختصاراتك، وأدواتك الذكية في مكان واحد", color = Color(0xFFD1EAE2))
                        Surface(color = Color(0x3322E0A8), shape = CircleShape) {
                            Text("الإصدار 0.3.0", color = MantiqAccent, modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp), fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
        item {
            KeyboardStatusCard(keyboardState, onEnableKeyboard, onChooseKeyboard)
        }
        item {
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.fillMaxWidth()) {
                StatCard("اختصاراتك", snippets.size.toString(), Icons.Default.ContentCut, Modifier.weight(1f))
                StatCard("النشطة", snippets.count { it.enabled && it.allowedPackages.isNotEmpty() }.toString(), Icons.Default.Shield, Modifier.weight(1f))
            }
        }
        item {
            AppCard {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text("اختصاراتك الخاصة", style = MaterialTheme.typography.titleLarge)
                        Text("اكتب الاختصار والنص وحدد التطبيقات المسموحة.", color = MantiqMuted)
                    }
                    IconButton(onClick = onOpenSnippets) { Icon(Icons.Default.Add, contentDescription = "إضافة اختصار", tint = MantiqAccent) }
                }
            }
        }
        item {
            AppCard {
                Text("تجربة اللوحة", style = MaterialTheme.typography.titleLarge)
                Spacer(Modifier.height(8.dp))
                OutlinedTextField(
                    value = testText,
                    onValueChange = { testText = it },
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(min = 96.dp)
                        .focusRequester(focusRequester)
                        .testTag("keyboard_test_field"),
                    placeholder = { Text("اضغط هنا واكتب باستخدام Mantiq…") },
                )
                Spacer(Modifier.height(10.dp))
                Button(
                    onClick = {
                        focusRequester.requestFocus()
                        keyboardController?.show()
                    },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = keyboardState.selected,
                ) {
                    Icon(Icons.Default.Keyboard, contentDescription = null)
                    Spacer(Modifier.width(8.dp))
                    Text(if (keyboardState.selected) "فتح لوحة Mantiq" else "اختر Mantiq أولاً")
                }
            }
        }
    }
}

@Composable
private fun KeyboardStatusCard(
    state: MainActivity.KeyboardState,
    onEnable: () -> Unit,
    onChoose: () -> Unit,
) {
    val (title, detail, color) = when {
        state.selected -> Triple("اللوحة جاهزة", "Mantiq مفعّلة ومختارة كلوحتك الحالية.", MantiqAccent)
        state.enabled -> Triple("باقي خطوة واحدة", "اختر Mantiq كلوحة المفاتيح الحالية.", Color(0xFFFFC766))
        else -> Triple("ابدأ تفعيل اللوحة", "Android يطلب موافقتك قبل استخدام أي لوحة جديدة.", Color(0xFFFF7B86))
    }
    AppCard {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            Box(Modifier.size(10.dp).background(color, CircleShape))
            Column(Modifier.weight(1f)) {
                Text(title, style = MaterialTheme.typography.titleLarge)
                Text(detail, color = MantiqMuted)
            }
        }
        Spacer(Modifier.height(14.dp))
        if (!state.enabled) Button(onClick = onEnable, modifier = Modifier.fillMaxWidth()) { Text("تفعيل Mantiq") }
        else OutlinedButton(onClick = onChoose, modifier = Modifier.fillMaxWidth()) { Text(if (state.selected) "تغيير لوحة المفاتيح" else "اختيار Mantiq") }
    }
}

@Composable
private fun StatCard(label: String, value: String, icon: ImageVector, modifier: Modifier = Modifier) {
    Card(modifier, colors = CardDefaults.cardColors(containerColor = MantiqSurface), border = BorderStroke(1.dp, MantiqBorder)) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Icon(icon, null, tint = MantiqAccent)
            Text(value, style = MaterialTheme.typography.headlineMedium)
            Text(label, color = MantiqMuted)
        }
    }
}

@Composable
private fun AppCard(content: @Composable ColumnScope.() -> Unit) {
    Card(
        colors = CardDefaults.cardColors(containerColor = MantiqSurface),
        border = BorderStroke(1.dp, MantiqBorder),
        shape = MaterialTheme.shapes.large,
    ) {
        Column(Modifier.fillMaxWidth().padding(18.dp), content = content)
    }
}

@Composable
private fun SnippetsScreen(
    snippets: List<UserSnippet>,
    installedApps: List<InstalledApp>,
    onSave: (UserSnippet) -> Unit,
    onCreate: (String, String, Boolean, Set<String>) -> UserSnippet,
    onDelete: (String) -> Unit,
) {
    var showEditor by remember { mutableStateOf(false) }
    var editing by remember { mutableStateOf<UserSnippet?>(null) }
    var deleting by remember { mutableStateOf<UserSnippet?>(null) }

    Scaffold(
        containerColor = Color.Transparent,
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = { editing = null; showEditor = true },
                icon = { Icon(Icons.Default.Add, null) },
                text = { Text("اختصار جديد") },
            )
        },
    ) { inner ->
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(inner),
            contentPadding = androidx.compose.foundation.layout.PaddingValues(start = 20.dp, end = 20.dp, top = 20.dp, bottom = 100.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            item { ScreenHeader("الاختصارات", "أنت تكتبها وتحدد أين تعمل", Icons.Default.ContentCut) }
            if (snippets.isEmpty()) {
                item {
                    EmptyState(
                        title = "لا توجد اختصارات بعد",
                        body = "أنشئ أول اختصار، اكتب النص البديل، ثم اختر التطبيقات التي تسمح له بالعمل داخلها.",
                        icon = Icons.Default.ContentCut,
                    )
                }
            } else {
                items(snippets, key = UserSnippet::id) { snippet ->
                    SnippetCard(
                        snippet = snippet,
                        onEdit = { editing = snippet; showEditor = true },
                        onDelete = { deleting = snippet },
                    )
                }
            }
        }
    }

    if (showEditor) {
        SnippetEditorDialog(
            snippet = editing,
            installedApps = installedApps,
            onDismiss = { showEditor = false },
            onSave = { trigger, template, enabled, packages ->
                val value = editing?.copy(
                    trigger = trigger.trim(),
                    template = template.trim(),
                    enabled = enabled,
                    allowedPackages = packages,
                ) ?: onCreate(trigger, template, enabled, packages)
                onSave(value)
                showEditor = false
            },
        )
    }
    deleting?.let { snippet ->
        AlertDialog(
            onDismissRequest = { deleting = null },
            title = { Text("حذف الاختصار؟") },
            text = { Text("سيتم حذف ${snippet.trigger} نهائيًا من هذا الجهاز.") },
            confirmButton = { TextButton(onClick = { onDelete(snippet.id); deleting = null }) { Text("حذف", color = MaterialTheme.colorScheme.error) } },
            dismissButton = { TextButton(onClick = { deleting = null }) { Text("إلغاء") } },
        )
    }
}

@Composable
private fun SnippetCard(snippet: UserSnippet, onEdit: () -> Unit, onDelete: () -> Unit) {
    AppCard {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Surface(shape = MaterialTheme.shapes.small, color = Color(0xFF173A31)) {
                Text(snippet.trigger, color = MantiqAccent, fontWeight = FontWeight.Bold, modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp))
            }
            Spacer(Modifier.weight(1f))
            Box(Modifier.size(8.dp).background(if (snippet.enabled) MantiqAccent else MantiqMuted, CircleShape))
            IconButton(onClick = onEdit) { Icon(Icons.Default.Edit, "تعديل") }
            IconButton(onClick = onDelete) { Icon(Icons.Default.DeleteOutline, "حذف", tint = Color(0xFFFF7B86)) }
        }
        Text(snippet.template, maxLines = 3, overflow = TextOverflow.Ellipsis, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Spacer(Modifier.height(10.dp))
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Default.Apps, null, tint = MantiqMuted, modifier = Modifier.size(18.dp))
            Spacer(Modifier.width(6.dp))
            Text(
                if (snippet.allowedPackages.isEmpty()) "لن يعمل: لم تحدد تطبيقات" else "مسموح في ${snippet.allowedPackages.size} تطبيق",
                color = if (snippet.allowedPackages.isEmpty()) Color(0xFFFFC766) else MantiqMuted,
                style = MaterialTheme.typography.bodyMedium,
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SnippetEditorDialog(
    snippet: UserSnippet?,
    installedApps: List<InstalledApp>,
    onDismiss: () -> Unit,
    onSave: (String, String, Boolean, Set<String>) -> Unit,
) {
    var trigger by remember(snippet?.id) { mutableStateOf(snippet?.trigger.orEmpty()) }
    var template by remember(snippet?.id) { mutableStateOf(snippet?.template.orEmpty()) }
    var enabled by remember(snippet?.id) { mutableStateOf(snippet?.enabled ?: true) }
    var packages by remember(snippet?.id) { mutableStateOf(snippet?.allowedPackages ?: emptySet()) }
    var selectingApps by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf<String?>(null) }

    if (selectingApps) {
        AppPickerDialog(
            apps = installedApps,
            selected = packages,
            onSelectionChanged = { packages = it },
            onDone = { selectingApps = false },
        )
        return
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (snippet == null) "اختصار جديد" else "تعديل الاختصار") },
        text = {
            Column(Modifier.verticalScroll(rememberScrollState()), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(
                    value = trigger,
                    onValueChange = { trigger = it; error = null },
                    label = { Text("الاختصار") },
                    placeholder = { Text("مثال: @@mail أو !موعد") },
                    supportingText = { Text("بدون مسافات، ويُستبدل عند ضغط المسافة") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                )
                OutlinedTextField(
                    value = template,
                    onValueChange = { template = it; error = null },
                    label = { Text("النص البديل") },
                    placeholder = { Text("اكتب النص الذي تريد إدراجه") },
                    minLines = 4,
                    modifier = Modifier.fillMaxWidth(),
                )
                FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    AssistChip(onClick = { template += "{{time}}" }, label = { Text("+ الوقت") })
                    AssistChip(onClick = { template += "{{date}}" }, label = { Text("+ التاريخ") })
                    AssistChip(onClick = { template += "{{time+1.5h}}" }, label = { Text("+ ساعة ونصف") })
                }
                OutlinedButton(onClick = { selectingApps = true }, modifier = Modifier.fillMaxWidth()) {
                    Icon(Icons.Default.Apps, null)
                    Spacer(Modifier.width(8.dp))
                    Text(if (packages.isEmpty()) "اختيار التطبيقات (مطلوب للتشغيل)" else "التطبيقات المحددة: ${packages.size}")
                }
                if (packages.isEmpty()) Text("لن يعمل الاختصار في أي تطبيق حتى تحدد تطبيقًا واحدًا على الأقل.", color = Color(0xFFFFC766), style = MaterialTheme.typography.bodyMedium)
                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
                    Column(Modifier.weight(1f)) {
                        Text("الاختصار مفعّل", fontWeight = FontWeight.SemiBold)
                        Text("يمكنك إيقافه مؤقتًا دون حذفه", color = MantiqMuted, style = MaterialTheme.typography.bodyMedium)
                    }
                    Switch(checked = enabled, onCheckedChange = { enabled = it })
                }
                error?.let { Text(it, color = MaterialTheme.colorScheme.error) }
            }
        },
        confirmButton = {
            Button(onClick = {
                error = when {
                    trigger.isBlank() -> "اكتب الاختصار أولاً"
                    trigger.any(Char::isWhitespace) -> "الاختصار لا يقبل المسافات"
                    template.isBlank() -> "اكتب النص البديل"
                    else -> null
                }
                if (error == null) runCatching { onSave(trigger, template, enabled, packages) }
                    .onFailure { error = if (it.message == "Trigger already exists") "هذا الاختصار موجود مسبقًا" else "تعذر حفظ الاختصار" }
            }) { Text("حفظ") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("إلغاء") } },
    )
}

@Composable
private fun AppPickerDialog(
    apps: List<InstalledApp>,
    selected: Set<String>,
    onSelectionChanged: (Set<String>) -> Unit,
    onDone: () -> Unit,
) {
    var query by remember { mutableStateOf("") }
    val filtered = remember(query, apps) {
        if (query.isBlank()) apps else apps.filter { it.label.contains(query, true) || it.packageName.contains(query, true) }
    }
    AlertDialog(
        onDismissRequest = onDone,
        title = { Text("التطبيقات المسموحة") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedTextField(
                    value = query,
                    onValueChange = { query = it },
                    leadingIcon = { Icon(Icons.Default.Search, null) },
                    placeholder = { Text("ابحث عن تطبيق") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                )
                Text("تم تحديد ${selected.size}", color = MantiqAccent, fontWeight = FontWeight.Bold)
                LazyColumn(Modifier.heightIn(max = 420.dp)) {
                    items(filtered, key = InstalledApp::packageName) { app ->
                        Row(
                            modifier = Modifier.fillMaxWidth().clickable {
                                onSelectionChanged(if (app.packageName in selected) selected - app.packageName else selected + app.packageName)
                            }.padding(vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Checkbox(
                                checked = app.packageName in selected,
                                onCheckedChange = { checked -> onSelectionChanged(if (checked) selected + app.packageName else selected - app.packageName) },
                            )
                            Column(Modifier.weight(1f)) {
                                Text(app.label, fontWeight = FontWeight.SemiBold)
                                Text(app.packageName, color = MantiqMuted, fontSize = 11.sp, maxLines = 1)
                            }
                        }
                    }
                }
            }
        },
        confirmButton = { Button(onClick = onDone) { Icon(Icons.Default.Check, null); Spacer(Modifier.width(6.dp)); Text("تم") } },
        dismissButton = { TextButton(onClick = { onSelectionChanged(emptySet()) }) { Text("مسح التحديد") } },
    )
}

@Composable
private fun AiScreen() {
    var prompt by rememberSaveable { mutableStateOf("") }
    var selectedAction by rememberSaveable { mutableStateOf("إعادة صياغة") }
    LazyColumn(
        contentPadding = androidx.compose.foundation.layout.PaddingValues(20.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        modifier = Modifier.fillMaxSize(),
    ) {
        item { ScreenHeader("Mantiq AI", "مركز الكتابة والتحويل الذكي", Icons.Default.AutoAwesome) }
        item {
            Card(colors = CardDefaults.cardColors(containerColor = Color(0xFF122C27)), border = BorderStroke(1.dp, Color(0xFF2C6859))) {
                Row(Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                    Box(Modifier.size(10.dp).background(Color(0xFFFFC766), CircleShape))
                    Spacer(Modifier.width(10.dp))
                    Column {
                        Text("المحرك غير مربوط بعد", fontWeight = FontWeight.Bold)
                        Text("لن تُرسل نصوصك لأي خدمة قبل اختيار مزود AI ووضع المفتاح بأمان.", color = MantiqMuted)
                    }
                }
            }
        }
        item {
            AppCard {
                Text("ماذا تريد أن تفعل؟", style = MaterialTheme.typography.titleLarge)
                Spacer(Modifier.height(10.dp))
                FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    listOf("إعادة صياغة", "تدقيق", "تلخيص", "رسمي", "ترجمة").forEach { action ->
                        FilterChip(selected = selectedAction == action, onClick = { selectedAction = action }, label = { Text(action) })
                    }
                }
                Spacer(Modifier.height(12.dp))
                OutlinedTextField(
                    value = prompt,
                    onValueChange = { prompt = it },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 7,
                    label = { Text("النص أو الطلب") },
                    placeholder = { Text("اكتب النص الذي تريد معالجته…") },
                )
                Spacer(Modifier.height(12.dp))
                Button(onClick = {}, enabled = false, modifier = Modifier.fillMaxWidth()) { Text("ربط مزود AI أولاً") }
            }
        }
        item { EmptyState("قريبًا داخل اللوحة", "بعد ربط المحرك ستظهر نفس الأدوات عند تحديد النص من شريط Mantiq.", Icons.Default.AutoAwesome) }
    }
}

@Composable
private fun ThemesScreen() {
    val context = androidx.compose.ui.platform.LocalContext.current
    var selectedId by remember { mutableStateOf(KeyboardAppearancePreferences.selectedId(context)) }
    LazyColumn(
        contentPadding = androidx.compose.foundation.layout.PaddingValues(20.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp),
        modifier = Modifier.fillMaxSize(),
    ) {
        item { ScreenHeader("التصاميم", "اختر هوية لوحة المفاتيح", Icons.Default.Palette) }
        items(KeyboardAppearancePreferences.palettes, key = KeyboardPalette::id) { palette ->
            ThemeCard(
                palette = palette,
                selected = selectedId == palette.id,
                onClick = {
                    KeyboardAppearancePreferences.select(context, palette.id)
                    selectedId = palette.id
                },
            )
        }
        item { Text("يظهر التصميم المختار عند فتح اللوحة من جديد.", color = MantiqMuted, style = MaterialTheme.typography.bodyMedium) }
    }
}

@Composable
private fun ThemeCard(palette: KeyboardPalette, selected: Boolean, onClick: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth().clickable(onClick = onClick),
        colors = CardDefaults.cardColors(containerColor = Color(palette.background)),
        border = BorderStroke(if (selected) 2.dp else 1.dp, if (selected) Color(palette.accent) else MantiqBorder),
    ) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(palette.name, style = MaterialTheme.typography.titleLarge)
                Spacer(Modifier.weight(1f))
                if (selected) Icon(Icons.Default.Check, "مختار", tint = Color(palette.accent))
            }
            Row(horizontalArrangement = Arrangement.spacedBy(7.dp), modifier = Modifier.fillMaxWidth()) {
                listOf("ض", "ص", "ث", "ق", "ف", "غ").forEach { letter ->
                    Box(
                        Modifier.weight(1f).height(44.dp).background(Color(palette.key), MaterialTheme.shapes.small).border(1.dp, Color(palette.border), MaterialTheme.shapes.small),
                        contentAlignment = Alignment.Center,
                    ) { Text(letter) }
                }
            }
        }
    }
}

@Composable
private fun SettingsScreen(onCopyDiagnostics: () -> Unit) {
    val context = androidx.compose.ui.platform.LocalContext.current
    var haptics by remember { mutableStateOf(KeyboardPreferences.hapticsEnabled(context)) }
    var sound by remember { mutableStateOf(KeyboardPreferences.soundEnabled(context)) }
    LazyColumn(
        contentPadding = androidx.compose.foundation.layout.PaddingValues(20.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        modifier = Modifier.fillMaxSize(),
    ) {
        item { ScreenHeader("الإعدادات", "تحكم في تجربة الكتابة والخصوصية", Icons.Default.Settings) }
        item {
            AppCard {
                SettingSwitch("الاهتزاز عند الضغط", "استجابة لمسية خفيفة لكل مفتاح", haptics) {
                    haptics = it; KeyboardPreferences.setHapticsEnabled(context, it)
                }
                HorizontalDivider(color = MantiqBorder, modifier = Modifier.padding(vertical = 8.dp))
                SettingSwitch("صوت المفاتيح", "استخدام صوت النظام عند الكتابة", sound) {
                    sound = it; KeyboardPreferences.setSoundEnabled(context, it)
                }
            }
        }
        item {
            AppCard {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Lock, null, tint = MantiqAccent)
                    Spacer(Modifier.width(10.dp))
                    Text("الخصوصية", style = MaterialTheme.typography.titleLarge)
                }
                Spacer(Modifier.height(10.dp))
                Text("الاختصارات مشفرة بمفتاح محفوظ داخل Android Keystore. لا توجد صلاحية إنترنت في النسخة الحالية، ولا يتم حفظ ما تكتبه.", color = MantiqMuted)
            }
        }
        item { OutlinedButton(onClick = onCopyDiagnostics, modifier = Modifier.fillMaxWidth()) { Text("نسخ تقرير التشخيص") } }
        item { Text("Mantiq 0.3.0 • Android 8–16", color = MantiqMuted, modifier = Modifier.fillMaxWidth()) }
    }
}

@Composable
private fun SettingSwitch(title: String, subtitle: String, checked: Boolean, onChecked: (Boolean) -> Unit) {
    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
        Column(Modifier.weight(1f)) {
            Text(title, fontWeight = FontWeight.SemiBold)
            Text(subtitle, color = MantiqMuted, style = MaterialTheme.typography.bodyMedium)
        }
        Switch(checked = checked, onCheckedChange = onChecked)
    }
}

@Composable
private fun EmptyState(title: String, body: String, icon: ImageVector) {
    AppCard {
        Icon(icon, null, tint = MantiqAccent, modifier = Modifier.size(34.dp))
        Spacer(Modifier.height(12.dp))
        Text(title, style = MaterialTheme.typography.titleLarge)
        Spacer(Modifier.height(4.dp))
        Text(body, color = MantiqMuted)
    }
}
