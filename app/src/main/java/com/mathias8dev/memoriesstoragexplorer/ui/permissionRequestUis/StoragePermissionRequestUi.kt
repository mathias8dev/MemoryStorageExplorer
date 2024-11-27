package com.mathias8dev.memoriesstoragexplorer.ui.permissionRequestUis

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Storage
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.mathias8dev.memoriesstoragexplorer.ui.composables.StandardButton
import com.mathias8dev.memoriesstoragexplorer.ui.composables.StandardDialog
import com.mathias8dev.permissionhelper.permission.OneShotPermissionsRequestUi
import com.mathias8dev.permissionhelper.permission.Permission


object StoragePermissionRequestUi : OneShotPermissionsRequestUi {
    @Composable
    override fun OnPermissionsGotoSettings(permissions: List<Permission>, onProceed: (Boolean) -> Unit) {
        StandardDialog(
            backgroundColor = MaterialTheme.colorScheme.surface,
            onDismissRequest = {
                onProceed(false)
            }
        ) {
            Image(
                modifier = Modifier
                    .align(Alignment.CenterHorizontally)
                    .padding(top = 16.dp)
                    .size(width = 256.dp, height = 72.dp),
                imageVector = Icons.Default.Storage,
                colorFilter = ColorFilter.tint(MaterialTheme.colorScheme.onSurface),
                contentDescription = null
            )

            Text(
                modifier = Modifier
                    .padding(top = 16.dp)
                    .fillMaxWidth(),
                text = "Nous avons besoin d'accéder à votre espace de stockage.",
                fontWeight = FontWeight.SemiBold,
                textAlign = TextAlign.Center
            )

            Text(
                modifier = Modifier
                    .padding(top = 16.dp)
                    .fillMaxWidth(),
                text = "Pour cela, vous devez vous rendre dans les paramètres pour nous donner l'accès.",
                fontWeight = FontWeight.SemiBold,
                textAlign = TextAlign.Center
            )

            StandardButton(
                modifier = Modifier
                    .padding(top = 32.dp)
                    .height(45.dp),
                text = "OK",
                colors = ButtonDefaults.elevatedButtonColors(
                    containerColor = MaterialTheme.colorScheme.secondaryContainer,
                    contentColor = MaterialTheme.colorScheme.onSecondaryContainer
                ),
                onClick = {
                    onProceed(true)
                }
            )

        }
    }

    @Composable
    override fun OnPermissionsInitialRequest(permissions: List<Permission>, onProceed: (Boolean) -> Unit) {
        StandardDialog(
            backgroundColor = MaterialTheme.colorScheme.surface,
            onDismissRequest = {
                onProceed(false)
            }
        ) {
            Image(
                modifier = Modifier
                    .align(Alignment.CenterHorizontally)
                    .padding(top = 16.dp)
                    .size(width = 256.dp, height = 72.dp),
                imageVector = Icons.Default.Storage,
                colorFilter = ColorFilter.tint(MaterialTheme.colorScheme.onSurface),
                contentDescription = null
            )

            Text(
                modifier = Modifier
                    .padding(top = 16.dp)
                    .fillMaxWidth(),
                text = "Nous avons besoin d'accéder à votre espace de stockage.",
                fontWeight = FontWeight.SemiBold,
                textAlign = TextAlign.Center
            )

            StandardButton(
                modifier = Modifier
                    .padding(top = 32.dp)
                    .height(45.dp),
                text = "J'accepte",
                colors = ButtonDefaults.elevatedButtonColors(
                    containerColor = MaterialTheme.colorScheme.secondaryContainer,
                    contentColor = MaterialTheme.colorScheme.onSecondaryContainer
                ),
                onClick = {
                    onProceed(true)
                }
            )

            StandardButton(
                modifier = Modifier
                    .padding(top = 8.dp)
                    .height(45.dp),
                text = "Je n'accepte pas",
                colors = ButtonDefaults.elevatedButtonColors(
                    containerColor = MaterialTheme.colorScheme.errorContainer,
                    contentColor = MaterialTheme.colorScheme.onErrorContainer
                ),
                onClick = {
                    onProceed(false)
                }
            )
        }
    }

    @Composable
    override fun OnPermissionsShowRationale(permissions: List<Permission>, onProceed: (Boolean) -> Unit) {
        StandardDialog(
            backgroundColor = MaterialTheme.colorScheme.surface,
            onDismissRequest = {
                onProceed(false)
            }
        ) {
            Image(
                modifier = Modifier
                    .align(Alignment.CenterHorizontally)
                    .padding(top = 16.dp)
                    .size(width = 256.dp, height = 72.dp),
                imageVector = Icons.Default.Storage,
                colorFilter = ColorFilter.tint(MaterialTheme.colorScheme.onSurface),
                contentDescription = null
            )

            Text(
                modifier = Modifier
                    .padding(top = 16.dp)
                    .fillMaxWidth(),
                text = "Nous avons besoin d'accéder à votre espace de stockage.",
                fontWeight = FontWeight.SemiBold,
                textAlign = TextAlign.Center
            )

            Text(
                modifier = Modifier
                    .padding(top = 16.dp)
                    .fillMaxWidth(),
                text = "Pour cela, cliquez sur 'J'accepte' lorsque la permission vous sera demandée.",
                fontWeight = FontWeight.SemiBold,
                textAlign = TextAlign.Center
            )

            StandardButton(
                modifier = Modifier
                    .padding(top = 32.dp)
                    .height(45.dp),
                text = "OK",
                colors = ButtonDefaults.elevatedButtonColors(
                    containerColor = MaterialTheme.colorScheme.secondaryContainer,
                    contentColor = MaterialTheme.colorScheme.onSecondaryContainer
                ),
                onClick = {
                    onProceed(true)
                }
            )

        }
    }

}