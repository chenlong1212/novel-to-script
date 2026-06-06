package com.example.novel2script.model;

/**
 * 剧本包装类
 * 用于生成符合 YAML Schema 规范的输出
 * 输出格式要求:
 * script:
 *   schema_version: "1.0"
 *   meta: ...
 */
public class ScriptWrapper {
    private Script script;

    public ScriptWrapper() {
    }

    public ScriptWrapper(Script script) {
        this.script = script;
    }

    public Script getScript() {
        return script;
    }

    public void setScript(Script script) {
        this.script = script;
    }
}
