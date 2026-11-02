package com.wangzan101.mika.ui

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.dp

/**
 * 设备/对话的小图标:圆角方块 + 两只眼睛,一个 id 一个固定颜色。
 *
 * 以前这里是每台设备起一个 WebView 画会动的卡通形象,颜色又被"界面只有一个色相"统一成了紫,
 * 结果所有设备长得一模一样,只能靠形状区分。现在改成画出来的小图标 —— 颜色是"头像"级的区分,
 * 界面本身(底、卡片、按钮)仍然只有一套紫色。
 *
 * 用固定调色板而不是按色相随机:随机会让相邻两台撞成近似色,也给不出黑、青这类有辨识度的颜色。
 */
private val AvatarPalette = listOf(
    Color(0xFFF2761F), // 橙
    Color(0xFF14A79B), // 青
    Color(0xFF2F6FDE), // 蓝
    Color(0xFF15794B), // 绿
    Color(0xFF12151A), // 近黑
    Color(0xFFE0A31F), // 琥珀
    Color(0xFFD8443C), // 红
    Color(0xFF7A5AE0), // 紫
)

/** 同一个 id 永远是同一个颜色:FNV-1a 取模,只跟 id 有关,改名字不影响。 */
internal fun avatarColorIndex(id: String): Int {
    var hash = 2166136261L
    for (b in id.lowercase(java.util.Locale.ROOT).toByteArray(Charsets.UTF_8)) {
        hash = ((hash xor (b.toInt() and 255).toLong()) * 16777619L) and 0xffffffffL
    }
    return (hash % AvatarPalette.size).toInt()
}

internal fun avatarColor(id: String): Color = AvatarPalette[avatarColorIndex(id)]

/** 设备头像;[active] 为 false 时压暗(还没连上/不在干活),颜色和形状不变。 */
@Composable
fun FleetAvatar(id:String,modifier:Modifier=Modifier,active:Boolean=true) {
    val context=androidx.compose.ui.platform.LocalContext.current
    // 本机用设备自己的稳定 id:改设备名不该换颜色(和以前一样)。
    val stableId=if(id=="local")context.getSharedPreferences("lan-identity",0).getString("id",id) ?: id else id
    val base=avatarColor(stableId)
    val top=if(active)base else base.copy(alpha=0.45f)
    val bottom=if(active)base.copy(alpha=0.86f) else base.copy(alpha=0.32f)
    Canvas(modifier){
        val side=size.minDimension
        drawRoundRect(
            brush=Brush.verticalGradient(listOf(top,bottom)),
            size=Size(side,side),
            cornerRadius=CornerRadius(side*0.28f,side*0.28f),
        )
        val eyeW=side*0.13f; val eyeH=side*0.20f; val eyeY=side*0.42f
        val ink=if(active)Color(0xFF16181D) else Color(0xFF3A3D45)
        for(cx in listOf(side*0.34f,side*0.66f)){
            drawRoundRect(
                color=ink,
                topLeft=Offset(cx-eyeW/2f,eyeY),
                size=Size(eyeW,eyeH),
                cornerRadius=CornerRadius(eyeW/2f,eyeW/2f),
            )
        }
    }
}


@Composable
fun FleetAvatarButton(id:String,name:String,selected:Boolean,onClick:()->Unit,onMention:()->Unit){
    var pressing by androidx.compose.runtime.remember{androidx.compose.runtime.mutableStateOf(false)}
    val progress by androidx.compose.animation.core.animateFloatAsState(if(pressing)1f else 0f,animationSpec=androidx.compose.animation.core.tween(if(pressing)450 else 100),label="mention-fill")
    val haptic=androidx.compose.ui.platform.LocalHapticFeedback.current
    androidx.compose.foundation.layout.Box(Modifier.width(72.dp).height(94.dp).clip(androidx.compose.foundation.shape.RoundedCornerShape(16.dp)).background(if(selected)androidx.compose.material3.MaterialTheme.colorScheme.primaryContainer else androidx.compose.ui.graphics.Color.Transparent)){
        androidx.compose.foundation.layout.Column(horizontalAlignment=androidx.compose.ui.Alignment.CenterHorizontally,modifier=Modifier.fillMaxSize()){
            FleetAvatar(id,Modifier.size(60.dp));androidx.compose.material3.Text(name,style=androidx.compose.material3.MaterialTheme.typography.labelSmall,maxLines=2)
        }
        androidx.compose.foundation.layout.Box(Modifier.fillMaxWidth().fillMaxHeight(progress).background(androidx.compose.material3.MaterialTheme.colorScheme.primary.copy(alpha=0.18f)))
        androidx.compose.foundation.layout.Box(Modifier.matchParentSize().pointerInput(id){detectTapGestures(onTap={onClick()},onLongPress={haptic.performHapticFeedback(androidx.compose.ui.hapticfeedback.HapticFeedbackType.LongPress);onMention()},onPress={pressing=true;tryAwaitRelease();pressing=false})})
    }
}
