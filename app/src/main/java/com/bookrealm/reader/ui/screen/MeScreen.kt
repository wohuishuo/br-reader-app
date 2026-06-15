package com.bookrealm.reader.ui.screen

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Login
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp

@Composable
fun MeScreen(
    loggedIn: Boolean,
    account: String,
    username: String,
    onLogin: (String, String) -> Unit,
) {
    var userAccount by remember { mutableStateOf("root") }
    var password by remember { mutableStateOf("12345678") }
    Column(
        Modifier.fillMaxSize().padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        horizontalAlignment = Alignment.Start,
    ) {
        Text("我的", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
        if (loggedIn) {
            Card {
                Column(Modifier.fillMaxWidth().padding(18.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("已登录", color = MaterialTheme.colorScheme.primary)
                    Text(username.ifBlank { account }, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                    Text("v2 后续会在这里放阅读统计、成就、导入任务和会员能力。")
                }
            }
        } else {
            OutlinedTextField(value = userAccount, onValueChange = { userAccount = it }, label = { Text("账号") })
            OutlinedTextField(
                value = password,
                onValueChange = { password = it },
                label = { Text("密码") },
                visualTransformation = PasswordVisualTransformation(),
            )
            Button(onClick = { onLogin(userAccount, password) }) {
                Icon(Icons.Filled.Login, contentDescription = null)
                Spacer(Modifier.width(8.dp))
                Text("登录")
            }
            Text("默认演示账号 root / 12345678。")
        }
    }
}
