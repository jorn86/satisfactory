package org.hertsig.satisfactory.data

import com.google.common.base.CaseFormat

enum class Building(val basePower: Int?) {
    CONSTRUCTOR(4),
    ASSEMBLER(15),
    MANUFACTURER(55),
    SMELTER(4),
    FOUNDRY(16),
    PACKAGER(10),
    REFINERY(30),
    BLENDER(75),
    PARTICLE_ACCELERATOR(null),

    WATER_EXTRACTOR(20),
    ;

    val displayName = CaseFormat.UPPER_UNDERSCORE.to(CaseFormat.UPPER_CAMEL, name)
}
