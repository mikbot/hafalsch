package dev.schlaubi.hafalsch.bot.command

import com.kotlindiscord.kord.extensions.commands.Arguments
import com.kotlindiscord.kord.extensions.commands.application.slash.converters.ChoiceEnum
import com.kotlindiscord.kord.extensions.commands.application.slash.converters.impl.optionalEnumChoice
import dev.schlaubi.hafalsch.marudor.routes.HafasProfile

enum class HafasProfileChoice(val profile: HafasProfile) : ChoiceEnum {
    DB(HafasProfile.DB),
    OEBB(HafasProfile.OEBB),
    BVG(HafasProfile.BVG),
    HVV(HafasProfile.HVV),
    RMV(HafasProfile.RMV),
    SNCB(HafasProfile.SNCB),
    AVV(HafasProfile.AVV),
    NAHSH(HafasProfile.NAHSH),
    INSA(HafasProfile.INSA),
    ANACHB(HafasProfile.ANACHB),
    VAO(HafasProfile.VAO),
    SBB(HafasProfile.SBB),
    DBNETZ(HafasProfile.DBNETZ),
    PKP(HafasProfile.PKP),
    DBREGIO(HafasProfile.DBREGIO),
    SMARTRBL(HafasProfile.SMARTRBL),
    VBN(HafasProfile.VBN);

    override val readableName: String
        get() = profile.serialName
}

fun Arguments.profile() = optionalEnumChoice<HafasProfileChoice> {
    name = "profile"
    typeName = "HafasProfile"
    description = "arguments.profile.description"
}
