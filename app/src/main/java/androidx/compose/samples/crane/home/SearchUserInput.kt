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

package androidx.compose.samples.crane.home

import androidx.compose.animation.animateColor
import androidx.compose.animation.core.MutableTransitionState
import androidx.compose.animation.core.tween
import androidx.compose.animation.core.updateTransition
import androidx.compose.foundation.layout.Column
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.State
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.samples.crane.R
import androidx.compose.samples.crane.base.CraneEditableUserInput
import androidx.compose.samples.crane.base.CraneUserInput
import androidx.compose.samples.crane.base.rememberEditableUserInputState
import androidx.compose.samples.crane.home.PeopleUserInputAnimationState.Invalid
import androidx.compose.samples.crane.home.PeopleUserInputAnimationState.Valid
import androidx.compose.samples.crane.ui.CraneTheme
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import kotlinx.coroutines.flow.filter

enum class PeopleUserInputAnimationState { Valid, Invalid }

class PeopleUserInputState {
    var people by mutableStateOf(1)
        private set

    val animationState: MutableTransitionState<PeopleUserInputAnimationState> =
        MutableTransitionState(Valid)

    fun addPerson() {
        people = (people % (MAX_PEOPLE + 1)) + 1
        updateAnimationState()
    }

    private fun updateAnimationState() {
        val newState =
            if (people > MAX_PEOPLE) Invalid
            else Valid

        if (animationState.currentState != newState) animationState.targetState = newState
    }
}

@Composable
fun PeopleUserInput(
    titleSuffix: String? = "",
    onPeopleChanged: (Int) -> Unit,
    peopleState: PeopleUserInputState = remember { PeopleUserInputState() }
) {
    Column {
        val transitionState = remember { peopleState.animationState }
        val tint = tintPeopleUserInput(transitionState)

        val people = peopleState.people
        CraneUserInput(
            text = if (people == 1) "$people Adult$titleSuffix" else "$people Adults$titleSuffix",
            vectorImageId = R.drawable.ic_person,
            tint = tint.value,
            onClick = {
                peopleState.addPerson()
                onPeopleChanged(peopleState.people)
            }
        )
        if (transitionState.targetState == Invalid) {
            Text(
                text = "Error: We don't support more than $MAX_PEOPLE people",
                style = MaterialTheme.typography.body1.copy(color = MaterialTheme.colors.secondary)
            )
        }
    }
}

@Composable
fun FromDestination() {
    CraneUserInput(text = "Seoul, South Korea", vectorImageId = R.drawable.ic_location)
}

@Composable
fun ToDestinationUserInput(onToDestinationChanged: (String) -> Unit) {
    val editableUserInputState = rememberEditableUserInputState(hint = "Choose Destination")
    CraneEditableUserInput(
        state = editableUserInputState,
        caption = "To",
        vectorImageId = R.drawable.ic_plane,
    )

    /** rememberUpdatedState, bir değerin en güncel halini korumak ve
    bu güncel değere güvenli bir şekilde erişmek için kullanılan bir Compose API'sidir.

    by anahtar kelimesi, currentOnDestinationChanged'in onToDestinationChanged
    fonksiyonunu temsil etmesini sağlar, böylece currentOnDestinationChanged
    değişkenini kullanarak aslında onToDestinationChanged fonksiyonunu
    çalıştırmış olursunuz.

    snapshotFlow { editableUserInputState.text }: Bu, editableUserInputState.text'in her değişiminde bir Flow yayını oluşturur.
    Yani, editableUserInputState.text her değiştiğinde bu Flow yeni bir değer üretir.

    filter { !editableUserInputState.isHint }: Flow üzerinde bir filtreleme işlemi yaparak,
    yalnızca editableUserInputState.text değeri hint'e eşit değilse (yani kullanıcı gerçekten bir şeyler yazmışsa) devam eder.

    .collect { ... }: collect bloğu, Flow'dan gelen verileri dinler ve her yeni veri geldiğinde
    (bu durumda editableUserInputState.text değeri güncellendiğinde) çalışır.

    currentOnDestinationChanged(editableUserInputState.text): editableUserInputState.text'in yeni değerini alır
    ve bu değeri currentOnDestinationChanged fonksiyonuna geçirir. Yani, bu fonksiyon her çalıştırıldığında
    editableUserInputState.text'in güncel değeri onToDestinationChanged lambda fonksiyonuna iletilir.

    LaunchedEffect bir Composable'da, belirli bir key veya key seti değiştiğinde bir yan etkiyi başlatmak için kullanılır.
    Bu yan etkiler, bir suspend fonksiyon çalıştırmak veya bir akış (flow) dinlemek gibi işlemler olabilir.
     Bizde burda editableUserInputState.text her değitiğinde text de yazan değere göre önerilen yerler listesini güncellemek istiyoruz.
     Yani bir değişiklik başka bir değişikliği tetikliyor buna YAN ETKİ denir. Bu yan etki ele almak için LaunhedEffect kullanılır.
     buradaki parametre olarak geçirilmiş onToDestinationChanged fonksiyonu aslında viewmodelda önerilen yerler listesini güncelleyen
     toDestinationChanged fonksiyonunu temsil ediyor yani burada text state indeki değişikliğin bir yan etkisi olarak önerilen
    yerlerin değişmesini sağlıyoruz.

     */
    val currentOnDestinationChanged by rememberUpdatedState(onToDestinationChanged)
    LaunchedEffect(editableUserInputState) {
        snapshotFlow { editableUserInputState.text }
            .filter { !editableUserInputState.isHint }
            .collect {
                currentOnDestinationChanged(editableUserInputState.text)
            }
    }
}

@Composable
fun DatesUserInput() {
    CraneUserInput(
        caption = "Select Dates",
        text = "",
        vectorImageId = R.drawable.ic_calendar
    )
}

@Composable
private fun tintPeopleUserInput(
    transitionState: MutableTransitionState<PeopleUserInputAnimationState>
): State<Color> {
    val validColor = MaterialTheme.colors.onSurface
    val invalidColor = MaterialTheme.colors.secondary

    val transition = updateTransition(transitionState, label = "")
    return transition.animateColor(
        transitionSpec = { tween(durationMillis = 300) }, label = ""
    ) {
        if (it == Valid) validColor else invalidColor
    }
}

@Preview
@Composable
fun PeopleUserInputPreview() {
    CraneTheme {
        PeopleUserInput(onPeopleChanged = {})
    }
}
