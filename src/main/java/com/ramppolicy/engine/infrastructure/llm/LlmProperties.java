package com.ramppolicy.engine.infrastructure.llm;

import java.time.Duration;
import java.util.Locale;
import java.util.Map;

/**
 * 从环境变量加载的 LLM 解释器配置。
 *
 * @param provider 选择的解释器类型
 * @param timeout 解释器超时时间
 * @param maxInputChars 最大输入字符数
 * @param maxOutputChars 最大输出字符数
 * @param openAiApiKey OpenAI Key，仅 OPENAI 模式填充
 * @param openAiModel OpenAI 模型名称，仅 OPENAI 模式使用
 * @param recordedFile 录制解释来源
 */
public record LlmProperties(
        ExplanationProviderType provider,
        Duration timeout,
        int maxInputChars,
        int maxOutputChars,
        String openAiApiKey,
        String openAiModel,
        String recordedFile) {

    private static final String DEFAULT_RECORDED_FILE = "classpath:recorded-explanations.json";

    /**
     * 从环境变量风格的键值对加载配置。
     *
     * @param env 环境变量映射
     * @return 解析后的配置
     */
    public static LlmProperties from(Map<String, String> env) {
        ExplanationProviderType provider = parseProvider(env.getOrDefault("LLM_PROVIDER", "stub"));
        String apiKey = null;
        String model = null;
        if (provider == ExplanationProviderType.OPENAI) {
            apiKey = blankToNull(env.get("OPENAI_API_KEY"));
            if (apiKey == null) {
                throw new LlmConfigurationException("OPENAI_API_KEY is required when LLM_PROVIDER=openai");
            }
            model = env.getOrDefault("OPENAI_MODEL", "gpt-5-mini");
        }
        return new LlmProperties(
                provider,
                Duration.ofMillis(parseInt(env, "LLM_TIMEOUT_MS", 3000)),
                parseInt(env, "LLM_MAX_INPUT_CHARS", 8000),
                parseInt(env, "LLM_MAX_OUTPUT_CHARS", 2000),
                apiKey,
                model,
                env.getOrDefault("LLM_RECORDED_FILE", DEFAULT_RECORDED_FILE));
    }

    private static ExplanationProviderType parseProvider(String value) {
        return switch (value.toLowerCase(Locale.ROOT)) {
            case "stub" -> ExplanationProviderType.STUB;
            case "recorded" -> ExplanationProviderType.RECORDED;
            case "openai" -> ExplanationProviderType.OPENAI;
            default -> throw new LlmConfigurationException("Unsupported LLM_PROVIDER: " + value);
        };
    }

    private static int parseInt(Map<String, String> env, String key, int defaultValue) {
        String value = env.get(key);
        return value == null || value.isBlank() ? defaultValue : Integer.parseInt(value);
    }

    private static String blankToNull(String value) {
        return value == null || value.isBlank() ? null : value;
    }
}
