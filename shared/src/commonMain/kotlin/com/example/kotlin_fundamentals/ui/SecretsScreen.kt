package com.example.kotlin_fundamentals.ui

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.example.kotlin_fundamentals.security.SecurityStorage
import com.example.kotlin_fundamentals.security.StorageMechanism
import kotlinx.coroutines.launch

@Composable
fun SecretsScreen(securityStorage: SecurityStorage) {
    var keyInput by remember { mutableStateOf("") }
    var valueInput by remember { mutableStateOf("") }
    var selectedMechanism by remember { mutableStateOf(StorageMechanism.SHARED_PREFERENCES) }
    var resultMessage by remember { mutableStateOf("") }

    val coroutineScope = rememberCoroutineScope()

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
    ) {
        Text("Gestión de Secretos", modifier = Modifier.padding(bottom = 16.dp))

        OutlinedTextField(
            value = keyInput,
            onValueChange = { keyInput = it },
            label = { Text("Llave") },
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 8.dp)
        )

        OutlinedTextField(
            value = valueInput,
            onValueChange = { valueInput = it },
            label = { Text("Valor") },
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp)
        )

        Text("Mecanismo de Almacenamiento:", modifier = Modifier.padding(bottom = 8.dp))

        StorageMechanism.entries.forEach { mechanism ->
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(bottom = 8.dp)
            ) {
                RadioButton(
                    selected = selectedMechanism == mechanism,
                    onClick = { selectedMechanism = mechanism }
                )
                Text(mechanism.toString(), modifier = Modifier.padding(start = 8.dp))
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Row(modifier = Modifier.fillMaxWidth()) {
            Button(
                onClick = {
                    coroutineScope.launch {
                        try {
                            securityStorage.saveSecret(keyInput, valueInput, selectedMechanism)
                            resultMessage = "Secreto guardado exitosamente"
                        } catch (e: Exception) {
                            resultMessage = "Error al guardar: ${e.message}"
                        }
                    }
                },
                modifier = Modifier
                    .weight(1f)
                    .padding(end = 4.dp)
            ) {
                Text("Guardar")
            }

            Button(
                onClick = {
                    coroutineScope.launch {
                        try {
                            val secret = securityStorage.getSecret(keyInput, selectedMechanism)
                            resultMessage = if (secret != null) {
                                "Valor: $secret"
                            } else {
                                "Secreto no encontrado"
                            }
                        } catch (e: Exception) {
                            resultMessage = "Error al recuperar: ${e.message}"
                        }
                    }
                },
                modifier = Modifier
                    .weight(1f)
                    .padding(start = 4.dp)
            ) {
                Text("Recuperar")
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        if (resultMessage.isNotEmpty()) {
            Text(
                text = resultMessage,
                color = if (resultMessage.contains("Error")) Color.Red else Color.Green,
                modifier = Modifier.padding(top = 8.dp)
            )
        }
    }
}

