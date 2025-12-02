package com.cs407.capstone.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.cs407.capstone.R

@Composable
fun AboutScreen() {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        item {
            Text(
                "Fraud Detection System",
                style = MaterialTheme.typography.headlineMedium,
                modifier = Modifier.padding(bottom = 16.dp)
            )
            Text(
                "This application monitors Gmail notifications for transaction data and automatically processes them for fraud detection.",
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.padding(bottom = 24.dp)
            )
        }
        
        item {
            Text(
                "Meet the Team",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 8.dp)
            )
            Text(
                "We collaborate across engineering and mentorship to deliver trustworthy fraud intelligence.",
                style = MaterialTheme.typography.bodyMedium,
                color = Color.Gray,
                modifier = Modifier.padding(bottom = 24.dp)
            )
        }
        
        item {
            Text(
                "Engineering Team",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 16.dp)
            )
        }
        
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                TeamMemberCard(
                    imageRes = R.drawable.tanush_shrivastava,
                    name = "Tanush Shrivastava",
                    role = "Backend Engineer",
                    modifier = Modifier.weight(1f)
                )
                TeamMemberCard(
                    imageRes = R.drawable.mohammad_janius,
                    name = "Mohammad Izzraff Janius",
                    role = "API Integrations",
                    modifier = Modifier.weight(1f)
                )
            }
        }
        
        item {
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                TeamMemberCard(
                    imageRes = R.drawable.daniel_hsiao,
                    name = "Daniel Hsiao",
                    role = "Model Engineer",
                    modifier = Modifier.weight(1f)
                )
                TeamMemberCard(
                    imageRes = R.drawable.kavya_mathur,
                    name = "Kavya Mathur",
                    role = "UI Engineer",
                    modifier = Modifier.weight(1f)
                )
            }
        }
        
        item {
            Spacer(modifier = Modifier.height(24.dp))
            Text(
                "Mentor Panel",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 16.dp)
            )
        }
        
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                TeamMemberCard(
                    imageRes = R.drawable.peter_daly,
                    name = "Peter Daly",
                    role = "Capital One Mentor",
                    modifier = Modifier.weight(1f)
                )
                TeamMemberCard(
                    imageRes = R.drawable.jillian_jenova,
                    name = "Jillian Jenova",
                    role = "Capital One Mentor",
                    modifier = Modifier.weight(1f)
                )
            }
        }
        
        item {
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                TeamMemberCard(
                    imageRes = R.drawable.garret_huibregtse,
                    name = "Garret Huibregtse",
                    role = "Capital One Mentor",
                    modifier = Modifier.weight(1f)
                )
                TeamMemberCard(
                    imageRes = R.drawable.tyler_luedtke,
                    name = "Tyler Luedtke",
                    role = "Capital One Mentor",
                    modifier = Modifier.weight(1f)
                )
            }
        }
        item {
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {

                TeamMemberCard(
                    imageRes = R.drawable.nolan_smith,
                    name = "Nolan Smith",
                    role = "Capital One Mentor",
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

@Composable
fun TeamMemberCard(
    imageRes: Int,
    name: String,
    role: String,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.padding(4.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Image(
                painter = painterResource(id = imageRes),
                contentDescription = name,
                modifier = Modifier
                    .size(80.dp)
                    .clip(CircleShape),
                contentScale = ContentScale.Crop
            )
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = name,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = role,
                style = MaterialTheme.typography.bodySmall,
                color = Color.Gray,
                textAlign = TextAlign.Center
            )
        }
    }
}
