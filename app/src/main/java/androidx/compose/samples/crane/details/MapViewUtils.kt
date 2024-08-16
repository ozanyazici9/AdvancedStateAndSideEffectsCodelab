/*
 * Copyright 2020 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package androidx.compose.samples.crane.details

import android.os.Bundle
import androidx.annotation.FloatRange
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.remember
import androidx.compose.samples.crane.R
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import com.google.android.libraries.maps.GoogleMap
import com.google.android.libraries.maps.MapView

/**
 * Kaynak Yönetimi: View bileşenleri genellikle belirli kaynakları yönetir (örneğin, bellek, ağ bağlantıları, sensörler vb.).
 * Bu kaynakların doğru bir şekilde başlatılması ve serbest bırakılması, bellek sızıntıları veya diğer kaynak sızıntılarının önlenmesi
 * açısından kritik öneme sahiptir. Bu yüzden bulunduğu Composable ın vb. yaşam döngüsüne entegre edilmelidir.
 *
 * DisposableEffect, Composable bileşeni ilk kez oluşturulduğunda çalışır ve bu bileşen kaldırılmadan önce (unmounted) veya
 * belirli bir anahtar değiştiğinde bir temizleme (cleanup) işlemi gerçekleştirilir.
 * View bileşenlerinin yaşam döngüsünü yönetmek, örneğin bir harita görünümünü (MapView) başlatmak ve
 * doğru bir şekilde sonlandırmak, sensörleri etkinleştirmek/devre dışı bırakmak gibi durumlar için kullanılır.
 */

// Bir yaşam döngüsü gözlemcisi oluşturduk.
private fun getMapLifecycleObserver(mapView: MapView): LifecycleEventObserver =
    LifecycleEventObserver {_, event ->
        when (event) {
            Lifecycle.Event.ON_CREATE -> mapView.onCreate(Bundle())
            Lifecycle.Event.ON_START -> mapView.onStart()
            Lifecycle.Event.ON_RESUME -> mapView.onResume()
            Lifecycle.Event.ON_PAUSE -> mapView.onPause()
            Lifecycle.Event.ON_STOP -> mapView.onStop()
            Lifecycle.Event.ON_DESTROY -> mapView.onDestroy()
            else -> throw IllegalStateException()
        }
    }

@Composable
fun rememberMapViewWithLifecycle(): MapView {
    val context = LocalContext.current
    // TODO Codelab: DisposableEffect step. Make MapView follow the lifecycle
    val mapView =  remember {
        MapView(context).apply {
            id = R.id.map
        }
    }

    //  mevcut lifecycle alınıp ona gözlemci olarak oluşturduğumuz gözlemciyi veriyoruz bu sayede mevcut lifecycle da ne olursa
    //  mapView içinde lifecycle anlamında aynısı oluyor ve senkron olmuş oluyorlar.
    val lifecycle = LocalLifecycleOwner.current.lifecycle
    DisposableEffect(key1 = lifecycle, key2 = mapView) {
        val lifecycleObserver = getMapLifecycleObserver(mapView)
        lifecycle.addObserver(lifecycleObserver)
        onDispose {
            lifecycle.removeObserver(lifecycleObserver)
        }
    }

    return mapView
}

fun GoogleMap.setZoom(
    @FloatRange(from = MinZoom.toDouble(), to = MaxZoom.toDouble()) zoom: Float
) {
    resetMinMaxZoomPreference()
    setMinZoomPreference(zoom)
    setMaxZoomPreference(zoom)
}
