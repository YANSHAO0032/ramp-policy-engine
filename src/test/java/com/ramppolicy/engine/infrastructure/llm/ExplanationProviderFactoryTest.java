package com.ramppolicy.engine.infrastructure.llm;

import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertThrows;

class ExplanationProviderFactoryTest {

    @Test
    void unsetProviderDefaultsToStubWithoutOpenAiKey() {
        LlmProperties properties = LlmProperties.from(Map.of());

        ExplanationProvider provider = new ExplanationProviderFactory(properties).create();

        assertEquals(ExplanationProviderType.STUB, properties.provider());
        assertInstanceOf(StubExplanationProvider.class, provider);
    }

    @Test
    void explicitStubIgnoresOpenAiKey() {
        LlmProperties properties = LlmProperties.from(Map.of(
                "LLM_PROVIDER", "stub",
                "OPENAI_API_KEY", "unused"));

        ExplanationProvider provider = new ExplanationProviderFactory(properties).create();

        assertInstanceOf(StubExplanationProvider.class, provider);
    }

    @Test
    void recordedProviderCanBeSelectedOffline() {
        LlmProperties properties = LlmProperties.from(Map.of("LLM_PROVIDER", "recorded"));

        ExplanationProvider provider = new ExplanationProviderFactory(properties).create();

        assertInstanceOf(RecordedExplanationProvider.class, provider);
    }

    @Test
    void openAiProviderRequiresKeyOnlyWhenExplicitlySelected() {
        LlmConfigurationException exception = assertThrows(
                LlmConfigurationException.class,
                () -> LlmProperties.from(Map.of("LLM_PROVIDER", "openai")));

        assertEquals("OPENAI_API_KEY is required when LLM_PROVIDER=openai", exception.getMessage());
    }

    @Test
    void unknownProviderFailsWithClearConfigurationError() {
        LlmConfigurationException exception = assertThrows(
                LlmConfigurationException.class,
                () -> LlmProperties.from(Map.of("LLM_PROVIDER", "mystery")));

        assertEquals("Unsupported LLM_PROVIDER: mystery", exception.getMessage());
    }
}
