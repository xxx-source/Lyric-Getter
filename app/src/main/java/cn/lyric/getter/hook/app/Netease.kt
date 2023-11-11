package cn.lyric.getter.hook.app

import android.annotation.SuppressLint
import android.content.Context
import cn.lyric.getter.hook.BaseHook
import cn.lyric.getter.tool.HookTools
import cn.lyric.getter.tool.HookTools.MockFlyme
import cn.lyric.getter.tool.HookTools.eventTools
import cn.lyric.getter.tool.HookTools.fuckTinker
import cn.lyric.getter.tool.HookTools.mediaMetadataCompatLyric
import cn.xiaowine.xkt.LogTool.log
import cn.xiaowine.xkt.Tool.isNotNull
import com.github.kyuubiran.ezxhelper.ClassUtils.loadClass
import com.github.kyuubiran.ezxhelper.HookFactory.`-Static`.createHook
import com.github.kyuubiran.ezxhelper.finders.MethodFinder.`-Static`.methodFinder
import com.github.kyuubiran.ezxhelper.paramTypes
import io.luckypray.dexkit.DexKitBridge
import io.luckypray.dexkit.enums.MatchType


@SuppressLint("StaticFieldLeak")
object Netease : BaseHook() {
    override fun init() {
        super.init()
        MockFlyme().mock()
        fuckTinker()
        HookTools.getApplication {
            System.loadLibrary("dexkit")
            val verCode = it.packageManager?.getPackageInfo(it.packageName, 0)?.versionCode ?: 0
            verCode.log()
            if (verCode >= 8000041 || it.packageName == "com.hihonor.cloudmusic") {
                DexKitBridge.create(it.classLoader, false).use { use ->
                    use.isNotNull { bridge ->
                        val result = bridge.findMethodUsingString {
                            usingString = "StatusBarLyricController"
                            matchType = MatchType.FULL
                            methodReturnType = "void"
                            paramTypes(Context::class.java)
                        }
                        result.forEach { res ->
                            loadClass(res.declaringClassName).methodFinder().filterByParamCount(0).filterByReturnType(String::class.java).first().createHook {
                                after { hookParam ->
                                    eventTools.sendLyric(hookParam.result as String)
                                }
                            }
                        }
                    }
                }
            } else {
                mediaMetadataCompatLyric(it.classLoader)
            }
        }
    }
}