/*
 * Copyright 2021 The Android Open Source Project
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

package androidx.compose.samples.crane.base

import androidx.annotation.DrawableRes
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.LocalContentColor
import androidx.compose.material.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.Saver
import androidx.compose.runtime.saveable.listSaver
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.samples.crane.ui.captionTextStyle
import androidx.compose.ui.graphics.SolidColor

/**
rememberSaveable, temel türler dışındaki özel sınıfların (stateholder gibi) durumunu saklayabilmek için
bir Saver nesnesine ihtiyaç duyar.

rememberSaveable, temel türler (primitive types) ve Parcelable olan nesneleri otomatik olarak saklayabilir ve geri yükleyebilir.
Ancak, kendi özel bir sınıfınızı (stateholder gibi) saklamak istiyorsanız, Compose'un bu sınıfın durumunu nasıl saklayacağını
ve geri yükleyeceğini bilmesi gerekir. İşte bu noktada Saver devreye girer.
Saver, bir nesneyi (örneğin, EditableUserInputState gibi) bir liste veya başka bir seri hale getirilebilir
formata dönüştürmek ve daha sonra bu formattan geri yüklemek için kullanılır.
Bu, rememberSaveable'ın sınıfın durumunu doğru bir şekilde saklamasını ve geri yüklemesini sağlar.

rememberSaveable içinde tanımlanan Saver, EditableUserInputState nesnesinin mevcut durumunu alır
ve bu durumu seri hale getirilebilir bir formata (genellikle bir liste gibi) dönüştürür.

Bu restore fonksiyonu, saklanan listeden hint ve text değerlerini alır ve bunları kullanarak
yeni bir EditableUserInputState nesnesi oluşturur.

companion object, bir sınıfın statik üyelerine benzer şekilde, sınıfın herhangi bir örneği olmadan kullanılabilecek
üyeleri tanımlar. Saver genellikle companion object içinde tanımlanır, çünkü Saver'a sınıfın her yerinden erişilebilir olmasını sağlar.
companion object içinde Saver tanımlamak, rememberSaveable fonksiyonunda bu Saver'ı kolayca kullanabilmenizi sağlar.
 */

class EditableUserInputState(private val hint: String, initialText: String) {

    var text by  mutableStateOf<String>(initialText)
        private set

    fun updateText(newText: String) {
        text = newText
    }

    val isHint: Boolean
        get() = text == hint

    companion object {
        val Saver: Saver<EditableUserInputState, *> = listSaver(
            save = { listOf(it.hint, it.hint) },
            restore = {
                EditableUserInputState(
                    hint = it[0],
                    initialText = it[1],
                )
            }
        )
    }
}

@Composable
fun rememberEditableUserInputState(hint: String): EditableUserInputState =
    rememberSaveable(hint, saver = EditableUserInputState.Saver) {
        EditableUserInputState(hint, hint)
    }

@Composable
fun CraneEditableUserInput(
    /**
     * Esneklik Sağlama: Bu sayede, durumu CraneEditableUserInput dışındaki bir yerden
     * (örneğin, üst seviye bir Composable veya ViewModel) kontrol edebilirsiniz.
     * CraneEditableUserInput'i kullanan başka bir Composable, EditableUserInputState'i yaratıp yönetebilir.
     *
     * Test Edilebilirlik: Durum dışarıdan kontrol edilebilir hale geldiği için,
     * CraneEditableUserInput bileşenini farklı durumlarla test etmek daha kolay hale gelir.
     * Bu, bileşeni test ederken belirli bir durumu doğrudan geçebilmenizi sağlar.
     *
     * EditableUserInputState sınıfını bir kez tanımladıktan sonra, bu sınıfı ve onunla ilişkili
     * CraneEditableUserInput bileşenini başka bir dosyada veya proje bölümünde kullanmak için
     * tekrar yazmanıza gerek yok. Aynı EditableUserInputState sınıfını ve
     * CraneEditableUserInput bileşenini farklı yerlerde kolayca kullanabilirsiniz. Bu Test
     * edilebilirliği sağlar.
     */
    state: EditableUserInputState = rememberEditableUserInputState(hint = ""),
    caption: String? = null,
    @DrawableRes vectorImageId: Int? = null,
) {
    // TODO Codelab: Encapsulate this state in a state holder

    CraneBaseUserInput(
        caption = caption,
        tintIcon = { !state.isHint },
        showCaption = { !state.isHint },
        vectorImageId = vectorImageId
    ) {
        BasicTextField(
            singleLine = true,
            value = state.text,
            onValueChange = {
                state.updateText(it)
            },
            textStyle = if (state.isHint) {
                captionTextStyle.copy(color = LocalContentColor.current)
            } else {
                MaterialTheme.typography.body1.copy(color = LocalContentColor.current)
            },
            cursorBrush = SolidColor(LocalContentColor.current)
        )
    }
}
