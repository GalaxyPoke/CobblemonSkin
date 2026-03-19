/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.util

import com.cobblemon.mod.common.Cobblemon

fun lang(
    subKey: String,
    vararg objects: Any
) = "cobblemon.$subKey".asTranslated(*objects)

fun commandLang(subKey: String, vararg objects: Any ) = lang("command.$subKey", *objects)
fun battleLang(key: String, vararg objects: Any) = lang("battle.$key", *objects)
fun tooltipLang(modId: String = Cobblemon.MODID, key: String, vararg objects: Any) = "item.$modId.$key.tooltip".asTranslated(*objects)