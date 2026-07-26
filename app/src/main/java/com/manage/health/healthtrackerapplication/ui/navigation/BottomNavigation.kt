package com.manage.health.healthtrackerapplication.ui.navigation

import android.widget.Space
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Analytics
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.Bedtime

import androidx.compose.material.icons.filled.DirectionsRun
import androidx.compose.material.icons.filled.DirectionsWalk
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.LocalDrink
import androidx.compose.material.icons.filled.Settings

import androidx.compose.material.icons.filled.WaterDrop
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.manage.health.healthtrackerapplication.ui.theme.HealthBlue
import com.manage.health.healthtrackerapplication.ui.theme.HealthGreen
import com.manage.health.healthtrackerapplication.ui.theme.HealthPurple
import com.manage.health.healthtrackerapplication.ui.theme.HealthTeal

sealed class Screen(
    val route: String,
    val title: String,
    val icon: ImageVector,
    val selectedIcon: ImageVector? = null,
    val color: Color
) {

    object Dashboard : Screen(
        route = "dashboard",
        title = "Home",
        icon = Icons.Default.Home,
        selectedIcon = Icons.Default.Home,
        color = HealthBlue
    )

    object Water : Screen(
        route = "water",
        title = "Water",
        icon = Icons.Default.LocalDrink,
        selectedIcon = Icons.Default.WaterDrop,
        color = HealthTeal
    )

    object Step: Screen(
        route = "steps",
        title = "Steps",
        icon = Icons.Default.DirectionsWalk,
        selectedIcon = Icons.Default.DirectionsRun,
        color = HealthGreen
    )

    object Sleep : Screen(
        route = "sleep",
        title = "Sleep",
        icon = Icons.Default.Bedtime,
        selectedIcon = Icons.Default.Bedtime,
        color = HealthPurple
    )

    object Report : Screen(
        route = "reports",
        title = "Reports",
        icon = Icons.Default.Analytics,
        selectedIcon = Icons.Default.BarChart,
        color = Color.Blue
    )

    object Settings : Screen(
        route = "settings",
        title = "Setting",
        icon = Icons.Default.Settings,
        selectedIcon = Icons.Default.Settings,
        color = Color.Gray
    )
}

@Composable
fun BottomNavigation(currentRoute: String, onNavigate:(String)-> Unit){
    val items = listOf(
        Screen.Dashboard,
        Screen.Water,
        Screen.Step,
        Screen.Sleep,
        Screen.Report,
        Screen.Settings,
    )

    Box(modifier =
        Modifier.fillMaxWidth()
            .height(80.dp)
            .padding(
                bottom = WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding()
            )
            .shadow(elevation = 8.dp,
                shape = RoundedCornerShape(16.dp, topEnd = 16.dp),
                ambientColor = Color.Black.copy(0.1F),
                spotColor = Color.Black.copy(0.1f)
            )
            .clip(RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp))
            .background(Color.White)
    ){

        Row(modifier = Modifier.fillMaxSize()
            .padding(horizontal = 8.dp),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            items.forEach {
                screen ->
                val isSelected = currentRoute == screen.route
                Column(horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center,
                    modifier = Modifier.weight(1f)
                        .clickable{
                            onNavigate(screen.route)
                        }
                        .padding(vertical = 8.dp)
                ) {
                    Icon(
                        imageVector = if(isSelected&& screen.selectedIcon!=null){
                            screen.selectedIcon
                        }else{
                            screen.icon
                        },
                        contentDescription = screen.title,
                        modifier = Modifier.size(24.dp),
                        tint = if(isSelected){
                            screen.color
                            }else{
                                Color.Gray
                            }
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = screen.title,
                        fontSize = 11.sp,
                        fontWeight = if(isSelected){
                            FontWeight.SemiBold
                        }else{
                            FontWeight.Normal
                        },
                        color = if(isSelected){
                            screen.color
                        }else{
                            Color.Gray
                        },
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
        }
    }
}



